package com.werty.easycellular.core

import android.os.Build

enum class OsFamily { ANDROID, IOS, OTHER }

data class DeviceOsInfo(
    val family: OsFamily,
    val detectedBy: String,
    val release: String,
    val sdkInt: Int,
    val codename: String,
    val securityPatch: String?,
    val manufacturer: String,
    val model: String,
    val abis: List<String>
)

/**
 * Определитель платформы. Android-приложение определяет ОС по признакам JVM-рантайма:
 * на Android виртуальная машина называется ART (или Dalvik), на iOS такого рантайма нет.
 * Версия iOS определяется в iOS-версии приложения (PlatformDetector.swift).
 */
object PlatformDetector {

    fun detect(): DeviceOsInfo {
        val vm = System.getProperty("java.vm.name").orEmpty()
        val isAndroidRuntime = vm.contains("Dalvik", ignoreCase = true) || vm.contains("ART", ignoreCase = true)
        return DeviceOsInfo(
            family = if (isAndroidRuntime) OsFamily.ANDROID else OsFamily.OTHER,
            detectedBy = "java.vm.name = $vm",
            release = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT,
            codename = Build.VERSION.CODENAME,
            securityPatch = Build.VERSION.SECURITY_PATCH.takeIf { it.isNotBlank() },
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            abis = Build.SUPPORTED_ABIS.toList()
        )
    }

    fun supportsDisplayInfo() = Build.VERSION.SDK_INT >= 31
    fun supportsCellInfo() = Build.VERSION.SDK_INT >= 29
    fun isAndroid17OrNewer() = Build.VERSION.SDK_INT >= 37
}
