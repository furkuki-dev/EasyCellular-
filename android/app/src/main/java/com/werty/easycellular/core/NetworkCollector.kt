package com.werty.easycellular.core

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoNr
import android.telephony.CellInfoWcdma
import android.telephony.ServiceState
import android.telephony.SignalStrength
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyDisplayInfo
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import java.net.Inet4Address

enum class Gen { NONE, WIFI, G2, G3, G4, G5, G5_PLUS, UNKNOWN }

data class CellItem(
    val tech: String,
    val registered: Boolean,
    val bands: List<Int>,
    val pci: Int?,
    val channel: Int?,
    val cellId: Long?,
    val tac: Int?,
    val bandwidthKhz: Int?,
    val dbm: Int?,
    val rsrp: Int?,
    val rsrq: Int?,
    val sinr: Int?
)

data class SimItem(
    val slot: Int,
    val carrier: String?,
    val mccmnc: String?,
    val country: String?,
    val embedded: Boolean
)

data class NetworkSnapshot(
    val gen: Gen = Gen.UNKNOWN,
    val inService: Boolean = false,
    val carrier: String? = null,
    val mccmnc: String? = null,
    val country: String? = null,
    val roaming: Boolean = false,
    val sims: List<SimItem> = emptyList(),
    val cells: List<CellItem> = emptyList(),
    val transport: String = "none",
    val validated: Boolean = false,
    val metered: Boolean = false,
    val downKbps: Int? = null,
    val upKbps: Int? = null,
    val iface: String? = null,
    val ipv4: List<String> = emptyList(),
    val dns: List<String> = emptyList(),
    val mtu: Int? = null,
    val signalLevel: Int? = null,
    val updatedAt: Long = 0L
) {
    val primaryCell: CellItem? get() = cells.firstOrNull { it.registered } ?: cells.firstOrNull()
}

private inline fun <T> safe(default: T, block: () -> T): T =
    try { block() } catch (e: SecurityException) { default } catch (e: IllegalStateException) { default }

private fun Int.validInt(): Int? = if (this == CellInfo.UNAVAILABLE) null else this
private fun Long.validLong(): Long? = if (this == CellInfo.UNAVAILABLE_LONG) null else this

class NetworkCollector(private val context: Context) {

    private val tm = context.getSystemService(TelephonyManager::class.java)
    private val sm = context.getSystemService(SubscriptionManager::class.java)
    private val cm = context.getSystemService(ConnectivityManager::class.java)

    @Volatile private var displayInfo: TelephonyDisplayInfo? = null
    @Volatile private var signal: SignalStrength? = null
    private var callback: TelephonyCallback? = null

    fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    fun missingPermissions(): List<String> = REQUIRED.filterNot { hasPermission(it) }

    fun activeNetwork(): Network? = cm.activeNetwork

    /** Подписка на события телефонии: тип сети с признаком 5G+ и уровень сигнала приходят сюда. */
    @SuppressLint("MissingPermission")
    fun startListening() {
        if (callback != null || !hasPermission(Manifest.permission.READ_PHONE_STATE)) return
        val cb = object : TelephonyCallback(),
            TelephonyCallback.DisplayInfoListener,
            TelephonyCallback.SignalStrengthsListener {
            override fun onDisplayInfoChanged(info: TelephonyDisplayInfo) {
                displayInfo = info
            }
            override fun onSignalStrengthsChanged(strength: SignalStrength) {
                signal = strength
            }
        }
        try {
            tm.registerTelephonyCallback(ContextCompat.getMainExecutor(context), cb)
            callback = cb
        } catch (e: SecurityException) {
            callback = null
        }
    }

    fun stopListening() {
        callback?.let { tm.unregisterTelephonyCallback(it) }
        callback = null
    }

    @SuppressLint("MissingPermission")
    fun collect(): NetworkSnapshot {
        val net = cm.activeNetwork
        val caps = net?.let { cm.getNetworkCapabilities(it) }
        val props = net?.let { cm.getLinkProperties(it) }
        val canPhone = hasPermission(Manifest.permission.READ_PHONE_STATE)
        val canLocation = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

        val transport = when {
            caps == null -> "none"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "vpn"
            else -> "other"
        }

        val di = displayInfo
        val override = di?.overrideNetworkType ?: TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NONE
        val rawType = safe(TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            if (canPhone) tm.dataNetworkType else TelephonyManager.NETWORK_TYPE_UNKNOWN
        }
        val type = if (rawType != TelephonyManager.NETWORK_TYPE_UNKNOWN) rawType else di?.networkType ?: rawType

        val cellGen = when {
            override == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_ADVANCED -> Gen.G5_PLUS
            override == TelephonyDisplayInfo.OVERRIDE_NETWORK_TYPE_NR_NSA -> Gen.G5
            else -> genOf(type)
        }
        val gen = when {
            caps == null -> Gen.NONE
            cellGen == Gen.UNKNOWN && transport == "wifi" -> Gen.WIFI
            else -> cellGen
        }

        val cells = if (canLocation) readCells() else emptyList()
        val sims = if (canPhone) readSims() else emptyList()

        return NetworkSnapshot(
            gen = gen,
            inService = safe(false) { tm.serviceState?.state == ServiceState.STATE_IN_SERVICE },
            carrier = safe<String?>(null) { tm.networkOperatorName.takeIf { it.isNotBlank() } },
            mccmnc = safe<String?>(null) { tm.networkOperator.takeIf { it.isNotBlank() } },
            country = safe<String?>(null) { tm.networkCountryIso.takeIf { it.isNotBlank() }?.uppercase() },
            roaming = safe(false) { tm.isNetworkRoaming },
            sims = sims,
            cells = cells,
            transport = transport,
            validated = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false,
            metered = caps?.let { !it.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED) } ?: false,
            downKbps = caps?.linkDownstreamBandwidthKbps,
            upKbps = caps?.linkUpstreamBandwidthKbps,
            iface = props?.interfaceName,
            ipv4 = props?.linkAddresses?.map { it.address }?.filterIsInstance<Inet4Address>()
                ?.mapNotNull { it.hostAddress }.orEmpty(),
            dns = props?.dnsServers?.mapNotNull { it.hostAddress }.orEmpty(),
            mtu = props?.mtu?.takeIf { it > 0 },
            signalLevel = signal?.level,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun genOf(type: Int): Gen = when (type) {
        TelephonyManager.NETWORK_TYPE_GPRS,
        TelephonyManager.NETWORK_TYPE_EDGE,
        TelephonyManager.NETWORK_TYPE_CDMA,
        TelephonyManager.NETWORK_TYPE_1xRTT,
        TelephonyManager.NETWORK_TYPE_IDEN -> Gen.G2
        TelephonyManager.NETWORK_TYPE_UMTS,
        TelephonyManager.NETWORK_TYPE_EVDO_0,
        TelephonyManager.NETWORK_TYPE_EVDO_A,
        TelephonyManager.NETWORK_TYPE_EVDO_B,
        TelephonyManager.NETWORK_TYPE_EHRPD,
        TelephonyManager.NETWORK_TYPE_HSDPA,
        TelephonyManager.NETWORK_TYPE_HSUPA,
        TelephonyManager.NETWORK_TYPE_HSPA,
        TelephonyManager.NETWORK_TYPE_HSPAP,
        TelephonyManager.NETWORK_TYPE_TD_SCDMA -> Gen.G3
        TelephonyManager.NETWORK_TYPE_LTE -> Gen.G4
        TelephonyManager.NETWORK_TYPE_NR -> Gen.G5
        else -> Gen.UNKNOWN
    }

    @SuppressLint("MissingPermission")
    private fun readCells(): List<CellItem> = safe(emptyList()) {
        tm.allCellInfo.orEmpty().mapNotNull { toItem(it) }
    }

    private fun toItem(c: CellInfo): CellItem? = when (c) {
        is CellInfoLte -> {
            val id = c.cellIdentity
            val ss = c.cellSignalStrength
            CellItem(
                tech = "LTE",
                registered = c.isRegistered,
                bands = id.bands.toList(),
                pci = id.pci.validInt(),
                channel = id.earfcn.validInt(),
                cellId = id.ci.validInt()?.toLong(),
                tac = id.tac.validInt(),
                bandwidthKhz = id.bandwidth.validInt(),
                dbm = ss.dbm.validInt(),
                rsrp = ss.rsrp.validInt(),
                rsrq = ss.rsrq.validInt(),
                sinr = ss.rssnr.validInt()
            )
        }
        is CellInfoNr -> {
            // Read NR-only fields through their concrete Android telephony types.
            val id = c.cellIdentity as? android.telephony.CellIdentityNr ?: return null
            val ss = c.cellSignalStrength as? android.telephony.CellSignalStrengthNr ?: return null
            CellItem(
                tech = "NR",
                registered = c.isRegistered,
                bands = id.bands.toList(),
                pci = id.pci.validInt(),
                channel = id.nrarfcn.validInt(),
                cellId = id.nci.validLong(),
                tac = id.tac.validInt(),
                bandwidthKhz = null,
                dbm = ss.dbm.validInt(),
                rsrp = ss.ssRsrp.validInt(),
                rsrq = ss.ssRsrq.validInt(),
                sinr = ss.ssSinr.validInt()
            )
        }
        is CellInfoWcdma -> CellItem(
            tech = "WCDMA",
            registered = c.isRegistered,
            bands = emptyList(),
            pci = c.cellIdentity.psc.validInt(),
            channel = c.cellIdentity.uarfcn.validInt(),
            cellId = c.cellIdentity.cid.validInt()?.toLong(),
            tac = c.cellIdentity.lac.validInt(),
            bandwidthKhz = null,
            dbm = c.cellSignalStrength.dbm.validInt(),
            rsrp = null, rsrq = null, sinr = null
        )
        is CellInfoGsm -> CellItem(
            tech = "GSM",
            registered = c.isRegistered,
            bands = emptyList(),
            pci = null,
            channel = c.cellIdentity.arfcn.validInt(),
            cellId = c.cellIdentity.cid.validInt()?.toLong(),
            tac = c.cellIdentity.lac.validInt(),
            bandwidthKhz = null,
            dbm = c.cellSignalStrength.dbm.validInt(),
            rsrp = null, rsrq = null, sinr = null
        )
        else -> null
    }

    @SuppressLint("MissingPermission")
    private fun readSims(): List<SimItem> = safe(emptyList()) {
        sm.activeSubscriptionInfoList.orEmpty().map { s ->
            SimItem(
                slot = s.simSlotIndex + 1,
                carrier = s.carrierName?.toString(),
                mccmnc = if (s.mccString != null && s.mncString != null) "${s.mccString}-${s.mncString}" else null,
                country = s.countryIso?.uppercase(),
                embedded = s.isEmbedded
            )
        }
    }

    companion object {
        val REQUIRED: Array<String> = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }
}
