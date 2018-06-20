package com.saulnunez.quicklauncher

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.RecyclerView
import android.content.pm.ResolveInfo
import android.os.Build
import android.support.v7.widget.GridLayoutManager
import kotlinx.android.synthetic.main.activity_home.*
import java.util.*


class Home : AppCompatActivity(), AppInstallReceiver.IOnAppChanged {
    //var appList : MutableList<ResolveInfo> = mutableListOf<ResolveInfo>()
    lateinit var appAdapter: AppIconAdapter

    override fun AppUninstalled(packageChanged: String) {
        // I think the last one is less efficient, so this one is used on newer platforms
        val uninstalledIndex = appAdapter.appList.indexOfFirst { it.activityInfo.packageName == packageChanged }
        appAdapter.appList.removeAt(uninstalledIndex)
        appAdapter.notifyItemRemoved(uninstalledIndex)
    }

    override fun AppInstalled(packageChanged: String) {
        appAdapter.appList = getApps()
        val installedIndex = appAdapter.appList.indexOfFirst { it.activityInfo.packageName == packageChanged }
        appAdapter.notifyItemInserted(installedIndex)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        AppInstallReceiver.classesToAlert.add(this)

        val layoutMan: RecyclerView.LayoutManager =
                GridLayoutManager(applicationContext, 4)
        appGrid.layoutManager = layoutMan
        appGrid.itemAnimator = DefaultItemAnimator()
        appAdapter = AppIconAdapter(getApps(), packageManager, this)
        appGrid.adapter = appAdapter
    }

    private fun getApps(): MutableList<ResolveInfo> {
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

        return appList
    }


    override fun onDestroy() {
        super.onDestroy()

        AppInstallReceiver.classesToAlert.remove(this)
    }
}
