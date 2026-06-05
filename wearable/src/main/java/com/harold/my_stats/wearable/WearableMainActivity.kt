package com.harold.my_stats.wearable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class WearableMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { WearValidationScreen() }
    }
}
