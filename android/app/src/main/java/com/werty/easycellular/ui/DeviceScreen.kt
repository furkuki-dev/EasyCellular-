package com.werty.easycellular.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.werty.easycellular.core.DeviceOsInfo
import com.werty.easycellular.core.OsFamily
import com.werty.easycellular.core.PlatformDetector
import com.werty.easycellular.i18n.LocalL

@Composable
fun DeviceScreen(info: DeviceOsInfo) {
    val t = LocalL.current
    val platform = when (info.family) {
        OsFamily.ANDROID -> "Android"
        OsFamily.IOS -> "iOS"
        OsFamily.OTHER -> "Other"
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard {
            Text(
                text = "$platform ${info.release}",
                style = TextStyle(
                    brush = Brush.linearGradient(listOf(EcAccent, EcAccent2)),
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black
                )
            )
            Text("API ${info.sdkInt}", color = Color.White.copy(alpha = 0.7f))
        }
        GlassCard {
            SectionTitle(t.t("device_platform"))
            InfoRow(t.t("device_platform"), platform)
            InfoRow(t.t("device_detected_by"), info.detectedBy)
            InfoRow(t.t("device_version"), info.release)
            InfoRow(t.t("device_api"), info.sdkInt.toString())
            InfoRow(t.t("device_patch"), info.securityPatch ?: "—")
            InfoRow(t.t("device_maker"), info.manufacturer)
            InfoRow(t.t("device_model"), info.model)
            InfoRow(t.t("device_abi"), info.abis.joinToString().ifEmpty { "—" })
        }
        GlassCard {
            SectionTitle(t.t("device_feature"))
            InfoRow(t.t("feat_display"), t.yn(PlatformDetector.supportsDisplayInfo()))
            InfoRow(t.t("feat_cells"), t.yn(PlatformDetector.supportsCellInfo()))
            InfoRow(t.t("feat_a17"), t.yn(PlatformDetector.isAndroid17OrNewer()))
        }
    }
}
