package com.saulnunez.quicklauncher

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.ShortcutInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.saulnunez.quicklauncher.databinding.ShortcutItemLayoutBinding

class AppShortcutAdapter(
    private var shortcuts: List<ShortcutInfo>,
    private val context: Context, val launcherApps: LauncherApps) :
        RecyclerView.Adapter<AppShortcutAdapter.AppShortcutViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppShortcutViewHolder {
        return AppShortcutViewHolder(ShortcutItemLayoutBinding.inflate(LayoutInflater.from(parent.context),
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

    inner class AppShortcutViewHolder(private val view: ShortcutItemLayoutBinding) : RecyclerView.ViewHolder(view.root),
            View.OnClickListener {
        init {
            view.root.setOnClickListener(this)
        }

        private var pointingToShortcut: ShortcutInfo? = null
        override fun onClick(v: View?) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                pointingToShortcut?.let { launcherApps.startShortcut(it, null, null) }
            }
        }

        fun setShortcut(icon: Drawable, shortcutLabel: String, shortcut: ShortcutInfo) {
            view.shortcutIcon.setImageDrawable(icon)
            view.shortcutLabel.text = shortcutLabel

            pointingToShortcut = shortcut
        }
    }
}

