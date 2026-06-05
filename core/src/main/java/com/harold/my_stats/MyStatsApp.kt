package com.harold.my_stats

import android.app.Application
import com.harold.my_stats.db.MyStatsDatabase

class MyStatsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MyStatsDatabase.initialize(this)
    }
}
