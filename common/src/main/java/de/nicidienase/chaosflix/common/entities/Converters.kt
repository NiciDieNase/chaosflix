package de.nicidienase.chaosflix.common.entities

import android.arch.persistence.room.TypeConverter

class Converters{
        @TypeConverter
        fun longArrayToString(longArray: LongArray): String
            = longArray.joinToString(separator = "|")

        @TypeConverter
        fun stringToLongArray(string: String): LongArray
            = string.split("|").map { it.toLong() }.toLongArray()
}