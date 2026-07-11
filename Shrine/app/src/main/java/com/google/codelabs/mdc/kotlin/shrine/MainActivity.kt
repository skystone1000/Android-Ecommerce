package com.google.codelabs.mdc.kotlin.shrine

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.google.codelabs.mdc.kotlin.shrine.ui.ShrineApp
import dagger.hilt.android.AndroidEntryPoint

/**
 * Single-Activity host for the Compose app (plan_8 Phase 3). Renders [ShrineApp], which owns the
 * NavHost (auth/main graphs) and the bottom-bar scaffold. The legacy Fragment/XML stack is no
 * longer launched and is removed in Phase 5.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ShrineApp()
        }
    }
}
