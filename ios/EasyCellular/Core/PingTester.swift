import Foundation
import Network

struct PingStats {
    let average: Double
    let minimum: Double
    let maximum: Double
    let jitter: Double
    let lossPercent: Double
}

/// Пинг через TCP-подключение: ICMP на iOS недоступен без спец. прав.
enum PingTester {
    /// Выполняет `count` замеров и возвращает статистику (nil, если все попытки неудачны).
    static func run(host: String, count: Int = 10) async -> PingStats? {
        var samples: [Double] = []
        var lost = 0

        for _ in 0..<count {
            if Task.isCancelled { break }
            if let rtt = await connectTime(host: host) {
                samples.append(rtt)
            } else {
                lost += 1
            }
            try? await Task.sleep(nanoseconds: 150_000_000)
        }

        guard !samples.isEmpty else { return nil }

        let average = samples.reduce(0, +) / Double(samples.count)
        var jitter = 0.0
        if samples.count > 1 {
            let diffs = zip(samples, samples.dropFirst()).map { pair in abs(pair.1 - pair.0) }
            jitter = diffs.reduce(0, +) / Double(diffs.count)
        }

        return PingStats(
            average: average,
            minimum: samples.min() ?? 0,
            maximum: samples.max() ?? 0,
            jitter: jitter,
            lossPercent: Double(lost) / Double(count) * 100
        )
    }

    /// Время установления TCP-соединения в миллисекундах или nil при таймауте/ошибке.
    private static func connectTime(host: String, port: UInt16 = 443, timeout: Double = 2) async -> Double? {
        guard let nwPort = NWEndpoint.Port(rawValue: port) else { return nil }

        return await withCheckedContinuation { (continuation: CheckedContinuation<Double?, Never>) in
            let queue = DispatchQueue(label: "ec.ping")
            let connection = NWConnection(host: NWEndpoint.Host(host), port: nwPort, using: .tcp)
            let started = DispatchTime.now().uptimeNanoseconds
            var finished = false

            // Все обращения идут с одной очереди, поэтому гонок нет.
            let finish: (Double?) -> Void = { value in
                guard !finished else { return }
                finished = true
                connection.cancel()
                continuation.resume(returning: value)
            }

            connection.stateUpdateHandler = { state in
                switch state {
                case .ready:
                    let elapsed = DispatchTime.now().uptimeNanoseconds - started
                    finish(Double(elapsed) / 1_000_000)
                case .failed, .cancelled:
                    finish(nil)
                default:
                    break
                }
            }

            queue.asyncAfter(deadline: .now() + timeout) { finish(nil) }
            connection.start(queue: queue)
        }
    }
}
