import Foundation

enum SpeedConfig {
    /// Публичные эндпоинты Cloudflare для замера скорости.
    static let downloadEndpoint = "https://speed.cloudflare.com/__down?bytes="
    static let uploadEndpoint = "https://speed.cloudflare.com/__up"

    static let streams = 4                       // параллельных потоков
    static let downloadChunkBytes = 25_000_000   // размер одного запроса загрузки
    static let uploadChunkBytes = 4_000_000      // размер одного запроса отдачи
    static let testDuration: TimeInterval = 8   // секунд на каждое направление
    static let warmup: TimeInterval = 2          // первые секунды не учитываем (разгон TCP)

    static let gaugeMax: Double = 1500           // шкала спидометра, Мбит/с
    static let fiveGPlusThreshold: Double = 500  // порог 5G+, Мбит/с
}

struct ThroughputResult {
    let mbps: Double
    let bytes: Int64
}

/// Замер пропускной способности: несколько параллельных потоков, новый запрос открывается по завершении предыдущего.
final class ThroughputEngine: NSObject, URLSessionDataDelegate {
    enum Direction {
        case download
        case upload
    }

    private let lock = NSLock()
    private var transferred: Int64 = 0
    private var activeStreams = 0
    private var stopped = false
    private var direction: Direction = .download
    private var session: URLSession?
    private let payload = Data(count: SpeedConfig.uploadChunkBytes)
    private let delegateQueue: OperationQueue = {
        let queue = OperationQueue()
        queue.maxConcurrentOperationCount = 1
        return queue
    }()

    /// Запускает замер на заданное время и возвращает среднюю скорость после прогрева.
    func measure(_ direction: Direction,
                 duration: TimeInterval,
                 onProgress: @escaping (Double) -> Void) async -> ThroughputResult {
        self.direction = direction

        let config = URLSessionConfiguration.ephemeral
        config.httpMaximumConnectionsPerHost = SpeedConfig.streams + 2
        config.requestCachePolicy = .reloadIgnoringLocalCacheData
        config.timeoutIntervalForRequest = 15
        let newSession = URLSession(configuration: config, delegate: self, delegateQueue: delegateQueue)

        lock.lock()
        session = newSession
        stopped = false
        activeStreams = 0
        transferred = 0
        lock.unlock()

        for _ in 0..<SpeedConfig.streams {
            startStream()
        }

        var samples: [(time: TimeInterval, bytes: Int64)] = []
        let start = Date()

        while !Task.isCancelled {
            let elapsed = Date().timeIntervalSince(start)
            if elapsed >= duration { break }

            try? await Task.sleep(nanoseconds: 200_000_000)

            let now = Date().timeIntervalSince(start)
            let bytes = currentTotal()
            samples.append((now, bytes))

            // Мгновенная скорость за последнюю секунду для анимации спидометра
            if let base = samples.first(where: { now - $0.time <= 1.0 }), now > base.time {
                onProgress(Double(bytes - base.bytes) * 8 / (now - base.time) / 1_000_000)
            }

            if currentActive() == 0 { break }
        }

        let end = Date().timeIntervalSince(start)
        let endBytes = currentTotal()
        shutdown()

        guard let warm = samples.first(where: { $0.time >= SpeedConfig.warmup }), end > warm.time else {
            let seconds = max(end, 0.001)
            return ThroughputResult(mbps: Double(endBytes) * 8 / seconds / 1_000_000, bytes: endBytes)
        }

        let mbps = Double(endBytes - warm.bytes) * 8 / (end - warm.time) / 1_000_000
        return ThroughputResult(mbps: max(0, mbps), bytes: endBytes)
    }

    /// Останавливает замер (например, при нажатии «Стоп»).
    func stop() {
        shutdown()
    }

    // MARK: - Потоки

    private func startStream() {
        lock.lock()
        let current = session
        let isStopped = stopped
        lock.unlock()
        guard let current, !isStopped else { return }

        let task: URLSessionTask
        switch direction {
        case .download:
            guard let url = URL(string: SpeedConfig.downloadEndpoint + String(SpeedConfig.downloadChunkBytes)) else { return }
            task = current.dataTask(with: url)
        case .upload:
            guard let url = URL(string: SpeedConfig.uploadEndpoint) else { return }
            var request = URLRequest(url: url)
            request.httpMethod = "POST"
            task = current.uploadTask(with: request, from: payload)
        }

        lock.lock()
        activeStreams += 1
        lock.unlock()
        task.resume()
    }

    private func shutdown() {
        lock.lock()
        stopped = true
        let current = session
        session = nil
        lock.unlock()
        current?.invalidateAndCancel()
    }

    // MARK: - Делегат URLSession

    func urlSession(_ session: URLSession, dataTask: URLSessionDataTask, didReceive data: Data) {
        guard direction == .download else { return }
        addBytes(Int64(data.count))
    }

    func urlSession(_ session: URLSession,
                    task: URLSessionTask,
                    didSendBodyData bytesSent: Int64,
                    totalBytesSent: Int64,
                    totalBytesExpectedToSend: Int64) {
        guard direction == .upload else { return }
        addBytes(bytesSent)
    }

    func urlSession(_ session: URLSession, task: URLSessionTask, didCompleteWithError error: Error?) {
        lock.lock()
        activeStreams -= 1
        let shouldRestart = !stopped && error == nil
        lock.unlock()

        if shouldRestart {
            startStream()
        }
    }

    // MARK: - Счётчики

    private func addBytes(_ count: Int64) {
        lock.lock()
        transferred += count
        lock.unlock()
    }

    private func currentTotal() -> Int64 {
        lock.lock()
        defer { lock.unlock() }
        return transferred
    }

    private func currentActive() -> Int {
        lock.lock()
        defer { lock.unlock() }
        return activeStreams
    }
}
