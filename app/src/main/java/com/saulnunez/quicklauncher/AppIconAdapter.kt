package com.saulnunez.quicklauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.view.ViewGroup.LayoutParams
import kotlinx.android.synthetic.main.app_item_layout.view.*
import kotlinx.android.synthetic.main.shortcut_popup.view.*

class AppIconAdapter(var appList: MutableList<ResolveInfo>,
                     val packageManager: PackageManager,
                     val context: Context) : RecyclerView.Adapter<AppIconAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.app_item_layout,
                parent, false), packageManager, context)
    }

    override fun getItemCount(): Int = appList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setPackageInfo(appList[position].loadLabel(packageManager).toString(),
                appList[position].activityInfo.packageName.toString(),
                appList[position].loadIcon(packageManager))
    }

    class ViewHolder(itemView: View, val packageManager: PackageManager,
                     val context: Context) : RecyclerView.ViewHolder(itemView),
            View.OnClickListener, View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            //Setting popup window
            val popupWindow = PopupWindow(LayoutInflater.from(context).inflate(R.layout.shortcut_popup,
                    null, false), LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT)

            popupWindow.contentView.buttonDetails.setOnClickListener {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }

            //Show shortcuts list
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                val shortcutQuery = LauncherApps.ShortcutQuery()
                shortcutQuery.setQueryFlags(LauncherApps.ShortcutQuery.FLAG_MATCH_DYNAMIC or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_MANIFEST or
                        LauncherApps.ShortcutQuery.FLAG_MATCH_PINNED)
                shortcutQuery.setPackage(packageName)

                val launcherApps = context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                try {
                    val shortcuts: List<ShortcutInfo> = launcherApps.getShortcuts(shortcutQuery,
                            android.os.Process.myUserHandle())

                    val layoutMan: RecyclerView.LayoutManager = LinearLayoutManager(context)
                    val shortcutAdapter = AppShortcutAdapter(shortcuts, context, launcherApps)

                    popupWindow.contentView.recyclerView.layoutManager = layoutMan
                    popupWindow.contentView.recyclerView.itemAnimator = DefaultItemAnimator()
                    popupWindow.contentView.recyclerView.adapter = shortcutAdapter

                    popupWindow.isFocusable = true

                    popupWindow.showAsDropDown(v, -v!!.width, -v!!.height, Gravity.START)
                } catch (e: Exception) {
                    //Logic to run if we aren't the primary launcher, thus we don't have permission
                    //to look for shortcuts
                }
            }

            return false
        }

        private var packageName: String? = null

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            Log.d("Debug QuickLauncher", "OnClick")
            val intentToLaunchApp = packageManager.getLaunchIntentForPackage(packageName)
            if (intentToLaunchApp != null) {
                Log.d("Debug QuickLauncher", "Starting")
                context.startActivity(intentToLaunchApp)
            }
        }

        fun setPackageInfo(label: String, packageName: String, icon: Drawable) {
            this.packageName = packageName
            itemView.appName.text = label
            itemView.appIcon.setImageDrawable(icon)
        }
    }

}