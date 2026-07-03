package com.google.codelabs.mdc.kotlin.shrine.database

import android.content.Context
import com.google.codelabs.mdc.kotlin.shrine.R
import com.google.codelabs.mdc.kotlin.shrine.models.Product
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader

/**
 * Reads the bundled product catalog (res/raw/products.json) into [Product] entities.
 * Used to seed the Room `products` table on first launch.
 */
object ProductSeed {
    fun read(context: Context): List<Product> {
        val json = context.resources.openRawResource(R.raw.products)
            .bufferedReader().use(BufferedReader::readText)
        val type = object : TypeToken<List<Product>>() {}.type
        return Gson().fromJson(json, type)
    }
}
