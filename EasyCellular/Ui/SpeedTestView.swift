import SwiftUI
import UIKit

@MainActor
final class SpeedTestViewModel: ObservableObject {
    enum Phase {
        case idle
        case ping
        case download
        case upload
        case done
        case failed
    }

    @Published private(set) var phase: Phase = .idle
    @Published private(set) var liveMbps: Double = 0
    @Published private(set) var pingStats: PingStats?
    @Published private(set) var downloadMbps: Double?
    @Published private(set) var uploadMbps: Double?
    @Published private(set) var dataUsedBytes: Int64 = 0
    @Published private(set) var generation: NetworkGeneration = .unknown

    private var task: Task<Void, Never>?
    private var engine: ThroughputEngine?

    var isRunning: Bool {
        phase == .ping || phase == .download || phase == .upload
    }

    func start(host: String, rat: String?, carrier: String?, onWifi: Bool) {
        guard !isRunning else { return }
        pingStats = nil
        downloadMbps = nil
        uploadMbps = nil
        dataUsedBytes = 0
        liveMbps = 0
        task = Task { [weak self] in
            await self?.run(host: host, rat: rat, carrier: carrier, onWifi: onWifi)
        }
    }

    func cancel() {
        task?.cancel()
        engine?.stop()
    }

    private func run(host: String, rat: String?, carrier: String?, onWifi: Bool) async {
        phase = .ping
        let stats = await PingTester.run(host: host)
        pingStats = stats
        if Task.isCancelled {
            finishCancelled()
            return
        }

        phase = .download
        liveMbps = 0
        let down = await measure(.download)
        if Task.isCancelled {
            finishCancelled()
            return
        }
        guard down.bytes > 0 else {
            finishFailed()
            return
        }
        downloadMbps = down.mbps
        dataUsedBytes += down.bytes

        phase = .upload
        liveMbps = 0
        let up = await measure(.upload)
        if Task.isCancelled {
            finishCancelled()
            return
        }
        uploadMbps = up.mbps
        dataUsedBytes += up.bytes

        let gen: NetworkGeneration = onWifi
            ? .wifi
            : NetworkGeneration.resolve(rat: rat, downloadMbps: down.mbps)
        generation = gen
        liveMbps = down.mbps
        phase = .done

        HistoryStore.shared.add(SpeedRecord(
            id: UUID(),
            date: Date(),
            download: down.mbps,
            upload: up.mbps,
            ping: stats?.average,
            rat: onWifi ? nil : rat,
            carrier: carrier,
            generation: gen
        ))
        UINotificationFeedbackGenerator().notificationOccurred(.success)
    }

    private func measure(_ direction: ThroughputEngine.Direction) async -> ThroughputResult {
        let meter = ThroughputEngine()
        engine = meter
        let result = await meter.measure(direction, duration: SpeedConfig.testDuration) { [weak self] value in
            Task { @MainActor in
                self?.liveMbps = value
            }
        }
        engine = nil
        return result
    }

    private func finishCancelled() {
        phase = .idle
        liveMbps = 0
    }

    private func finishFailed() {
        phase = .failed
        liveMbps = 0
    }
}

/// Дуговой спидометр. Шкала логарифмическая, чтобы низкие скорости тоже были видны.
struct SpeedGauge: View {
    let value: Double

    private var fraction: Double {
        guard value > 0 else { return 0 }
        return min(1, log10(1 + value) / log10(1 + SpeedConfig.gaugeMax))
    }

    var body: some View {
        ZStack {
            Circle()
                .trim(from: 0, to: 0.75)
                .stroke(Color.white.opacity(0.08), style: StrokeStyle(lineWidth: 16, lineCap: .round))
                .rotationEffect(.degrees(135))

            Circle()
                .trim(from: 0, to: 0.75 * fraction)
                .stroke(
                    AngularGradient(colors: [Theme.accent, Theme.accent2, Theme.accent],
                                    center: .center,
                                    startAngle: .degrees(0),
                                    endAngle: .degrees(270)),
                    style: StrokeStyle(lineWidth: 16, lineCap: .round)
                )
                .rotationEffect(.degrees(135))
                .shadow(color: Theme.accent.opacity(0.5), radius: 10)
                .animation(.easeInOut(duration: 0.35), value: fraction)
        }
    }
}

struct SpeedTestView: View {
    @ObservedObject private var settings = AppSettings.shared
    @ObservedObject private var monitor = CellularMonitor.shared
    @StateObject private var vm = SpeedTestViewModel()
    @State private var pulse = false

    var body: some View {
        NavigationStack {
            ZStack {
                AppBackground()
                ScrollView {
                    VStack(spacing: 22) {
                        gauge

                        Text(phaseText)
                            .font(.headline)
                            .foregroundColor(.white.opacity(0.75))
                            .animation(.easeInOut, value: vm.phase)

                        metrics
                        startButton

                        if vm.phase == .done {
                            resultCard
                        }
                    }
                    .padding()
                }
            }
            .navigationTitle(settings.t("test_title"))
        }
    }

    // MARK: - Элементы

    private var gauge: some View {
        ZStack {
            Circle()
                .fill(Theme.accent.opacity(0.22))
                .blur(radius: 40)
                .scaleEffect(pulse ? 1.1 : 0.9)

            SpeedGauge(value: vm.liveMbps)

            VStack(spacing: 2) {
                Text(String(format: "%.0f", vm.liveMbps))
                    .font(.system(size: 60, weight: .bold, design: .rounded))
                    .monospacedDigit()
                    .contentTransition(.numericText())
                Text(settings.t("test_mbps"))
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.55))
            }
        }
        .frame(width: 270, height: 270)
        .onChange(of: vm.isRunning) { running in
            withAnimation(running ? .easeInOut(duration: 1.2).repeatForever(autoreverses: true) : .default) {
                pulse = running
            }
        }
    }

    private var metrics: some View {
        HStack(spacing: 12) {
            MetricTile(title: settings.t("test_ping"),
                       value: vm.pingStats.map { String(format: "%.0f", $0.average) } ?? "—",
                       unit: settings.t("test_ms"),
                       tint: Theme.warn)
            MetricTile(title: settings.t("test_download"),
                       value: vm.downloadMbps.map { String(format: "%.0f", $0) } ?? "—",
                       unit: settings.t("test_mbps"),
                       tint: Theme.accent)
            MetricTile(title: settings.t("test_upload"),
                       value: vm.uploadMbps.map { String(format: "%.0f", $0) } ?? "—",
                       unit: settings.t("test_mbps"),
                       tint: Theme.accent2)
        }
    }

    private var startButton: some View {
        Button(action: toggle) {
            Label(vm.isRunning ? settings.t("test_stop") : settings.t("test_start"),
                  systemImage: vm.isRunning ? "stop.fill" : "play.fill")
                .font(.title3.weight(.semibold))
                .foregroundColor(.white)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(
                    Capsule().fill(
                        LinearGradient(colors: vm.isRunning ? [Theme.bad, Theme.warn] : [Theme.accent, Theme.accent2],
                                       startPoint: .leading,
                                       endPoint: .trailing)
                    )
                )
        }
        .buttonStyle(.plain)
        .animation(.easeInOut(duration: 0.3), value: vm.isRunning)
    }

    private var resultCard: some View {
        GlassCard {
            SectionTitle(settings.t("test_result_title"))
            if let s = vm.pingStats {
                InfoRow(title: settings.t("test_min"), value: settings.f("fmt_ms", s.minimum))
                InfoRow(title: settings.t("test_max"), value: settings.f("fmt_ms", s.maximum))
                InfoRow(title: settings.t("test_jitter"), value: settings.f("fmt_ms", s.jitter))
                InfoRow(title: settings.t("test_loss"), value: settings.f("fmt_pct", s.lossPercent))
            }
            InfoRow(title: settings.t("test_generation"),
                    value: vm.generation.title(settings),
                    valueColor: Theme.accent)
            InfoRow(title: settings.t("test_data_used"),
                    value: settings.f("fmt_mb", Double(vm.dataUsedBytes) / 1_000_000))

            ShareLink(item: shareText) {
                Label(settings.t("test_share"), systemImage: "square.and.arrow.up")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .tint(Theme.accent)
        }
    }

    // MARK: - Логика

    private var phaseText: String {
        switch vm.phase {
        case .idle: return settings.t("test_phase_idle")
        case .ping: return settings.t("test_phase_ping")
        case .download: return settings.t("test_phase_download")
        case .upload: return settings.t("test_phase_upload")
        case .done: return settings.t("test_phase_done")
        case .failed: return settings.t("test_failed")
        }
    }

    private var shareText: String {
        let line = settings.f("share_format",
                              vm.downloadMbps ?? 0,
                              vm.uploadMbps ?? 0,
                              vm.pingStats?.average ?? 0)
        return line + " · " + vm.generation.title(settings)
    }

    private func toggle() {
        if vm.isRunning {
            vm.cancel()
        } else {
            vm.start(host: settings.safePingHost,
                     rat: monitor.primarySim?.rat,
                     carrier: monitor.primarySim?.carrier,
                     onWifi: monitor.activeInterface == .wifi)
        }
    }
}
