import Foundation
import Network
import CoreTelephony

struct SimSlot: Identifiable, Equatable {
    let id: String
    let index: Int
    let carrier: String?
    let country: String?
    let mccmnc: String?
    let rat: String?
    let isDataSlot: Bool

    var generation: NetworkGeneration { NetworkGeneration.from(rat: rat) }
}

enum ActiveInterface: Equatable {
    case offline
    case wifi
    case cellular
    case ethernet
}

enum CellularDataState: Equatable {
    case allowed
    case denied
    case unknown
}

/// Читает данные о сети: технологию, операторов, SIM, активный интерфейс и IP.
final class CellularMonitor: ObservableObject {
    static let shared = CellularMonitor()

    @Published private(set) var sims: [SimSlot] = []
    @Published private(set) var activeInterface: ActiveInterface = .offline
    @Published private(set) var isExpensive = false
    @Published private(set) var isLowDataMode = false
    @Published private(set) var cellularData: CellularDataState = .unknown
    @Published private(set) var cellularIP: String?
    @Published private(set) var wifiIP: String?
    @Published private(set) var publicIP: String?
    @Published private(set) var isFetchingPublicIP = false

    /// SIM, которая используется для мобильных данных (или первая доступная).
    var primarySim: SimSlot? {
        sims.first(where: { $0.isDataSlot }) ?? sims.first
    }

    private let telephony = CTTelephonyNetworkInfo()
    private let cellularDataInfo = CTCellularData()
    private let pathMonitor = NWPathMonitor()
    private let pathQueue = DispatchQueue(label: "ec.path")
    private var timer: Timer?
    private var started = false

    private init() {}

    func start() {
        guard !started else { return }
        started = true

        pathMonitor.pathUpdateHandler = { [weak self] path in
            let iface: ActiveInterface
            if path.status != .satisfied {
                iface = .offline
            } else if path.usesInterfaceType(.wifi) {
                iface = .wifi
            } else if path.usesInterfaceType(.cellular) {
                iface = .cellular
            } else if path.usesInterfaceType(.wiredEthernet) {
                iface = .ethernet
            } else {
                iface = .offline
            }
            let expensive = path.isExpensive
            let constrained = path.isConstrained
            DispatchQueue.main.async {
                self?.activeInterface = iface
                self?.isExpensive = expensive
                self?.isLowDataMode = constrained
            }
        }
        pathMonitor.start(queue: pathQueue)

        refresh()
        timer = Timer.scheduledTimer(withTimeInterval: 2, repeats: true) { [weak self] _ in
            self?.refresh()
        }
    }

    /// Обновляет данные о SIM, мобильных данных и IP. Вызывается по таймеру и при pull-to-refresh.
    func refresh() {
        let newSims = readSims()
        if newSims != sims { sims = newSims }

        let newState = CellularMonitor.cellularState(cellularDataInfo.restrictedState)
        if newState != cellularData { cellularData = newState }

        let newCell = CellularMonitor.address(prefix: "pdp_ip")
        let newWifi = CellularMonitor.address(exact: "en0")
        if newCell != cellularIP { cellularIP = newCell }
        if newWifi != wifiIP { wifiIP = newWifi }
    }

    /// Внешний IP-адрес через публичный сервис (нужен интернет).
    func fetchPublicIP() {
        guard !isFetchingPublicIP, let url = URL(string: "https://api.ipify.org") else { return }
        isFetchingPublicIP = true
        URLSession.shared.dataTask(with: url) { [weak self] data, _, _ in
            let ip = data.flatMap { String(data: $0, encoding: .utf8) }?.trimmingCharacters(in: .whitespacesAndNewlines)
            DispatchQueue.main.async {
                self?.isFetchingPublicIP = false
                if let ip, !ip.isEmpty { self?.publicIP = ip }
            }
        }.resume()
    }

    // MARK: - Внутреннее

    private func readSims() -> [SimSlot] {
        let rats = telephony.serviceCurrentRadioAccessTechnology ?? [:]
        let providers = telephony.serviceSubscriberCellularProviders ?? [:]
        let dataId = telephony.dataServiceIdentifier
        let keys = Set(rats.keys).union(providers.keys).sorted()

        var result: [SimSlot] = []
        for (offset, key) in keys.enumerated() {
            let carrier = providers[key]
            var mccmnc: String?
            if let mcc = CellularMonitor.clean(carrier?.mobileCountryCode),
               let mnc = CellularMonitor.clean(carrier?.mobileNetworkCode) {
                mccmnc = "\(mcc)-\(mnc)"
            }
            result.append(SimSlot(
                id: key,
                index: offset + 1,
                carrier: CellularMonitor.clean(carrier?.carrierName),
                country: CellularMonitor.clean(carrier?.isoCountryCode),
                mccmnc: mccmnc,
                rat: rats[key],
                isDataSlot: key == dataId
            ))
        }
        return result
    }

    /// iOS 16+ часто возвращает пустые или placeholder-значения («--») — такие считаем отсутствующими.
    private static func clean(_ value: String?) -> String? {
        guard let text = value?.trimmingCharacters(in: .whitespaces),
              !text.isEmpty, text != "--" else { return nil }
        return text
    }

    private static func cellularState(_ state: CTCellularDataRestrictedState) -> CellularDataState {
        switch state {
        case .notRestricted: return .allowed
        case .restricted: return .denied
        default: return .unknown
        }
    }

    /// IPv4-адрес интерфейса: по точному имени (en0) или по префиксу (pdp_ip0 для сотовой сети).
    private static func address(exact name: String? = nil, prefix: String? = nil) -> String? {
        var head: UnsafeMutablePointer<ifaddrs>?
        guard getifaddrs(&head) == 0, let first = head else { return nil }
        defer { freeifaddrs(head) }

        var cursor: UnsafeMutablePointer<ifaddrs>? = first
        while let node = cursor {
            cursor = node.pointee.ifa_next
            guard let addr = node.pointee.ifa_addr,
                  addr.pointee.sa_family == UInt8(AF_INET) else { continue }

            let ifName = String(cString: node.pointee.ifa_name)
            let matches = ifName == name || (prefix.map { ifName.hasPrefix($0) } ?? false)
            guard matches else { continue }

            var host = [CChar](repeating: 0, count: Int(NI_MAXHOST))
            let status = getnameinfo(addr, socklen_t(addr.pointee.sa_len),
                                     &host, socklen_t(host.count), nil, 0, NI_NUMERICHOST)
            if status == 0 {
                return String(cString: host)
            }
        }
        return nil
    }
}
