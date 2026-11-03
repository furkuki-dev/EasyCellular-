import SwiftUI
import Charts

struct HistoryView: View {
    @ObservedObject private var settings = AppSettings.shared
    @ObservedObject private var history = HistoryStore.shared
    @State private var confirmClear = false

    var body: some View {
        NavigationStack {
            ZStack {
                AppBackground()
                if history.records.isEmpty {
                    emptyState
                } else {
                    List {
                        if history.records.count > 1 {
                            Section {
                                chart
                            } header: {
                                Text(settings.t("hist_chart"))
                            }
                        }
                        Section {
                            ForEach(history.records) { record in
                                row(record)
                                    .listRowBackground(Color.white.opacity(0.06))
                            }
                            .onDelete { history.delete(at: $0) }
                        }
                    }
                    .scrollContentBackground(.hidden)
                }
            }
            .navigationTitle(settings.t("tab_history"))
            .toolbar {
                if !history.records.isEmpty {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button(settings.t("hist_clear")) {
                            confirmClear = true
                        }
                    }
                }
            }
            .confirmationDialog(settings.t("hist_clear_confirm"),
                                isPresented: $confirmClear,
                                titleVisibility: .visible) {
                Button(settings.t("hist_clear"), role: .destructive) {
                    history.clear()
                }
                Button(settings.t("common_cancel"), role: .cancel) { }
            }
        }
    }

    private var chart: some View {
        let points = Array(history.records.prefix(20).reversed())
        return Chart(points) { record in
            LineMark(x: .value("Date", record.date), y: .value("Mbps", record.download))
                .foregroundStyle(Theme.accent)
                .interpolationMethod(.catmullRom)
            PointMark(x: .value("Date", record.date), y: .value("Mbps", record.download))
                .foregroundStyle(Theme.accent2)
        }
        .frame(height: 160)
        .padding(.vertical, 4)
    }

    private func row(_ r: SpeedRecord) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            HStack {
                Text(r.date, format: .dateTime.day().month().hour().minute())
                    .font(.subheadline.weight(.semibold))
                    .foregroundColor(.white)
                Spacer()
                Text(r.generation.title(settings))
                    .font(.caption.weight(.bold))
                    .foregroundColor(Theme.accent)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Capsule().fill(Theme.accent.opacity(0.2)))
            }
            HStack(spacing: 14) {
                Label(settings.f("fmt_mbps", r.download), systemImage: "arrow.down")
                Label(settings.f("fmt_mbps", r.upload), systemImage: "arrow.up")
                if let ping = r.ping {
                    Label(settings.f("fmt_ms", ping), systemImage: "waveform.path.ecg")
                }
            }
            .font(.caption)
            .foregroundColor(.white.opacity(0.7))
        }
        .padding(.vertical, 4)
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            Image(systemName: "chart.line.uptrend.xyaxis")
                .font(.system(size: 52))
                .foregroundColor(Theme.accent)
            Text(settings.t("hist_empty"))
                .font(.title3.weight(.semibold))
                .foregroundColor(.white)
            Text(settings.t("hist_empty_hint"))
                .font(.subheadline)
                .foregroundColor(.white.opacity(0.6))
                .multilineTextAlignment(.center)
        }
        .padding()
    }
}
