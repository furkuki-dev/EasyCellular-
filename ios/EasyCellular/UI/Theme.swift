import SwiftUI

enum Theme {
    static let bgTop = Color(red: 0.03, green: 0.05, blue: 0.12)
    static let bgBottom = Color(red: 0.06, green: 0.09, blue: 0.20)
    static let accent = Color(red: 0.35, green: 0.85, blue: 1.00)
    static let accent2 = Color(red: 0.60, green: 0.45, blue: 1.00)
    static let good = Color(red: 0.30, green: 0.90, blue: 0.60)
    static let warn = Color(red: 1.00, green: 0.75, blue: 0.30)
    static let bad = Color(red: 1.00, green: 0.40, blue: 0.45)
    static let stroke = Color.white.opacity(0.10)
}

extension NetworkGeneration {
    func title(_ settings: AppSettings) -> String {
        switch self {
        case .offline: return settings.t("gen_offline")
        case .wifi: return "Wi-Fi"
        case .g2: return "2G"
        case .g3: return "3G"
        case .g4: return "4G"
        case .g5: return "5G"
        case .g5plus: return "5G+"
        case .unknown: return settings.t("gen_unknown")
        }
    }
}

struct AppBackground: View {
    var body: some View {
        LinearGradient(colors: [Theme.bgTop, Theme.bgBottom], startPoint: .top, endPoint: .bottom)
            .ignoresSafeArea()
    }
}

struct GlassCard<Content: View>: View {
    private let content: Content

    init(@ViewBuilder content: () -> Content) {
        self.content = content()
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            content
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .fill(.ultraThinMaterial)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 22, style: .continuous)
                .stroke(Theme.stroke, lineWidth: 1)
        )
    }
}

struct SectionTitle: View {
    let text: String

    init(_ text: String) {
        self.text = text
    }

    var body: some View {
        Text(text)
            .font(.headline)
            .foregroundColor(.white)
    }
}

struct InfoRow: View {
    let title: String
    let value: String
    var valueColor: Color = .white

    var body: some View {
        HStack(alignment: .firstTextBaseline) {
            Text(title)
                .foregroundColor(.white.opacity(0.6))
            Spacer(minLength: 12)
            Text(value)
                .fontWeight(.semibold)
                .foregroundColor(valueColor)
                .multilineTextAlignment(.trailing)
        }
        .font(.subheadline)
    }
}

struct MetricTile: View {
    let title: String
    let value: String
    let unit: String
    let tint: Color

    var body: some View {
        VStack(spacing: 6) {
            Text(title)
                .font(.caption)
                .foregroundColor(.white.opacity(0.6))
            Text(value)
                .font(.system(size: 24, weight: .bold, design: .rounded))
                .monospacedDigit()
                .foregroundColor(tint)
                .contentTransition(.numericText())
                .lineLimit(1)
                .minimumScaleFactor(0.6)
            Text(unit)
                .font(.caption2)
                .foregroundColor(.white.opacity(0.4))
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 14)
        .background(
            RoundedRectangle(cornerRadius: 18, style: .continuous)
                .fill(.ultraThinMaterial)
        )
    }
}
