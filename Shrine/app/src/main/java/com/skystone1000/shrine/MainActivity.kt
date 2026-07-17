package com.skystone1000.shrine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.skystone1000.shrine.ui.ShrineApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity host for the Compose app (plan_8 Phase 3). Renders [ShrineApp], which owns the
 * NavHost (auth/main graphs) and the bottom-bar scaffold. The legacy Fragment/XML stack is no
 * longer launched and is removed in Phase 5.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // plan_9 Phase A: opt in to edge-to-edge explicitly (transparent system bars). The app
        // already insets its own content per screen; ShrineApp drives the bar icon appearance
        // from the resolved theme so icons stay legible in both light and dark.
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            ShrineApp()
        }
    }
}
