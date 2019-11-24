package com.saulnunez.quicklauncher

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.content.res.XmlResourceParser
import android.graphics.drawable.Drawable
import org.w3c.dom.Document
import org.w3c.dom.Element
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class IconPack(packageName: String, context: Context, packageManager: PackageManager) {
    private var appFilter: XmlResourceParser
    private val res: Resources

    private var iconBack: MutableList<Drawable> = mutableListOf()
    private var iconMask: Drawable? = null
    private var iconUp: Drawable? = null

    init {
        val iconPackContext = context.createPackageContext(packageName,
                Context.CONTEXT_IGNORE_SECURITY)
        res = iconPackContext.resources
        val appFilterId = res.getIdentifier("appfilter", "xml", packageName)
        appFilter = res.getXml(appFilterId)

        //There's between 1 and 5 drawables that can be used as iconBack
        //As of explained here : https://forum.xda-developers.com/showthread.php?t=1649891
        for (i in (1..5)) {
            val iconBackDrawableId = res.getIdentifier("", "drawable", packageName)
            //iconBack.add(res.getDrawableForDensity())
        }
    }

    fun getIconForActivity(activityName: String): Drawable? {
        return null
    }

    fun getIconBackground(): Drawable? {
        when {
            iconBack.isEmpty() -> return null
            iconBack.size == 1 -> return iconBack[0]
            else -> return iconBack[(0..iconBack.size).shuffled().last()]
        }
    }

    fun getIconMask(): Drawable? {
        return iconMask
    }
}