import CoreTelephony

/// Поколение сети, которое видит приложение.
enum NetworkGeneration: String, Codable, Equatable {
    case offline
    case wifi
    case g2
    case g3
    case g4
    case g5
    case g5plus
    case unknown

    /// Определяет поколение по строке RAT из CoreTelephony.
    static func from(rat: String?) -> NetworkGeneration {
        guard let rat else { return .unknown }
        switch rat {
        case CTRadioAccessTechnologyGPRS, CTRadioAccessTechnologyEdge, CTRadioAccessTechnologyCDMA1x:
            return .g2
        case CTRadioAccessTechnologyWCDMA, CTRadioAccessTechnologyHSDPA, CTRadioAccessTechnologyHSUPA,
             CTRadioAccessTechnologyCDMAEVDORev0, CTRadioAccessTechnologyCDMAEVDORevA, CTRadioAccessTechnologyCDMAEVDORevB:
            return .g3
        case CTRadioAccessTechnologyLTE:
            return .g4
        case CTRadioAccessTechnologyNR, CTRadioAccessTechnologyNRNSA:
            return .g5
        default:
            return .unknown
        }
    }

    /// iOS не отдаёт значок 5G+ напрямую, поэтому 5G+ определяем по скорости последнего теста.
    static func resolve(rat: String?, downloadMbps: Double?) -> NetworkGeneration {
        let base = from(rat: rat)
        if base == .g5, let speed = downloadMbps, speed >= SpeedConfig.fiveGPlusThreshold {
            return .g5plus
        }
        return base
    }
}
