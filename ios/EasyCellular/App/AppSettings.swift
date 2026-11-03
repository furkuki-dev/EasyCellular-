import Foundation

enum AppLanguage: String, CaseIterable, Identifiable {
    case ru
    case en

    var id: String { rawValue }

    var displayName: String {
        switch self {
        case .ru: return "Русский"
        case .en: return "English"
        }
    }
}

/// Настройки приложения: язык интерфейса и хост для пинга.
final class AppSettings: ObservableObject {
    static let shared = AppSettings()

    @Published var language: AppLanguage {
        didSet { UserDefaults.standard.set(language.rawValue, forKey: "ec.language") }
    }

    @Published var pingHost: String {
        didSet { UserDefaults.standard.set(pingHost, forKey: "ec.pingHost") }
    }

    /// Хост, который реально используется для пинга (защита от пустого поля).
    var safePingHost: String {
        let trimmed = pingHost.trimmingCharacters(in: .whitespacesAndNewlines)
        return trimmed.isEmpty ? "1.1.1.1" : trimmed
    }

    private init() {
        let defaults = UserDefaults.standard
        if let raw = defaults.string(forKey: "ec.language"), let saved = AppLanguage(rawValue: raw) {
            language = saved
        } else {
            let system = Locale.preferredLanguages.first ?? "en"
            language = system.hasPrefix("ru") ? .ru : .en
        }
        pingHost = defaults.string(forKey: "ec.pingHost") ?? "1.1.1.1"
    }

    /// Перевод строки по ключу на выбранном языке.
    func t(_ key: String) -> String {
        guard let entry = Strings.table[key] else { return key }
        return entry[language.rawValue] ?? entry["en"] ?? key
    }

    /// Перевод с подстановкой значений (аналог String(format:)).
    func f(_ key: String, _ args: CVarArg...) -> String {
        String(format: t(key), arguments: args)
    }
}
