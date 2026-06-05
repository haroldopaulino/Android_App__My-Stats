package com.harold.my_stats.util

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull

object JsonUtils {
    val json = Json {
        prettyPrint = false
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun sanitize(element: JsonElement): JsonElement {
        return when (element) {
            is JsonObject -> JsonObject(element.mapValues { sanitize(it.value) })
            is JsonArray -> JsonArray(element.map { sanitize(it) })
            is JsonPrimitive -> {
                if (element.isString || element.booleanOrNull != null) {
                    element
                } else {
                    val number = element.doubleOrNull
                    if (number != null && (number.isNaN() || number.isInfinite())) {
                        JsonNull
                    } else {
                        element
                    }
                }
            }
        }
    }

    fun encodeElement(element: JsonElement): String {
        return json.encodeToString(JsonElement.serializer(), sanitize(element))
    }
}
