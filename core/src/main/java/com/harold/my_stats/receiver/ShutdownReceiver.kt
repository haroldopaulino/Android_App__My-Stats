package com.harold.my_stats.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.harold.my_stats.util.Prefs

class ShutdownReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        Prefs(context).setLastGracefulShutdownEpochMs(System.currentTimeMillis())
    }
}
