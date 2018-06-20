package com.saulnunez.quicklauncher

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.shortcut_item_layout.view.*

class AppShortcutAdapter(var shortcuts: List<ShortcutInfo>,
                         val context: Context, val launcherApps: LauncherApps) :
        RecyclerView.Adapter<AppShortcutAdapter.AppShortcutViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppShortcutViewHolder {
        return AppShortcutViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.shortcut_item_layout,
                parent, false))
    }

    override fun getItemCount(): Int = shortcuts.size

    override fun onBindViewHolder(holder: AppShortcutViewHolder, position: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            holder.setShortcut(launcherApps.getShortcutIconDrawable(shortcuts[position],
                    context.resources.displayMetrics.densityDpi),
                    shortcuts[position].shortLabel.toString(),
                    shortcuts[position])
        }
    }

    inner class AppShortcutViewHolder(val view: View) : RecyclerView.ViewHolder(view),
            View.OnClickListener {
        init {
            view.setOnClickListener(this)
        }

        var pointingToShortcut: ShortcutInfo? = null
        override fun onClick(v: View?) {
            Log.d("Quick launcher", "On shortcut click")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                launcherApps.startShortcut(pointingToShortcut, null, null)
            }
        }

        fun setShortcut(icon: Drawable, shortcutLabel: String, shortcut: ShortcutInfo) {
            view.shortcutIcon.setImageDrawable(icon)
            view.shortcutLabel.text = shortcutLabel

            pointingToShortcut = shortcut
        }
    }
}

