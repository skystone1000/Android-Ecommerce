package com.skystone1000.shrine.application

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Hilt application entry point. The legacy `instance` singleton and the AppCompat vector shim were
 * removed in plan_8 Phase 5 along with the Fragment/Volley stack that depended on them.
 */
@HiltAndroidApp
class ShrineApplication : Application()
