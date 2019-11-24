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
    companion object {
        val classesToAlert: MutableList<IOnAppChanged> = mutableListOf()

        fun registerReceiver(withContext: Context) {
            val intentFilter = IntentFilter(Intent.ACTION_PACKAGE_ADDED)
            intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED)

            withContext.registerReceiver(AppInstallReceiver as BroadcastReceiver, intentFilter)
        }

    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val info = intent.extras

            val installedOrUpdated = intent.data.encodedSchemeSpecificPart

            Log.d("QuickLauncher", installedOrUpdated)
            if (classesToAlert.isNotEmpty()) {
                for (classToAlert in classesToAlert) {
                    when (intent.action) {
                        Intent.ACTION_PACKAGE_ADDED -> classToAlert.appInstalled(packageChanged = installedOrUpdated)
                        Intent.ACTION_PACKAGE_REMOVED -> classToAlert.appUninstalled(packageChanged = installedOrUpdated)
                    }
                }
            }
        }
    }

    interface IOnAppChanged {
        fun appInstalled(packageChanged: String)
        fun appUninstalled(packageChanged: String)
    }

}