package com.adafruit.pyleap

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache

/**
 * Created by Antonio García (antonio@openroad.es)
 */

class PyLeapApplication : Application()/*, ImageLoaderFactory*/ {
    // AppContainer instance used by the rest of classes to obtain dependencies
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainerImpl(this)
    }
/*
    // region ImageLoaderFactory
    override fun newImageLoader(): ImageLoader {
        ImageLoader.Builder(applicationContext)
            .diskCache {
                DiskCache.Builder()
                    .directory(applicationContext.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02)
                    .build()

            }
    }
    // endregion
 */

}