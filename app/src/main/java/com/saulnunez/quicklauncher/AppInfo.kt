package com.saulnunez.quicklauncher

import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable

data class AppInfo(val dataOrigin: ResolveInfo, val packageManager: PackageManager) {
    val appLabel = dataOrigin.loadLabel(packageManager).toString()
    var icon: Drawable = dataOrigin.loadIcon(packageManager)

    fun UpdateIcon(withIconPack: IconPack?) {}

}