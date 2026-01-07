package com.nano.ai

import android.app.Application
import com.nano.ai.di.AppContainer

class NVApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContainer.init(applicationContext)
    }
}