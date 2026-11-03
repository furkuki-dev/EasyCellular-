import Foundation

/// Одна запись результата теста.
struct SpeedRecord: Codable, Identifiable {
    let id: UUID
    let date: Date
    let download: Double
    let upload: Double
    let ping: Double?
    let rat: String?
    let carrier: String?
    let generation: NetworkGeneration
}

/// История тестов в UserDefaults (последние 200 записей).
final class HistoryStore: ObservableObject {
    static let shared = HistoryStore()

    @Published private(set) var records: [SpeedRecord] = []

    private let key = "ec.history"
    private let limit = 200

    private init() {
        if let data = UserDefaults.standard.data(forKey: key),
           let decoded = try? JSONDecoder().decode([SpeedRecord].self, from: data) {
            records = decoded
        }
    }

    func add(_ record: SpeedRecord) {
        records.insert(record, at: 0)
        if records.count > limit {
            records = Array(records.prefix(limit))
        }
        save()
    }

    func delete(at offsets: IndexSet) {
        for index in offsets.sorted(by: >) where records.indices.contains(index) {
            records.remove(at: index)
        }
        save()
    }

    func clear() {
        records = []
        save()
    }

    private func save() {
        guard let data = try? JSONEncoder().encode(records) else { return }
        UserDefaults.standard.set(data, forKey: key)
    }
}
