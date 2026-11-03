package com.werty.easycellular.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.werty.easycellular.i18n.Lang
import com.werty.easycellular.i18n.LocalL

@Composable
fun SettingsScreen(
    lang: Lang,
    pingHost: String,
    onLang: (Lang) -> Unit,
    onHost: (String) -> Unit,
    onClear: () -> Unit,
    onOpenPermissions: () -> Unit
) {
    val t = LocalL.current
    val ctx = LocalContext.current
    var confirm by remember { mutableStateOf(false) }
    val version = remember {
        runCatching { ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName }.getOrNull() ?: "1.0.0"
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        GlassCard {
            SectionTitle(t.t("set_language"))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Lang.entries.forEach { l ->
                    FilterChip(
                        selected = lang == l,
                        onClick = { onLang(l) },
                        label = { Text(if (l == Lang.RU) "Русский" else "English") }
                    )
                }
            }
        }
        GlassCard {
            SectionTitle(t.t("set_ping_host"))
            OutlinedTextField(
                value = pingHost,
                onValueChange = onHost,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        GlassCard {
            SectionTitle(t.t("set_permissions"))
            OutlinedButton(onClick = onOpenPermissions, modifier = Modifier.fillMaxWidth()) {
                Text(t.t("perm_settings"))
            }
            FilledTonalButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) {
                Text(t.t("set_clear"))
            }
        }
        GlassCard {
            SectionTitle(t.t("set_about"))
            InfoRow(t.t("set_version"), version)
            Text(
                t.t("set_about_text"),
                color = Color.White.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text(t.t("hist_clear_ask")) },
            confirmButton = {
                TextButton(onClick = {
                    onClear()
                    confirm = false
                }) { Text(t.t("hist_clear")) }
            },
            dismissButton = {
                TextButton(onClick = { confirm = false }) { Text(t.t("cancel")) }
            }
        )
    }
}
