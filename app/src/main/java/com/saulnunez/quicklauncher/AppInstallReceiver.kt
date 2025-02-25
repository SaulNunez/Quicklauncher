package com.saulnunez.quicklauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppInstallReceiver : BroadcastReceiver() {
    var onAppInstalledListener: IOnAppChanged? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null) {
            val info = intent.extras

            val installedOrUpdated = intent.data?.encodedSchemeSpecificPart
            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> installedOrUpdated?.let {
                    onAppInstalledListener?.appInstalled(
                        it
                    )
                }
                Intent.ACTION_PACKAGE_REMOVED -> installedOrUpdated?.let {
                    onAppInstalledListener?.appUninstalled(
                        it
                    )
                }
            }
        }
    }

    interface IOnAppChanged {
        fun appInstalled(packageChanged: String)
        fun appUninstalled(packageChanged: String)
    }

}