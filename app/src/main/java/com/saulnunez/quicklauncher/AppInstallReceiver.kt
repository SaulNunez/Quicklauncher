package com.saulnunez.quicklauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.util.Log

class AppInstallReceiver : BroadcastReceiver() {
    var onAppInstalledListener: IOnAppChanged? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val info = intent.extras

            val installedOrUpdated = intent.data.encodedSchemeSpecificPart

            Log.d("QuickLauncher", installedOrUpdated)
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> onAppInstalledListener?.appInstalled(installedOrUpdated)
                Intent.ACTION_PACKAGE_REMOVED -> onAppInstalledListener?.appUninstalled(installedOrUpdated)
            }
        }
    }

    interface IOnAppChanged {
        fun appInstalled(packageChanged: String)
        fun appUninstalled(packageChanged: String)
    }

}