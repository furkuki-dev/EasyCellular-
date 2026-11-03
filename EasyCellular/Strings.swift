import Foundation

/// Все тексты приложения. Чтобы добавить язык, допишите код во все строки.
enum Strings {
    static let table: [String: [String: String]] = [
        // Вкладки
        "tab_network": ["ru": "Сеть", "en": "Network"],
        "tab_test": ["ru": "Тест", "en": "Speed"],
        "tab_history": ["ru": "История", "en": "History"],
        "tab_settings": ["ru": "Настройки", "en": "Settings"],

        // Общие
        "common_yes": ["ru": "Да", "en": "Yes"],
        "common_no": ["ru": "Нет", "en": "No"],
        "common_cancel": ["ru": "Отмена", "en": "Cancel"],

        // Форматы
        "fmt_ms": ["ru": "%.0f мс", "en": "%.0f ms"],
        "fmt_mbps": ["ru": "%.0f Мбит/с", "en": "%.0f Mbps"],
        "fmt_pct": ["ru": "%.0f%%", "en": "%.0f%%"],
        "fmt_mb": ["ru": "%.1f МБ", "en": "%.1f MB"],
        "sim_label": ["ru": "SIM %ld", "en": "SIM %ld"],

        // Сеть
        "net_title": ["ru": "Сотовая сеть", "en": "Cellular"],
        "net_connected": ["ru": "Подключено", "en": "Connected"],
        "net_on_wifi": ["ru": "Активен Wi-Fi", "en": "Wi-Fi active"],
        "net_no_connection": ["ru": "Нет подключения", "en": "No connection"],
        "net_footnote": ["ru": "iOS не даёт приложениям уровень сигнала, вышки и диапазоны, поэтому здесь показано только то, что разрешает система. 5G+ — это 5G при скорости теста от %.0f Мбит/с.", "en": "iOS does not give apps signal level, towers or bands, so this screen shows only what the system allows. 5G+ means 5G with a test speed of %.0f Mbps or more."],
        "gen_offline": ["ru": "Нет сети", "en": "No network"],
        "gen_unknown": ["ru": "Неизвестно", "en": "Unknown"],
        "card_carrier": ["ru": "Оператор", "en": "Carrier"],
        "card_connection": ["ru": "Подключение", "en": "Connection"],
        "card_sims": ["ru": "SIM-карты", "en": "SIM cards"],
        "card_ip": ["ru": "IP-адреса", "en": "IP addresses"],
        "net_carrier": ["ru": "Оператор", "en": "Carrier"],
        "net_carrier_unknown": ["ru": "Недоступно в iOS", "en": "Not provided by iOS"],
        "net_tech": ["ru": "Технология", "en": "Technology"],
        "net_country": ["ru": "Страна", "en": "Country"],
        "net_mccmnc": ["ru": "Код MCC-MNC", "en": "MCC-MNC code"],
        "net_cellular_data": ["ru": "Мобильные данные", "en": "Mobile data"],
        "cell_allowed": ["ru": "Разрешены", "en": "Allowed"],
        "cell_denied": ["ru": "Отключены", "en": "Disabled"],
        "cell_unknown": ["ru": "Неизвестно", "en": "Unknown"],
        "net_low_data": ["ru": "Экономия данных", "en": "Low Data Mode"],
        "net_expensive": ["ru": "Платный канал", "en": "Metered connection"],
        "net_active_iface": ["ru": "Активная сеть", "en": "Active network"],
        "iface_wifi": ["ru": "Wi-Fi", "en": "Wi-Fi"],
        "iface_cellular": ["ru": "Сотовая", "en": "Cellular"],
        "iface_ethernet": ["ru": "Ethernet", "en": "Ethernet"],
        "iface_none": ["ru": "Нет", "en": "None"],
        "net_ip_cell": ["ru": "Сотовый IP", "en": "Cellular IP"],
        "net_ip_wifi": ["ru": "IP Wi-Fi", "en": "Wi-Fi IP"],
        "net_ip_public": ["ru": "Внешний IP", "en": "Public IP"],
        "net_get_public": ["ru": "Определить внешний IP", "en": "Detect public IP"],
        "net_last_result": ["ru": "Последний тест", "en": "Last test"],
        "net_no_results": ["ru": "Пока нет результатов. Запустите тест на вкладке «Тест».", "en": "No results yet. Run a test on the Speed tab."],

        // Тест
        "test_title": ["ru": "Тест скорости", "en": "Speed test"],
        "test_start": ["ru": "Начать тест", "en": "Start test"],
        "test_stop": ["ru": "Остановить", "en": "Stop"],
        "test_mbps": ["ru": "Мбит/с", "en": "Mbps"],
        "test_ms": ["ru": "мс", "en": "ms"],
        "test_phase_idle": ["ru": "Нажмите, чтобы начать", "en": "Tap to start"],
        "test_phase_ping": ["ru": "Измеряем пинг…", "en": "Measuring ping…"],
        "test_phase_download": ["ru": "Загрузка…", "en": "Downloading…"],
        "test_phase_upload": ["ru": "Отдача…", "en": "Uploading…"],
        "test_phase_done": ["ru": "Готово", "en": "Done"],
        "test_failed": ["ru": "Тест не удался. Проверьте подключение.", "en": "Test failed. Check your connection."],
        "test_ping": ["ru": "Пинг", "en": "Ping"],
        "test_download": ["ru": "Загрузка", "en": "Download"],
        "test_upload": ["ru": "Отдача", "en": "Upload"],
        "test_result_title": ["ru": "Детали", "en": "Details"],
        "test_min": ["ru": "Минимум", "en": "Minimum"],
        "test_max": ["ru": "Максимум", "en": "Maximum"],
        "test_jitter": ["ru": "Джиттер", "en": "Jitter"],
        "test_loss": ["ru": "Потери пакетов", "en": "Packet loss"],
        "test_generation": ["ru": "Тип сети", "en": "Network type"],
        "test_data_used": ["ru": "Израсходовано трафика", "en": "Data used"],
        "test_share": ["ru": "Поделиться результатом", "en": "Share result"],
        "share_format": ["ru": "EasyCellular: ↓ %.1f / ↑ %.1f Мбит/с, пинг %.0f мс", "en": "EasyCellular: ↓ %.1f / ↑ %.1f Mbps, ping %.0f ms"],

        // История
        "hist_chart": ["ru": "Загрузка за последние тесты", "en": "Download over recent tests"],
        "hist_clear": ["ru": "Очистить", "en": "Clear"],
        "hist_clear_confirm": ["ru": "Удалить всю историю?", "en": "Delete all history?"],
        "hist_empty": ["ru": "Пока пусто", "en": "Nothing here yet"],
        "hist_empty_hint": ["ru": "Результаты тестов скорости появятся здесь", "en": "Speed test results will appear here"],

        // Настройки
        "set_language": ["ru": "Язык", "en": "Language"],
        "set_language_footer": ["ru": "Язык интерфейса приложения", "en": "App interface language"],
        "set_ping_host": ["ru": "Хост для пинга", "en": "Ping host"],
        "set_ping_footer": ["ru": "Пинг измеряет время TCP-подключения к этому адресу на порту 443.", "en": "Ping measures TCP connection time to this address on port 443."],
        "set_data": ["ru": "Данные", "en": "Data"],
        "set_clear_history": ["ru": "Очистить историю", "en": "Clear history"],
        "set_about": ["ru": "О приложении", "en": "About"],
        "set_version": ["ru": "Версия", "en": "Version"],
        "set_about_text": ["ru": "EasyCellular показывает тип сотовой сети, оператора, пинг и скорость. Тесты используют публичные серверы и расходуют трафик.", "en": "EasyCellular shows cellular network type, carrier, ping and speed. Tests use public servers and consume data."],
    ]
}
