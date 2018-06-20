package com.saulnunez.quicklauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppInstallReceiver : BroadcastReceiver() {
    companion object {
        val classesToAlert: MutableList<IOnAppChanged> = mutableListOf<IOnAppChanged>()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val info = intent.extras

            val installedOrUpdated = intent.data.encodedSchemeSpecificPart

            if (classesToAlert.isNotEmpty()) {
                for (classToAlert in classesToAlert) {
                    when (intent.action) {
                        Intent.ACTION_PACKAGE_ADDED -> classToAlert.AppInstalled(packageChanged = installedOrUpdated)
                        Intent.ACTION_PACKAGE_REMOVED -> classToAlert.AppUninstalled(packageChanged = installedOrUpdated)
                    }
                }
            }
        }
    }

    interface IOnAppChanged {
        fun AppInstalled(packageChanged: String)
        fun AppUninstalled(packageChanged: String)
    }

}