package com.saulnunez.quicklauncher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppChangeReceiver : BroadcastReceiver() {
    var onAppInstalledListener: IOnAppChanged? = null

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent != null && context != null) {
            val installedOrUpdated = intent.data?.schemeSpecificPart ?: return

            when (intent.action) {
                Intent.ACTION_PACKAGE_ADDED -> installedOrUpdated.let {
                    onAppInstalledListener?.appInstalled(it)
                }
                Intent.ACTION_PACKAGE_REMOVED -> installedOrUpdated.let {
                    onAppInstalledListener?.appUninstalled(it)
                }
            }
        }
    }

    interface IOnAppChanged {
        fun appInstalled(packageChanged: String)
        fun appUninstalled(packageChanged: String)
    }

}