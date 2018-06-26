package com.saulnunez.quicklauncher

import android.content.Context
import android.graphics.drawable.Drawable
import org.w3c.dom.Document
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class IconPack(packageName: String, context: Context) {
    private var appFilter: Document? = null

    private var iconBack: MutableList<Drawable> = mutableListOf()
    private var iconMask: Drawable? = null
    private var iconUp: Drawable? = null

    init {
        val iconPackContext = context.createPackageContext(packageName,
                Context.CONTEXT_IGNORE_SECURITY)
        val iconPackDir = iconPackContext.packageResourcePath + File.pathSeparator +
                "xml" + File.pathSeparator + "appfilter.xml"
        val appFilterFile = File(iconPackDir)
        appFilter = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(appFilterFile)
        appFilter!!.documentElement.normalize()

        //appFilter!!.getElementsByTagName("iconmask").item(0).attributes.getNamedItem("img1").
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