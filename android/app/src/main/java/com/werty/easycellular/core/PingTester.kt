package com.werty.easycellular.core

import android.net.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket
import kotlin.math.abs

data class PingResult(
    val avg: Double,
    val min: Double,
    val max: Double,
    val jitter: Double,
    val loss: Double
)

/** Пинг через TCP-подключение, привязанное к выбранной сети. ICMP без root недоступен. */
object PingTester {

    suspend fun run(host: String, network: Network?, count: Int = 10, port: Int = 443): PingResult? =
        withContext(Dispatchers.IO) {
            val samples = mutableListOf<Double>()
            var lost = 0
            repeat(count) {
                val rtt = connectMs(host, port, network)
                if (rtt != null) {
                    samples += rtt
                } else {
                    lost++
                }
                delay(150)
            }
            if (samples.isEmpty()) return@withContext null
            val jitter = if (samples.size > 1) samples.zipWithNext { a, b -> abs(b - a) }.average() else 0.0
            PingResult(
                avg = samples.average(),
                min = samples.min(),
                max = samples.max(),
                jitter = jitter,
                loss = lost * 100.0 / count
            )
        }

    private fun connectMs(host: String, port: Int, network: Network?): Double? = try {
        Socket().use { socket ->
            network?.bindSocket(socket)
            val start = System.nanoTime()
            socket.connect(InetSocketAddress(host, port), 2000)
            (System.nanoTime() - start) / 1_000_000.0
        }
    } catch (e: Exception) {
        null
    }
}
