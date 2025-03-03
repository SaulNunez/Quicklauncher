package com.saulnunez.quicklauncher

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ResolveInfo
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.saulnunez.quicklauncher.databinding.ActivityHomeBinding
import java.util.Collections


class Home : AppCompatActivity(), AppChangeReceiver.IOnAppChanged {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var adapter: AppIconAdapter
    val instance = AppChangeReceiver()

    override fun appUninstalled(packageChanged: String) {
        val index = adapter.appList.find { it.dataOrigin.activityInfo.packageName == packageChanged }
        Log.d("QuickLauncher", "Index " + index + "deleted as app " + packageChanged + " was uninstalled")
        adapter.appList.remove(index)
        adapter.notifyDataSetChanged()
    }

    override fun appInstalled(packageChanged: String) {
        adapter.appList = getApps(this)
        binding.appGrid.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerAppUpdateReceiver()

        adapter = AppIconAdapter(getApps(this), packageManager, this)

        val layoutMan: RecyclerView.LayoutManager =
                LinearLayoutManager(applicationContext)
        binding.appGrid.layoutManager = layoutMan
        binding.appGrid.itemAnimator = DefaultItemAnimator()
        binding.appGrid.adapter = adapter
    }

    private fun getApps(context: Context): MutableList<AppInfo> {
        //Fill info about appList
        val intentForGettingLaunchableApps = Intent(Intent.ACTION_MAIN, null)
        intentForGettingLaunchableApps.addCategory(Intent.CATEGORY_LAUNCHER)

        val appList = packageManager.queryIntentActivities(intentForGettingLaunchableApps, 0)
        Collections.sort(appList, ResolveInfo.DisplayNameComparator(packageManager))

        //Removing own activity from launcher
        val packageName = applicationContext.packageName

        appList.remove(appList.find { it.activityInfo.packageName == packageName })

        val appInfo: MutableList<AppInfo> = mutableListOf()
        for (app in appList) {
            appInfo.add(AppInfo(app, packageManager))
        }

        return appInfo
    }

    private fun registerAppUpdateReceiver() {
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

        instance.onAppInstalledListener = this

        this.registerReceiver(instance, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(instance)
    }
}
