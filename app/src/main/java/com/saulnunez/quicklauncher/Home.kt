package com.saulnunez.quicklauncher

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.content.pm.ResolveInfo
import android.os.Build
import android.support.v7.widget.GridLayoutManager
import android.util.Log
import kotlinx.android.synthetic.main.activity_home.*

import java.util.*


class Home : AppCompatActivity(), AppInstallReceiver.IOnAppChanged {
    var adapter = AppIconAdapter(getApps(), packageManager, this)

    override fun appUninstalled(packageChanged: String) {
        val index = adapter.appList.indexOfFirst { it.dataOrigin.activityInfo.packageName == packageChanged }
        Log.d("QuickLauncher", "Index " + index + "deleted as app " + packageChanged + " was uninstalled")
        adapter.appList.removeAt(index)
        adapter.notifyItemRemoved(index)
    }

    override fun appInstalled(packageChanged: String) {
        adapter.appList = getApps()
        appGrid.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        AppInstallReceiver.registerReceiver(withContext = this)
        AppInstallReceiver.classesToAlert.add(this)

        val layoutMan: RecyclerView.LayoutManager =
                GridLayoutManager(applicationContext, 4)
        appGrid.layoutManager = layoutMan
        appGrid.itemAnimator = DefaultItemAnimator()
        appGrid.adapter = adapter
    }

    private fun getApps(): MutableList<AppInfo> {
        //Fill info about appList
        val intentForGettingLaunchableApps = Intent(Intent.ACTION_MAIN, null)
        intentForGettingLaunchableApps.addCategory(Intent.CATEGORY_LAUNCHER)

        val appList = packageManager.queryIntentActivities(intentForGettingLaunchableApps, 0)
        Collections.sort(appList, ResolveInfo.DisplayNameComparator(packageManager))

        //Removing own activity from launcher
        val packageName = applicationContext.packageName

        // I think the last one is less efficient, so this one is used on newer platforms
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            appList.removeIf { it.activityInfo.packageName == packageName }
        } else {
            appList.remove(appList.find { it.activityInfo.packageName == packageName })
        }

        val appInfo: MutableList<AppInfo> = mutableListOf()
        for (app in appList) {
            appInfo.add(AppInfo(app, packageManager))
        }

        return appInfo
    }

    override fun onDestroy() {
        super.onDestroy()

        AppInstallReceiver.classesToAlert.remove(this)
    }


}
