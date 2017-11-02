package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.TypeConverter

class Converters {
    @TypeConverter
    fun longArrayToString(longArray: LongArray?): String
            = longArray?.joinToString(separator = "|") ?: ""

    @TypeConverter
    fun stringToLongArray(string: String?): LongArray
            = string?.split("|")?.filter { it.length > 0 }?.map { it.toLong() }?.toLongArray() ?: longArrayOf()

    @TypeConverter
    fun stringArrayToString(array: Array<String>): String
            = array.joinToString("|$|")

    @TypeConverter
    fun stringToStringArray(string: String): Array<String>
        = string.split("|$|").toTypedArray()
}