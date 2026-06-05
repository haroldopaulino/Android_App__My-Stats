package com.harold.my_stats.phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class PhoneMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { PhoneValidationScreen() }
    }
}
