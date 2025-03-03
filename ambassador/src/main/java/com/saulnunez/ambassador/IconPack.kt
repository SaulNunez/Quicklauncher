package com.saulnunez.ambassador

import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.IOException
import java.util.Locale
import kotlin.random.Random

// Code adapted from https://stackoverflow.com/a/31512017/14228475
// License https://creativecommons.org/licenses/by-sa/3.0/
// Modifications:
// - Autoadapted to Kotlin

class IconPack(private val packageName: String, val name: String, private val mContext: Context) {
    private var mLoaded = false
    private val mPackagesDrawables = HashMap<String?, String?>()

    private val mBackImages: MutableList<Bitmap> = ArrayList()
    private var mMaskImage: Bitmap? = null
    private var mFrontImage: Bitmap? = null
    private var mFactor = 1.0f

    private var iconPackres: Resources? = null

    fun load() {
        // load appfilter.xml from the icon pack package
        try {
            var xpp: XmlPullParser? = null

            iconPackres = mContext.packageManager.getResourcesForApplication(packageName)
            val appfilterid = iconPackres!!.getIdentifier("appfilter", "xml", packageName)
            if (appfilterid > 0) {
                xpp = iconPackres!!.getXml(appfilterid)
            } else {
                // no resource found, try to open it from assests folder
                try {
                    val appfilterstream = iconPackres!!.assets.open("appfilter.xml")

                    val factory = XmlPullParserFactory.newInstance()
                    factory.isNamespaceAware = true
                    xpp = factory.newPullParser()
                    xpp.setInput(appfilterstream, "utf-8")
                } catch (e1: IOException) {
                    Log.d(mContext.getString(R.string.app_name),"No appfilter.xml file")
                }
            }

            if (xpp != null) {
                var eventType = xpp.eventType
                while (eventType != XmlPullParser.END_DOCUMENT) {
                    if (eventType == XmlPullParser.START_TAG) {
                        if (xpp.name == "iconback") {
                            for (i in 0 until xpp.attributeCount) {
                                if (xpp.getAttributeName(i).startsWith("img")) {
                                    val drawableName = xpp.getAttributeValue(i)
                                    val iconback = loadBitmap(drawableName)
                                    if (iconback != null) mBackImages.add(iconback)
                                }
                            }
                        } else if (xpp.name == "iconmask") {
                            if (xpp.attributeCount > 0 && xpp.getAttributeName(0) == "img1") {
                                val drawableName = xpp.getAttributeValue(0)
                                mMaskImage = loadBitmap(drawableName)
                            }
                        } else if (xpp.name == "iconupon") {
                            if (xpp.attributeCount > 0 && xpp.getAttributeName(0) == "img1") {
                                val drawableName = xpp.getAttributeValue(0)
                                mFrontImage = loadBitmap(drawableName)
                            }
                        } else if (xpp.name == "scale") {
                            // mFactor
                            if (xpp.attributeCount > 0 && xpp.getAttributeName(0) == "factor") {
                                mFactor = xpp.getAttributeValue(0).toFloat()
                            }
                        } else if (xpp.name == "item") {
                            var componentName: String? = null
                            var drawableName: String? = null

                            for (i in 0 until xpp.attributeCount) {
                                if (xpp.getAttributeName(i) == "component") {
                                    componentName = xpp.getAttributeValue(i)
                                } else if (xpp.getAttributeName(i) == "drawable") {
                                    drawableName = xpp.getAttributeValue(i)
                                }
                            }
                            if (!mPackagesDrawables.containsKey(componentName)) mPackagesDrawables[componentName] =
                                drawableName
                        }
                    }
                    eventType = xpp.next()
                }
            }
            mLoaded = true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.d(mContext.getString(R.string.app_name),"Cannot load icon pack")
        } catch (e: XmlPullParserException) {
            Log.d(mContext.getString(R.string.app_name),"Cannot parse icon pack appfilter.xml")
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadBitmap(drawableName: String): Bitmap? {
        val id = iconPackres!!.getIdentifier(drawableName, "drawable", packageName)
        if (id > 0) {
            val bitmap = iconPackres!!.getDrawable(id)
            if (bitmap is BitmapDrawable) return bitmap.bitmap
        }
        return null
    }

    private fun loadDrawable(drawableName: String): Drawable? {
        val id = iconPackres!!.getIdentifier(drawableName, "drawable", packageName)
        if (id > 0) {
            val bitmap = iconPackres!!.getDrawable(id)
            return bitmap
        }
        return null
    }

    fun getDrawableIconForPackage(appPackageName: String?, defaultDrawable: Drawable?): Drawable? {
        if (!mLoaded) load()

        val pm: PackageManager = mContext.getPackageManager()
        val launchIntent = pm.getLaunchIntentForPackage(appPackageName!!)
        var componentName: String? = null
        if (launchIntent != null) componentName =
            pm.getLaunchIntentForPackage(appPackageName)!!.component.toString()
        var drawable = mPackagesDrawables[componentName]
        if (drawable != null) {
            return loadDrawable(drawable)
        } else {
            // try to get a resource with the component filename
            if (componentName != null) {
                val start = componentName.indexOf("{") + 1
                val end = componentName.indexOf("}", start)
                if (end > start) {
                    drawable = componentName.substring(start, end).lowercase(Locale.getDefault())
                        .replace(".", "_").replace("/", "_")
                    if (iconPackres!!.getIdentifier(
                            drawable,
                            "drawable",
                            packageName
                        ) > 0
                    ) return loadDrawable(drawable)
                }
            }
        }
        return defaultDrawable
    }

    fun getIconForPackage(appPackageName: String, defaultBitmap: Bitmap?): Bitmap? {
        if (!mLoaded) load()

        val pm: PackageManager = mContext.getPackageManager()
        val launchIntent = pm.getLaunchIntentForPackage(appPackageName)
        var componentName: String? = null
        if (launchIntent != null) componentName =
            pm.getLaunchIntentForPackage(appPackageName)!!.component.toString()
        var drawable = mPackagesDrawables[componentName]
        if (drawable != null) {
            return loadBitmap(drawable)
        } else {
            // try to get a resource with the component filename
            if (componentName != null) {
                val start = componentName.indexOf("{") + 1
                val end = componentName.indexOf("}", start)
                if (end > start) {
                    drawable = componentName.substring(start, end).lowercase(Locale.getDefault())
                        .replace(".", "_").replace("/", "_")
                    if (iconPackres!!.getIdentifier(
                            drawable,
                            "drawable",
                            packageName
                        ) > 0
                    ) return loadBitmap(drawable)
                }
            }
        }
        return generateBitmap(appPackageName, defaultBitmap)
    }

    private fun generateBitmap(appPackageName: String, defaultBitmap: Bitmap?): Bitmap? {
        // the key for the cache is the icon pack package name and the app package name
        val key = "$packageName:$appPackageName"

        // if generated bitmaps cache already contains the package name return it
//            Bitmap cachedBitmap = BitmapCache.getInstance(mContext).getBitmap(key);
//            if (cachedBitmap != null)
//                return cachedBitmap;

        // if no support images in the icon pack return the bitmap itself
        if (mBackImages.size == 0) return defaultBitmap

        val backImageInd: Int = Random.nextInt(mBackImages.size)
        val backImage = mBackImages[backImageInd]
        val w = backImage.width
        val h = backImage.height

        // create a bitmap for the result
        val result = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val mCanvas = Canvas(result)

        // draw the background first
        mCanvas.drawBitmap(backImage, 0f, 0f, null)

        // create a mutable mask bitmap with the same mask
        val scaledBitmap = defaultBitmap
        if (defaultBitmap != null && (defaultBitmap.width > w || defaultBitmap.height > h)) Bitmap.createScaledBitmap(
            defaultBitmap,
            (w * mFactor).toInt(),
            (h * mFactor).toInt(),
            false
        )

        if (mMaskImage != null) {
            // draw the scaled bitmap with mask
            val mutableMask = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
            val maskCanvas = Canvas(mutableMask)
            maskCanvas.drawBitmap(mMaskImage!!, 0f, 0f, Paint())

            // paint the bitmap with mask into the result
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.setXfermode(PorterDuffXfermode(PorterDuff.Mode.DST_OUT))
            mCanvas.drawBitmap(
                scaledBitmap!!,
                ((w - scaledBitmap.width) / 2).toFloat(),
                ((h - scaledBitmap.height) / 2).toFloat(),
                null
            )
            mCanvas.drawBitmap(mutableMask, 0f, 0f, paint)
            paint.setXfermode(null)
        } else  // draw the scaled bitmap without mask
        {
            mCanvas.drawBitmap(
                scaledBitmap!!,
                ((w - scaledBitmap.width) / 2).toFloat(),
                ((h - scaledBitmap.height) / 2).toFloat(),
                null
            )
        }

        // paint the front
        if (mFrontImage != null) {
            mCanvas.drawBitmap(mFrontImage!!, 0f, 0f, null)
        }

        // store the bitmap in cache
//            BitmapCache.getInstance(mContext).putBitmap(key, result);

        // return it
        return result
    }
}