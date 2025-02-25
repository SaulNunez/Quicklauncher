package com.saulnunez.quicklauncher

import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.saulnunez.quicklauncher.databinding.AppItemLayoutBinding
import com.saulnunez.quicklauncher.databinding.ShortcutPopupBinding


class AppIconAdapter(var appList: MutableList<AppInfo>,
                     private val packageManager: PackageManager,
                     private val context: Context) : RecyclerView.Adapter<AppIconAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemBinding =  AppItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemBinding, packageManager, context)
    }

    override fun getItemCount(): Int = appList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setPackageInfo(appList[position].appLabel,
                appList[position].dataOrigin.activityInfo.packageName.toString(),
                appList[position].icon)
    }

    class ViewHolder(private val itemBinding: AppItemLayoutBinding, private val packageManager: PackageManager,
                     private val context: Context) : RecyclerView.ViewHolder(itemBinding.root),
            View.OnClickListener, View.OnLongClickListener {
        override fun onLongClick(v: View?): Boolean {
            val inflatedView = ShortcutPopupBinding.inflate(LayoutInflater.from(context), null, false)
            //Setting popup window
            val popupWindow = PopupWindow(inflatedView.root, LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT)

            inflatedView.buttonDelete.setOnClickListener {
                val uninstallIntent = Intent(Intent.ACTION_DELETE,
                        Uri.fromParts("package", packageName, null))
                context.startActivity(uninstallIntent)

                popupWindow.dismiss()
            }

            inflatedView.buttonDetails.setOnClickListener {
                val appDetailsPageIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null))
                appDetailsPageIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(appDetailsPageIntent)

                popupWindow.dismiss()
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
                    val shortcuts: MutableList<ShortcutInfo>? = launcherApps.getShortcuts(shortcutQuery,
                            android.os.Process.myUserHandle())

                    val layoutMan: RecyclerView.LayoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
                    val shortcutAdapter =
                        shortcuts?.let { AppShortcutAdapter(it, context, launcherApps) }

                    inflatedView.recyclerView.layoutManager = layoutMan
                    inflatedView.recyclerView.itemAnimator = androidx.recyclerview.widget.DefaultItemAnimator()
                    inflatedView.recyclerView.adapter = shortcutAdapter

                    popupWindow.isFocusable = true
                    val location = IntArray(2)
                    v!!.getLocationOnScreen(location)
                    popupWindow.showAtLocation(v, Gravity.NO_GRAVITY, location[0], location[1])
                } catch (e: Exception) {
                    //Logic to run if we aren't the primary launcher, thus we don't have permission
                    //to look for shortcuts
                }
            }

            return true
        }

        private var packageName: String? = null

        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        override fun onClick(v: View?) {
            val intentToLaunchApp = packageName?.let { context.startActivity(packageManager.getLaunchIntentForPackage(it)) }
        }

        fun setPackageInfo(label: String, packageName: String, icon: Drawable) {
            this.packageName = packageName
            itemBinding.appName.text = label
            itemBinding.appIcon.setImageDrawable(icon)
        }
    }

}