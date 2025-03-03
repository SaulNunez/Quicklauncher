package com.saulnunez.ambassador

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager


class SystemIconPacks(private val context: Context) {
    fun getIconPacks(): List<IconPack> {
        val packageManager: PackageManager = context.packageManager


        // Define the intent filter you are looking for
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory("com.anddoes.launcher.THEME")


        // Query all matching activities
        val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL)

        return activities.map {
            val packageName = it.activityInfo.packageName
            val activityName = it.activityInfo.name
            IconPack(packageName, activityName, context)
        }
    }
}