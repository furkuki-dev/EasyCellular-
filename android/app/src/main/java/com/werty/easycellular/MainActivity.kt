package com.werty.easycellular

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.werty.easycellular.core.NetworkCollector
import com.werty.easycellular.i18n.LocalL
import com.werty.easycellular.i18n.L
import com.werty.easycellular.ui.DeviceScreen
import com.werty.easycellular.ui.EcBgBottom
import com.werty.easycellular.ui.EcBgTop
import com.werty.easycellular.ui.EcColors
import com.werty.easycellular.ui.HistoryScreen
import com.werty.easycellular.ui.NetworkScreen
import com.werty.easycellular.ui.SettingsScreen
import com.werty.easycellular.ui.SpeedScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AppRoot() }
    }
}

enum class Tab(val key: String, val icon: ImageVector) {
    NETWORK("tab_network", Icons.Filled.Home),
    TEST("tab_test", Icons.Filled.PlayArrow),
    HISTORY("tab_history", Icons.Filled.List),
    DEVICE("tab_device", Icons.Filled.Info),
    SETTINGS("tab_settings", Icons.Filled.Settings)
}

@Composable
fun AppRoot() {
    val vm: AppViewModel = viewModel()
    val lang by vm.language.collectAsStateWithLifecycle()
    CompositionLocalProvider(LocalL provides L(lang)) {
        MaterialTheme(colorScheme = EcColors) {
            MainContent(vm)
        }
    }
}

@Composable
private fun MainContent(vm: AppViewModel) {
    val t = LocalL.current
    val ctx = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(Tab.NETWORK) }

    val network by vm.network.collectAsStateWithLifecycle()
    val speed by vm.speed.collectAsStateWithLifecycle()
    val history by vm.history.collectAsStateWithLifecycle()
    val lang by vm.language.collectAsStateWithLifecycle()
    val pingHost by vm.pingHost.collectAsStateWithLifecycle()

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        vm.onPermissionsChanged()
    }
    LaunchedEffect(Unit) {
        if (vm.missingPermissions().isNotEmpty()) permLauncher.launch(NetworkCollector.REQUIRED)
    }

    Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(EcBgTop, EcBgBottom)))) {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                NavigationBar(containerColor = Color(0xCC0B1230)) {
                    Tab.entries.forEach { item ->
                        NavigationBarItem(
                            selected = tab == item,
                            onClick = { tab = item },
                            icon = { Icon(item.icon, contentDescription = null) },
                            label = { Text(t.t(item.key)) }
                        )
                    }
                }
            }
        ) { inner ->
            Box(Modifier.padding(inner)) {
                when (tab) {
                    Tab.NETWORK -> NetworkScreen(
                        state = network,
                        missing = vm.missingPermissions(),
                        onGrant = { permLauncher.launch(NetworkCollector.REQUIRED) },
                        onSettings = { openAppSettings(ctx) }
                    )
                    Tab.TEST -> SpeedScreen(state = speed, onStart = vm::startTest, onStop = vm::stopTest)
                    Tab.HISTORY -> HistoryScreen(records = history, onClear = vm::clearHistory)
                    Tab.DEVICE -> DeviceScreen(info = vm.device)
                    Tab.SETTINGS -> SettingsScreen(
                        lang = lang,
                        pingHost = pingHost,
                        onLang = vm::setLanguage,
                        onHost = vm::setPingHost,
                        onClear = vm::clearHistory,
                        onOpenPermissions = { openAppSettings(ctx) }
                    )
                }
            }
        }
    }
}

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(
        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
        Uri.fromParts("package", context.packageName, null)
    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}
