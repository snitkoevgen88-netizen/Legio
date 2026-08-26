package com.example.data

import androidx.room.TypeConverter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

/**
 * Converters for Room to handle complex types.
 * Uses Moshi to serialize/deserialize List<String> to JSON.
 */
object Converters {
    private val moshi: Moshi = Moshi.Builder().build()
    private val listType = Types.newParameterizedType(List::class.java, String::class.java)
    private val listAdapter = moshi.adapter<List<String>>(listType)

    @TypeConverter
    @JvmStatic
    fun fromStringList(list: List<String>?): String = listAdapter.toJson(list ?: emptyList())

    @TypeConverter
    @JvmStatic
    fun toStringList(json: String?): List<String> = json?.let { listAdapter.fromJson(it) } ?: emptyList()
}
