package com.kalindu.pocketfit.utils

import com.google.gson.JsonParser
import com.kalindu.pocketfit.data.model.Exercise

object ExerciseJsonParser {
    fun parseRemote(json: String): List<Exercise> = parse(json, requirePositiveId = true)

    fun parseStored(json: String): List<Exercise> = parse(json, requirePositiveId = false)

    private fun parse(json: String, requirePositiveId: Boolean): List<Exercise> {
        val root = runCatching { JsonParser.parseString(json) }.getOrNull()
        if (root?.isJsonArray != true) return emptyList()

        return root.asJsonArray.mapNotNull { element ->
            val item = element.takeIf { it.isJsonObject }?.asJsonObject ?: return@mapNotNull null
            val id = runCatching {
                item.get("id")?.takeIf { it.isJsonPrimitive }?.asInt
            }.getOrNull() ?: return@mapNotNull null
            val name = item.stringValue("name")
            if (name.isBlank() || id == 0 || (requirePositiveId && id < 0)) {
                return@mapNotNull null
            }

            Exercise(
                id = id,
                name = name,
                description = item.stringValue("description").ifBlank {
                    "No instructions are available for this exercise."
                },
                category = item.stringValue("category").ifBlank { "General" },
                muscles = item.stringList("muscles"),
                equipment = item.stringList("equipment")
            )
        }.distinctBy(Exercise::id)
    }

    private fun com.google.gson.JsonObject.stringValue(key: String): String =
        get(key)
            ?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
            ?.asString
            ?.trim()
            .orEmpty()

    private fun com.google.gson.JsonObject.stringList(key: String): List<String> =
        get(key)
            ?.takeIf { it.isJsonArray }
            ?.asJsonArray
            ?.mapNotNull { value ->
                value.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }
                    ?.asString
                    ?.trim()
                    ?.takeIf(String::isNotBlank)
            }
            ?.distinct()
            .orEmpty()
}
