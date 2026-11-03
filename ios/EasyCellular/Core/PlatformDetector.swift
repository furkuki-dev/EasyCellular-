import Foundation
import UIKit

/// Семейство ОС. Общая модель с Android-версией приложения.
enum OSFamily: String {
    case ios = "iOS"
    case android = "Android"
    case other = "Other"
}

struct DeviceOSInfo {
    let family: OSFamily
    let name: String        // "iOS"
    let version: String     // "18.4"
    let model: String       // "iPhone16,1"
    let isSimulator: Bool
}

/// Определитель платформы для iOS-версии EasyCellular.
/// Положите файл в папку EasyCellular/Core/ iOS-проекта.
enum PlatformDetector {

    /// Текущая ОС: в iOS-приложении это всегда iOS.
    static var currentFamily: OSFamily {
        #if os(iOS)
        return .ios
        #else
        return .other
        #endif
    }

    static func detect() -> DeviceOSInfo {
        let device = UIDevice.current
        return DeviceOSInfo(
            family: currentFamily,
            name: device.systemName,
            version: device.systemVersion,
            model: machineIdentifier(),
            isSimulator: isSimulatorBuild
        )
    }

    /// Строка для экрана «О приложении»: «iOS 18.4 · iPhone16,1».
    static func describe() -> String {
        let info = detect()
        let suffix = info.isSimulator ? " (Simulator)" : ""
        return "\(info.name) \(info.version) · \(info.model)\(suffix)"
    }

    /// Идентификатор модели, например iPhone16,1.
    static func machineIdentifier() -> String {
        var systemInfo = utsname()
        uname(&systemInfo)
        let mirror = Mirror(reflecting: systemInfo.machine)
        return mirror.children.reduce(into: "") { result, element in
            if let value = element.value as? Int8, value != 0 {
                result.append(Character(UnicodeScalar(UInt8(value))))
            }
        }
    }

    private static var isSimulatorBuild: Bool {
        #if targetEnvironment(simulator)
        return true
        #else
        return false
        #endif
    }
}
