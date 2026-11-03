package com.werty.easycellular

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.werty.easycellular.core.*
import com.werty.easycellular.i18n.Lang
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

enum class TestPhase { IDLE, PING, DOWN, UP, DONE, FAILED }

data class SpeedUi(
    val phase: TestPhase = TestPhase.IDLE,
    val live: Double = 0.0,
    val ping: PingResult? = null,
    val down: Double? = null,
    val up: Double? = null,
    val gen: Gen = Gen.UNKNOWN,
    val bytes: Long = 0L
)

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("ec_prefs", Context.MODE_PRIVATE)
    private val collector = NetworkCollector(app)
    private val historyStore = HistoryStore(app)

    private val _lang = MutableStateFlow(
        prefs.getString("lang", null)?.let { runCatching { Lang.valueOf(it) }.getOrNull() }
            ?: if (Locale.getDefault().language == "ru") Lang.RU else Lang.EN
    )
    val language: StateFlow<Lang> = _lang.asStateFlow()

    private val _pingHost = MutableStateFlow(prefs.getString("ping_host", "1.1.1.1") ?: "1.1.1.1")
    val pingHost: StateFlow<String> = _pingHost.asStateFlow()

    private val _network = MutableStateFlow(NetworkSnapshot())
    val network: StateFlow<NetworkSnapshot> = _network.asStateFlow()

    val history: StateFlow<List<SpeedRecord>> = historyStore.records

    private val _speed = MutableStateFlow(SpeedUi())
    val speed: StateFlow<SpeedUi> = _speed.asStateFlow()

    val device: DeviceOsInfo = PlatformDetector.detect()

    private var speedJob: Job? = null

    init {
        collector.startListening()
        viewModelScope.launch {
            while (isActive) {
                _network.value = withContext(Dispatchers.IO) { collector.collect() }
                delay(1500)
            }
        }
    }

    fun setLanguage(lang: Lang) {
        _lang.value = lang
        prefs.edit().putString("lang", lang.name).apply()
    }

    fun setPingHost(host: String) {
        _pingHost.value = host
        prefs.edit().putString("ping_host", host).apply()
    }

    fun onPermissionsChanged() = collector.startListening()

    fun missingPermissions(): List<String> = collector.missingPermissions()

    fun clearHistory() = historyStore.clear()

    fun startTest() {
        if (speedJob?.isActive == true) return
        speedJob = viewModelScope.launch {
            val net = collector.activeNetwork()
            val snap = _network.value
            _speed.value = SpeedUi(phase = TestPhase.PING)

            val ping = withContext(Dispatchers.IO) { PingTester.run(_pingHost.value, net) }
            _speed.update { it.copy(phase = TestPhase.DOWN, ping = ping) }

            val meter = ThroughputMeter(net)
            val (down, downBytes) = meter.measure(Direction.DOWN) { v -> _speed.update { it.copy(live = v) } }
            if (downBytes == 0L) {
                _speed.value = SpeedUi(phase = TestPhase.FAILED, ping = ping)
                return@launch
            }
            _speed.update { it.copy(phase = TestPhase.UP, down = down, live = 0.0) }

            val (up, upBytes) = meter.measure(Direction.UP) { v -> _speed.update { it.copy(live = v) } }

            val gen = when {
                snap.transport == "wifi" -> Gen.WIFI
                snap.gen == Gen.G5 && down >= SpeedConfig.FIVE_G_PLUS_MBPS -> Gen.G5_PLUS
                else -> snap.gen
            }
            _speed.update {
                it.copy(phase = TestPhase.DONE, up = up, live = down, gen = gen, bytes = downBytes + upBytes)
            }

            val now = System.currentTimeMillis()
            historyStore.add(
                SpeedRecord(
                    id = now, time = now, down = down, up = up,
                    ping = ping?.avg, gen = gen, carrier = snap.carrier
                )
            )
        }
    }

    fun stopTest() {
        speedJob?.cancel()
        speedJob = null
        _speed.value = SpeedUi()
    }

    override fun onCleared() {
        collector.stopListening()
        super.onCleared()
    }
}
