import SwiftUI

struct SettingsView: View {
    @ObservedObject private var settings = AppSettings.shared
    @ObservedObject private var history = HistoryStore.shared
    @State private var confirmClear = false

    private var version: String {
        Bundle.main.object(forInfoDictionaryKey: "CFBundleShortVersionString") as? String ?? "1.0"
    }

    var body: some View {
        NavigationStack {
            ZStack {
                AppBackground()
                Form {
                    Section(header: Text(settings.t("set_language")),
                            footer: Text(settings.t("set_language_footer"))) {
                        Picker(settings.t("set_language"), selection: $settings.language) {
                            ForEach(AppLanguage.allCases) { lang in
                                Text(lang.displayName).tag(lang)
                            }
                        }
                        .pickerStyle(.segmented)
                    }

                    Section(header: Text(settings.t("set_ping_host")),
                            footer: Text(settings.t("set_ping_footer"))) {
                        TextField("1.1.1.1", text: $settings.pingHost)
                            .keyboardType(.URL)
                            .textInputAutocapitalization(.never)
                            .autocorrectionDisabled()
                    }

                    Section(header: Text(settings.t("set_data"))) {
                        Button(role: .destructive) {
                            confirmClear = true
                        } label: {
                            Label(settings.t("set_clear_history"), systemImage: "trash")
                        }
                    }

                    Section(header: Text(settings.t("set_about"))) {
                        InfoRow(title: settings.t("set_version"), value: version)
                        Text(settings.t("set_about_text"))
                            .font(.footnote)
                            .foregroundColor(.white.opacity(0.6))
                    }
                }
                .scrollContentBackground(.hidden)
            }
            .navigationTitle(settings.t("tab_settings"))
            .confirmationDialog(settings.t("hist_clear_confirm"),
                                isPresented: $confirmClear,
                                titleVisibility: .visible) {
                Button(settings.t("set_clear_history"), role: .destructive) {
                    history.clear()
                }
                Button(settings.t("common_cancel"), role: .cancel) { }
            }
        }
    }
}
