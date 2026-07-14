package com.skystone1000.shrine.core.database

import androidx.room.TypeConverter
import com.skystone1000.shrine.core.model.DeliveryOption
import com.skystone1000.shrine.core.model.OrderStatus
import com.skystone1000.shrine.core.model.Variant
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Room type converters for the list and enum columns, backed by kotlinx-serialization JSON. */
class Converters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter fun fromStringList(value: List<String>): String = json.encodeToString(value)
    @TypeConverter fun toStringList(value: String): List<String> = json.decodeFromString(value)

    @TypeConverter fun fromVariantList(value: List<Variant>): String = json.encodeToString(value)
    @TypeConverter fun toVariantList(value: String): List<Variant> = json.decodeFromString(value)

    @TypeConverter fun fromOrderStatus(value: OrderStatus): String = value.name
    @TypeConverter fun toOrderStatus(value: String): OrderStatus = OrderStatus.valueOf(value)

    @TypeConverter fun fromDeliveryOption(value: DeliveryOption): String = value.name
    @TypeConverter fun toDeliveryOption(value: String): DeliveryOption = DeliveryOption.valueOf(value)
}
