package com.engage.app.data

import android.content.Context
import com.engage.core.Character
import com.engage.core.Dataset
import com.engage.core.DatasetLoader

object DatasetRepository {

    private const val ASSET_NAME = "engage.json"

    @Volatile
    private var cached: Dataset? = null

    fun load(context: Context): Dataset = cached ?: synchronized(this) {
        cached ?: context.assets.open(ASSET_NAME).bufferedReader().use { reader ->
            DatasetLoader.parse(reader.readText())
        }.also { cached = it }
    }

    fun avatarPath(character: Character): String = character.avatar
}