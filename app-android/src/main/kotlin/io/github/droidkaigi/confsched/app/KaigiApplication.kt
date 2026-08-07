package io.github.droidkaigi.confsched.app

import android.app.Application
import android.content.Context
import dev.zacsweers.metro.createGraphFactory

class KaigiApplication : Application() {
    val appGraph: AppGraph by lazy {
        createGraphFactory<AndroidAppGraph.Factory>().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
        appGraph.appInitializer.initialize()
    }
}

val Context.appGraph: AppGraph get() = (applicationContext as KaigiApplication).appGraph
