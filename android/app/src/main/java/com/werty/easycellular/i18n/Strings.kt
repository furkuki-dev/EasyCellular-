package com.werty.easycellular.i18n

import androidx.compose.runtime.staticCompositionLocalOf

enum class Lang { RU, EN }

class L(val lang: Lang) {
    fun t(key: String): String {
        val pair = Strings.table[key] ?: return key
        return if (lang == Lang.RU) pair.first else pair.second
    }

    fun f(key: String, vararg args: Any): String = String.format(t(key), *args)

    fun yn(flag: Boolean): String = t(if (flag) "yes" else "no")
}

val LocalL = staticCompositionLocalOf { L(Lang.RU) }

object Strings {
    val table: Map<String, Pair<String, String>> = mapOf(
        // Вкладки
        "tab_network" to ("Сеть" to "Network"),
        "tab_test" to ("Тест" to "Speed"),
        "tab_history" to ("История" to "History"),
        "tab_device" to ("Устройство" to "Device"),
        "tab_settings" to ("Настройки" to "Settings"),

        // Общее
        "yes" to ("Да" to "Yes"),
        "no" to ("Нет" to "No"),
        "cancel" to ("Отмена" to "Cancel"),
        "mbps" to ("Мбит/с" to "Mbps"),
        "ms" to ("мс" to "ms"),
        "fmt_ms" to ("%.0f мс" to "%.0f ms"),
        "fmt_mbps" to ("%.0f Мбит/с" to "%.0f Mbps"),
        "fmt_mhz" to ("%.1f МГц" to "%.1f MHz"),
        "fmt_pct" to ("%.0f%%" to "%.0f%%"),
        "fmt_mb" to ("%.1f МБ" to "%.1f MB"),
        "share_text" to ("EasyCellular: ↓ %.1f / ↑ %.1f Мбит/с, пинг %.0f мс" to "EasyCellular: ↓ %.1f / ↑ %.1f Mbps, ping %.0f ms"),

        // Сеть: состояние
        "gen_none" to ("Нет сети" to "No network"),
        "gen_wifi" to ("Wi-Fi" to "Wi-Fi"),
        "gen_unknown" to ("Неизвестно" to "Unknown"),
        "state_offline" to ("Нет подключения" to "No connection"),
        "state_wifi" to ("Активен Wi-Fi" to "Wi-Fi active"),
        "state_connected" to ("Подключено" to "Connected"),
        "state_no_service" to ("Нет сервиса" to "No service"),
        "perm_banner" to ("Выдайте разрешения, чтобы видеть точный сигнал, вышку и оператора." to "Grant permissions to see exact signal, cell and carrier."),
        "perm_grant" to ("Разрешить" to "Allow"),
        "perm_settings" to ("Открыть настройки" to "Open settings"),
        "net_footnote" to ("Данные берутся напрямую из Android Telephony API. Точность зависит от прошивки и оператора." to "Data comes directly from the Android Telephony API. Accuracy depends on firmware and carrier."),

        // Сеть: карточки
        "card_signal" to ("Сигнал" to "Signal"),
        "card_cell" to ("Вышка" to "Cell"),
        "card_carrier" to ("Оператор и SIM" to "Carrier & SIM"),
        "card_link" to ("Подключение и IP" to "Connection & IP"),
        "card_cells" to ("Все ячейки" to "All cells"),
        "cells_empty" to ("Нет данных о ячейках. Выдайте доступ к геолокации." to "No cell data. Grant location access."),
        "q_excellent" to ("Отлично" to "Excellent"),
        "q_good" to ("Хорошо" to "Good"),
        "q_fair" to ("Средне" to "Fair"),
        "q_poor" to ("Слабо" to "Poor"),
        "dbm_label" to ("Уровень, dBm" to "Level, dBm"),
        "tech" to ("Технология" to "Technology"),
        "band" to ("Диапазон" to "Band"),
        "pci" to ("PCI" to "PCI"),
        "channel" to ("Канал (EARFCN / NRARFCN)" to "Channel (EARFCN / NRARFCN)"),
        "cell_id" to ("Cell ID" to "Cell ID"),
        "tac" to ("TAC" to "TAC"),
        "bandwidth" to ("Ширина канала" to "Channel bandwidth"),
        "registered" to ("Текущая ячейка" to "Serving cell"),
        "carrier" to ("Оператор" to "Carrier"),
        "carrier_unknown" to ("Недоступно" to "Unavailable"),
        "mccmnc" to ("MCC-MNC" to "MCC-MNC"),
        "country" to ("Страна" to "Country"),
        "roaming" to ("Роуминг" to "Roaming"),
        "sim_count" to ("SIM-карт" to "SIM cards"),
        "sim_slot" to ("Слот %d" to "Slot %d"),
        "embedded" to ("eSIM" to "eSIM"),
        "transport" to ("Канал" to "Transport"),
        "transport_cellular" to ("Сотовая сеть" to "Cellular"),
        "transport_wifi" to ("Wi-Fi" to "Wi-Fi"),
        "transport_ethernet" to ("Ethernet" to "Ethernet"),
        "transport_vpn" to ("VPN" to "VPN"),
        "transport_none" to ("Нет" to "None"),
        "internet" to ("Интернет проверен" to "Internet verified"),
        "metered" to ("Лимитный трафик" to "Metered"),
        "down_link" to ("Канал вниз (макс.)" to "Downlink (max)"),
        "up_link" to ("Канал вверх (макс.)" to "Uplink (max)"),
        "iface" to ("Интерфейс" to "Interface"),
        "ipv4" to ("IPv4" to "IPv4"),
        "dns" to ("DNS" to "DNS"),
        "mtu" to ("MTU" to "MTU"),

        // Тест
        "phase_idle" to ("Нажмите, чтобы начать" to "Tap to start"),
        "phase_ping" to ("Измеряем пинг…" to "Measuring ping…"),
        "phase_down" to ("Загрузка…" to "Downloading…"),
        "phase_up" to ("Отдача…" to "Uploading…"),
        "phase_done" to ("Готово" to "Done"),
        "phase_failed" to ("Тест не удался. Проверьте подключение." to "Test failed. Check your connection."),
        "ping" to ("Пинг" to "Ping"),
        "download" to ("Загрузка" to "Download"),
        "upload" to ("Отдача" to "Upload"),
        "test_start" to ("Начать тест" to "Start test"),
        "test_stop" to ("Остановить" to "Stop"),
        "details" to ("Детали" to "Details"),
        "min" to ("Минимум" to "Minimum"),
        "max" to ("Максимум" to "Maximum"),
        "jitter" to ("Джиттер" to "Jitter"),
        "loss" to ("Потери" to "Loss"),
        "data_used" to ("Трафик за тест" to "Data used"),
        "gen_result" to ("Тип сети" to "Network type"),
        "share" to ("Поделиться" to "Share"),

        // История
        "hist_chart" to ("Загрузка за последние тесты" to "Download over recent tests"),
        "hist_empty" to ("Пока пусто" to "Nothing here yet"),
        "hist_empty_hint" to ("Результаты тестов появятся здесь" to "Test results will appear here"),
        "hist_clear" to ("Очистить" to "Clear"),
        "hist_clear_ask" to ("Удалить всю историю?" to "Delete all history?"),

        // Устройство
        "device_platform" to ("Платформа" to "Platform"),
        "device_detected_by" to ("Определено по" to "Detected by"),
        "device_version" to ("Версия ОС" to "OS version"),
        "device_api" to ("API уровень" to "API level"),
        "device_patch" to ("Патч безопасности" to "Security patch"),
        "device_maker" to ("Производитель" to "Manufacturer"),
        "device_model" to ("Модель" to "Model"),
        "device_abi" to ("Архитектура" to "Architecture"),
        "device_feature" to ("Поддержка функций" to "Feature support"),
        "feat_display" to ("Точный тип сети (5G+)" to "Exact network type (5G+)"),
        "feat_cells" to ("Данные вышек (RSRP, SINR)" to "Cell data (RSRP, SINR)"),
        "feat_a17" to ("Android 17 (API 37)" to "Android 17 (API 37)"),

        // Настройки
        "set_language" to ("Язык" to "Language"),
        "set_ping_host" to ("Хост для пинга" to "Ping host"),
        "set_permissions" to ("Разрешения" to "Permissions"),
        "set_clear" to ("Очистить историю" to "Clear history"),
        "set_about" to ("О приложении" to "About"),
        "set_version" to ("Версия" to "Version"),
        "set_about_text" to ("EasyCellular показывает точные данные сотовой сети, пинг и скорость. Тесты используют публичные серверы и расходуют трафик." to "EasyCellular shows exact cellular data, ping and speed. Tests use public servers and consume data.")
    )
}
