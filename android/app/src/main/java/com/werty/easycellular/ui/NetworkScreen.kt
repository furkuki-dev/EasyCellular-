package com.werty.easycellular.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.werty.easycellular.core.CellItem
import com.werty.easycellular.core.Gen
import com.werty.easycellular.core.NetworkSnapshot
import com.werty.easycellular.core.SpeedConfig
import com.werty.easycellular.i18n.LocalL

@Composable
fun NetworkScreen(
    state: NetworkSnapshot,
    missing: List<String>,
    onGrant: () -> Unit,
    onSettings: () -> Unit
) {
    val t = LocalL.current
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (missing.isNotEmpty()) PermissionCard(onGrant, onSettings)
        HeroCard(state)
        SignalCard(state)
        CellCard(state)
        CarrierCard(state)
        LinkCard(state)
        CellsCard(state)
        Text(
            text = t.t("net_footnote"),
            color = Color.White.copy(alpha = 0.45f),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun PermissionCard(onGrant: () -> Unit, onSettings: () -> Unit) {
    val t = LocalL.current
    GlassCard {
        Text(t.t("perm_banner"), color = Color.White.copy(alpha = 0.85f))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onGrant) { Text(t.t("perm_grant")) }
            OutlinedButton(onClick = onSettings) { Text(t.t("perm_settings")) }
        }
    }
}

@Composable
private fun HeroCard(s: NetworkSnapshot) {
    val t = LocalL.current
    val status = when {
        s.transport == "none" -> t.t("state_offline")
        s.gen == Gen.WIFI -> t.t("state_wifi")
        s.inService -> t.t("state_connected")
        else -> t.t("state_no_service")
    }
    GlassCard {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = s.gen.title(t),
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(EcAccent, EcAccent2)),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Black
                ),
                maxLines = 1
            )
            Text(status, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.titleSmall)
            s.carrier?.let { Text(it, color = EcAccent, fontWeight = FontWeight.SemiBold) }
        }
    }
}

private fun qualityOf(rsrp: Int?): Pair<String, Color>? = when {
    rsrp == null -> null
    rsrp >= -80 -> "q_excellent" to EcGood
    rsrp >= -95 -> "q_good" to EcAccent
    rsrp >= -105 -> "q_fair" to EcWarn
    else -> "q_poor" to EcBad
}

@Composable
private fun SignalCard(s: NetworkSnapshot) {
    val t = LocalL.current
    val c = s.primaryCell
    GlassCard {
        SectionTitle(t.t("card_signal"))
        if (c == null) {
            Text(t.t("cells_empty"), color = Color.White.copy(alpha = 0.6f))
        } else {
            qualityOf(c.rsrp)?.let { (key, color) ->
                Text(t.t(key), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = color)
            }
            InfoRow("RSRP", fmtDb(c.rsrp, "dBm"))
            InfoRow("RSRQ", fmtDb(c.rsrq, "dB"))
            InfoRow("SINR", fmtDb(c.sinr, "dB"))
            InfoRow(t.t("dbm_label"), fmtDb(c.dbm, "dBm"))
        }
    }
}

@Composable
private fun CellCard(s: NetworkSnapshot) {
    val t = LocalL.current
    val c = s.primaryCell
    GlassCard {
        SectionTitle(t.t("card_cell"))
        if (c == null) {
            Text(t.t("cells_empty"), color = Color.White.copy(alpha = 0.6f))
        } else {
            InfoRow(t.t("tech"), c.tech)
            InfoRow(t.t("band"), c.bands.joinToString(", ").ifEmpty { "—" })
            InfoRow(t.t("pci"), c.pci?.toString() ?: "—")
            InfoRow(t.t("channel"), c.channel?.toString() ?: "—")
            InfoRow(t.t("cell_id"), c.cellId?.toString() ?: "—")
            InfoRow(t.t("tac"), c.tac?.toString() ?: "—")
            InfoRow(t.t("bandwidth"), c.bandwidthKhz?.let { t.f("fmt_mhz", it / 1000.0) } ?: "—")
            InfoRow(t.t("registered"), t.yn(c.registered))
        }
    }
}

@Composable
private fun CarrierCard(s: NetworkSnapshot) {
    val t = LocalL.current
    GlassCard {
        SectionTitle(t.t("card_carrier"))
        InfoRow(t.t("carrier"), s.carrier ?: t.t("carrier_unknown"))
        InfoRow(t.t("mccmnc"), s.mccmnc ?: "—")
        InfoRow(t.t("country"), s.country ?: "—")
        InfoRow(t.t("roaming"), t.yn(s.roaming))
        InfoRow(t.t("sim_count"), s.sims.size.toString())
        s.sims.forEach { sim ->
            val details = listOfNotNull(sim.carrier, if (sim.embedded) t.t("embedded") else null)
                .joinToString(" · ")
                .ifEmpty { "—" }
            InfoRow(t.f("sim_slot", sim.slot), details)
        }
    }
}

@Composable
private fun LinkCard(s: NetworkSnapshot) {
    val t = LocalL.current
    val transport = when (s.transport) {
        "cellular" -> t.t("transport_cellular")
        "wifi" -> t.t("transport_wifi")
        "ethernet" -> t.t("transport_ethernet")
        "vpn" -> t.t("transport_vpn")
        "none" -> t.t("transport_none")
        else -> s.transport
    }
    GlassCard {
        SectionTitle(t.t("card_link"))
        InfoRow(t.t("transport"), transport)
        InfoRow(t.t("internet"), t.yn(s.validated))
        InfoRow(t.t("metered"), t.yn(s.metered))
        InfoRow(t.t("down_link"), s.downKbps?.let { t.f("fmt_mbps", it / 1000.0) } ?: "—")
        InfoRow(t.t("up_link"), s.upKbps?.let { t.f("fmt_mbps", it / 1000.0) } ?: "—")
        InfoRow(t.t("iface"), s.iface ?: "—")
        InfoRow(t.t("ipv4"), s.ipv4.joinToString().ifEmpty { "—" })
        InfoRow(t.t("dns"), s.dns.joinToString().ifEmpty { "—" })
        InfoRow(t.t("mtu"), s.mtu?.toString() ?: "—")
    }
}

@Composable
private fun CellsCard(s: NetworkSnapshot) {
    val t = LocalL.current
    GlassCard {
        SectionTitle(t.t("card_cells"))
        if (s.cells.isEmpty()) {
            Text(t.t("cells_empty"), color = Color.White.copy(alpha = 0.6f))
        }
        s.cells.forEach { c: CellItem ->
            val mark = if (c.registered) " ●" else ""
            Text(
                text = "${c.tech} · PCI ${c.pci ?: "—"} · ${fmtDb(c.rsrp, "dBm")}$mark",
                color = if (c.registered) EcAccent else Color.White.copy(alpha = 0.75f)
            )
        }
    }
}
