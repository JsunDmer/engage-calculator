package com.engage.core

import kotlinx.serialization.json.Json

object DatasetLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun parse(rawJson: String): Dataset = json.decodeFromString(Dataset.serializer(), rawJson)
}