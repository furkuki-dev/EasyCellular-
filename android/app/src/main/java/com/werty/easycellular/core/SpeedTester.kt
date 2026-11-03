package com.werty.easycellular.core

import android.net.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.max
import kotlin.math.min

enum class Direction { DOWN, UP }

object SpeedConfig {
    const val DOWN_URL = "https://speed.cloudflare.com/__down?bytes="
    const val UP_URL = "https://speed.cloudflare.com/__up"
    const val STREAMS = 4
    const val DOWN_CHUNK = 25_000_000
    const val UP_CHUNK = 4_000_000
    const val DURATION_MS = 8_000L
    const val WARMUP_MS = 2_000L
    const val GAUGE_MAX = 1500.0
    const val FIVE_G_PLUS_MBPS = 500.0
}

/**
 * Замер пропускной способности через выбранную сеть.
 * Несколько параллельных потоков, каждый запрос открывается заново по завершении предыдущего.
 */
class ThroughputMeter(private val network: Network?) {

    /** Возвращает (средняя скорость Мбит/с после прогрева, всего байт). */
    suspend fun measure(direction: Direction, onProgress: (Double) -> Unit): Pair<Double, Long> = coroutineScope {
        val total = AtomicLong(0)
        val startNs = System.nanoTime()
        val deadline = startNs + SpeedConfig.DURATION_MS * 1_000_000L

        val workers = List(SpeedConfig.STREAMS) {
            launch(Dispatchers.IO) {
                while (isActive && System.nanoTime() < deadline) {
                    try {
                        if (direction == Direction.DOWN) downloadChunk(total, deadline)
                        else uploadChunk(total, deadline)
                    } catch (e: Exception) {
                        break
                    }
                }
            }
        }

        val samples = ArrayList<Pair<Long, Long>>()
        while (System.nanoTime() < deadline && workers.any { it.isActive }) {
            delay(200)
            val t = (System.nanoTime() - startNs) / 1_000_000
            val b = total.get()
            samples += t to b
            samples.lastOrNull { t - it.first >= 1000 }?.let { base ->
                onProgress((b - base.second) * 8.0 / (t - base.first) / 1000.0)
            }
        }
        workers.forEach { it.cancel() }

        val endT = (System.nanoTime() - startNs) / 1_000_000
        val endB = total.get()
        val warm = samples.firstOrNull { it.first >= SpeedConfig.WARMUP_MS }
        val mbps = if (warm != null && endT > warm.first) {
            (endB - warm.second) * 8.0 / (endT - warm.first) / 1000.0
        } else {
            endB * 8.0 / max(endT, 1L) / 1000.0
        }
        mbps to endB
    }

    private fun open(url: URL): HttpURLConnection {
        val conn = (network?.openConnection(url) ?: url.openConnection()) as HttpURLConnection
        conn.connectTimeout = 5000
        conn.readTimeout = 5000
        conn.useCaches = false
        return conn
    }

    private fun downloadChunk(total: AtomicLong, deadline: Long) {
        val conn = open(URL(SpeedConfig.DOWN_URL + SpeedConfig.DOWN_CHUNK))
        try {
            conn.inputStream.use { input ->
                val buf = ByteArray(64 * 1024)
                while (System.nanoTime() < deadline) {
                    val n = input.read(buf)
                    if (n < 0) break
                    total.addAndGet(n.toLong())
                }
            }
        } finally {
            conn.disconnect()
        }
    }

    private fun uploadChunk(total: AtomicLong, deadline: Long) {
        val conn = open(URL(SpeedConfig.UP_URL)).apply {
            requestMethod = "POST"
            doOutput = true
            setFixedLengthStreamingMode(SpeedConfig.UP_CHUNK)
            setRequestProperty("Content-Type", "application/octet-stream")
        }
        try {
            conn.outputStream.use { out ->
                val buf = ByteArray(64 * 1024)
                var sent = 0
                while (sent < SpeedConfig.UP_CHUNK && System.nanoTime() < deadline) {
                    val n = min(buf.size, SpeedConfig.UP_CHUNK - sent)
                    out.write(buf, 0, n)
                    sent += n
                    total.addAndGet(n.toLong())
                }
            }
            conn.responseCode
        } finally {
            conn.disconnect()
        }
    }
}
