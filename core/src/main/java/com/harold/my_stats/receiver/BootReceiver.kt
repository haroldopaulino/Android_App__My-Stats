package com.harold.my_stats.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.harold.my_stats.service.CollectorService

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        CollectorService.start(context)
    }
}
