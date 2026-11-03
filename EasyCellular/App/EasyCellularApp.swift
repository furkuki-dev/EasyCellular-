import SwiftUI
import UIKit

@main
struct EasyCellularApp: App {
    init() {
        // Прозрачная навигационная панель поверх градиента
        let appearance = UINavigationBarAppearance()
        appearance.configureWithTransparentBackground()
        appearance.titleTextAttributes = [.foregroundColor: UIColor.white]
        appearance.largeTitleTextAttributes = [.foregroundColor: UIColor.white]
        UINavigationBar.appearance().standardAppearance = appearance
        UINavigationBar.appearance().scrollEdgeAppearance = appearance
        UINavigationBar.appearance().compactAppearance = appearance
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .preferredColorScheme(.dark)
        }
    }
}

struct RootView: View {
    @ObservedObject private var settings = AppSettings.shared

    var body: some View {
        TabView {
            NetworkView()
                .tabItem {
                    Label(settings.t("tab_network"), systemImage: "antenna.radiowaves.left.and.right")
                }
            SpeedTestView()
                .tabItem {
                    Label(settings.t("tab_test"), systemImage: "speedometer")
                }
            HistoryView()
                .tabItem {
                    Label(settings.t("tab_history"), systemImage: "clock.arrow.circlepath")
                }
            SettingsView()
                .tabItem {
                    Label(settings.t("tab_settings"), systemImage: "gearshape")
                }
        }
        .tint(Theme.accent)
        .onAppear { CellularMonitor.shared.start() }
    }
}
