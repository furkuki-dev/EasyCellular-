import SwiftUI

struct NetworkView: View {
    @ObservedObject private var settings = AppSettings.shared
    @ObservedObject private var monitor = CellularMonitor.shared
    @ObservedObject private var history = HistoryStore.shared

    var body: some View {
        NavigationStack {
            ZStack {
                AppBackground()
                ScrollView {
                    VStack(spacing: 16) {
                        heroCard
                        carrierCard
                        connectionCard
                        if monitor.sims.count > 1 {
                            simsCard
                        }
                        ipCard
                        lastResultCard

                        Text(settings.f("net_footnote", SpeedConfig.fiveGPlusThreshold))
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.45))
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                    }
                    .padding()
                }
                .refreshable {
                    monitor.refresh()
                    try? await Task.sleep(nanoseconds: 400_000_000)
                }
            }
            .navigationTitle(settings.t("net_title"))
        }
    }

    // MARK: - Карточки

    private var heroCard: some View {
        let generation = heroGeneration
        return GlassCard {
            VStack(spacing: 6) {
                Text(generation.title(settings))
                    .font(.system(size: 72, weight: .heavy, design: .rounded))
                    .lineLimit(1)
                    .minimumScaleFactor(0.4)
                    .foregroundStyle(LinearGradient(colors: [Theme.accent, Theme.accent2],
                                                    startPoint: .leading,
                                                    endPoint: .trailing))
                    .animation(.easeInOut(duration: 0.4), value: generation)
                Text(statusText)
                    .font(.subheadline.weight(.medium))
                    .foregroundColor(.white.opacity(0.7))
            }
            .frame(maxWidth: .infinity)
        }
    }

    private var carrierCard: some View {
        let sim = monitor.primarySim
        return GlassCard {
            SectionTitle(settings.t("card_carrier"))
            InfoRow(title: settings.t("net_carrier"),
                    value: sim?.carrier ?? settings.t("net_carrier_unknown"))
            InfoRow(title: settings.t("net_tech"), value: technologyName(sim?.rat))
            InfoRow(title: settings.t("net_country"), value: sim?.country?.uppercased() ?? "—")
            InfoRow(title: settings.t("net_mccmnc"), value: sim?.mccmnc ?? "—")
            InfoRow(title: settings.t("net_cellular_data"),
                    value: cellularDataText,
                    valueColor: cellularDataColor)
        }
    }

    private var connectionCard: some View {
        GlassCard {
            SectionTitle(settings.t("card_connection"))
            InfoRow(title: settings.t("net_active_iface"), value: interfaceText)
            InfoRow(title: settings.t("net_expensive"), value: yesNo(monitor.isExpensive))
            InfoRow(title: settings.t("net_low_data"), value: yesNo(monitor.isLowDataMode))
        }
    }

    private var simsCard: some View {
        GlassCard {
            SectionTitle(settings.t("card_sims"))
            ForEach(monitor.sims) { sim in
                HStack(spacing: 12) {
                    Image(systemName: sim.isDataSlot ? "simcard.fill" : "simcard")
                        .foregroundColor(Theme.accent)
                    VStack(alignment: .leading, spacing: 2) {
                        Text(settings.f("sim_label", sim.index))
                            .font(.subheadline.weight(.semibold))
                            .foregroundColor(.white)
                        Text(sim.carrier ?? settings.t("net_carrier_unknown"))
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.6))
                    }
                    Spacer()
                    Text(sim.generation.title(settings))
                        .font(.headline)
                        .foregroundColor(Theme.accent)
                }
            }
        }
    }

    private var ipCard: some View {
        GlassCard {
            SectionTitle(settings.t("card_ip"))
            InfoRow(title: settings.t("net_ip_cell"), value: monitor.cellularIP ?? "—")
            InfoRow(title: settings.t("net_ip_wifi"), value: monitor.wifiIP ?? "—")
            InfoRow(title: settings.t("net_ip_public"), value: monitor.publicIP ?? "—")
            Button {
                monitor.fetchPublicIP()
            } label: {
                HStack(spacing: 8) {
                    if monitor.isFetchingPublicIP {
                        ProgressView()
                    }
                    Text(settings.t("net_get_public"))
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.bordered)
            .tint(Theme.accent)
            .disabled(monitor.isFetchingPublicIP)
        }
    }

    private var lastResultCard: some View {
        GlassCard {
            SectionTitle(settings.t("net_last_result"))
            if let last = history.records.first {
                InfoRow(title: settings.t("test_ping"),
                        value: last.ping.map { settings.f("fmt_ms", $0) } ?? "—")
                InfoRow(title: settings.t("test_download"), value: settings.f("fmt_mbps", last.download))
                InfoRow(title: settings.t("test_upload"), value: settings.f("fmt_mbps", last.upload))
            } else {
                Text(settings.t("net_no_results"))
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.6))
            }
        }
    }

    // MARK: - Вычисляемые значения

    /// Поколение для главной карточки. 5G+ учитывает скорость последнего теста за 10 минут.
    private var heroGeneration: NetworkGeneration {
        if let sim = monitor.primarySim, sim.rat != nil {
            var recentSpeed: Double?
            if let last = history.records.first, Date().timeIntervalSince(last.date) < 600 {
                recentSpeed = last.download
            }
            return NetworkGeneration.resolve(rat: sim.rat, downloadMbps: recentSpeed)
        }
        switch monitor.activeInterface {
        case .wifi: return .wifi
        case .offline: return .offline
        default: return .unknown
        }
    }

    private var statusText: String {
        switch monitor.activeInterface {
        case .offline: return settings.t("net_no_connection")
        case .wifi: return settings.t("net_on_wifi")
        default: return settings.t("net_connected")
        }
    }

    private var interfaceText: String {
        switch monitor.activeInterface {
        case .wifi: return settings.t("iface_wifi")
        case .cellular: return settings.t("iface_cellular")
        case .ethernet: return settings.t("iface_ethernet")
        case .offline: return settings.t("iface_none")
        }
    }

    private var cellularDataText: String {
        switch monitor.cellularData {
        case .allowed: return settings.t("cell_allowed")
        case .denied: return settings.t("cell_denied")
        case .unknown: return settings.t("cell_unknown")
        }
    }

    private var cellularDataColor: Color {
        switch monitor.cellularData {
        case .allowed: return Theme.good
        case .denied: return Theme.bad
        case .unknown: return .white
        }
    }

    /// Убирает служебный префикс из названия технологии: CTRadioAccessTechnologyLTE → LTE.
    private func technologyName(_ rat: String?) -> String {
        guard let rat else { return "—" }
        return rat.replacingOccurrences(of: "CTRadioAccessTechnology", with: "")
    }

    private func yesNo(_ flag: Bool) -> String {
        settings.t(flag ? "common_yes" : "common_no")
    }
}
