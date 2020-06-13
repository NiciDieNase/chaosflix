package de.nicidienase.chaosflix.common.mediadata

import java.util.LinkedList
import okhttp3.OkHttpClient
import okhttp3.Request

class ThumbnailParser(private val okHttpClient: OkHttpClient) {

    fun parse(uri: String): List<ThumbnailInfo> {

        val request = Request.Builder().url(uri).build()
        val lines = LinkedList(okHttpClient.newCall(request).execute().body()?.byteStream()?.bufferedReader()?.readLines())

        val results = mutableListOf<Pair<String, String>>()
        while (lines.peek().isNullOrBlank() || lines.peek().equals("WEBVTT")) {
            lines.pop()
        }
        while (lines.isNotEmpty()) {
            val times = lines.pop()
            val datalines = mutableListOf<String>()
            while (lines.peek().isNotEmpty()) {
                datalines.add(lines.pop())
            }
            results.add(times to datalines.first())
            while (!lines.isEmpty() && (lines.peek().isNullOrBlank() || lines.peek().equals("WEBVTT"))) {
                lines.pop()
            }
        }
        return convert(uri, results)
    }

    fun convert(uri: String, input: List<Pair<String, String>>): List<ThumbnailInfo> {
        val baseUri = uri.substringBeforeLast("/")
        return input.map {
            val split = it.first.split(" --> ")
            val thumbUri = "$baseUri/${it.second}"
            ThumbnailInfo(
                    timestampToMillis(split.first()),
                    timestampToMillis(split.last()),
                    thumbUri
            )
        }
    }

    companion object {

        internal fun timestampToMillis(timestamp: String): Long {
            val split1 = timestamp.split(".")
            var result = split1.last().toLong()

            assert(split1.size == 2)
            val split2 = split1.first().split(":").reversed()
            var factor: Long = 1000L
            for (item in split2) {
                result += item.toLong() * factor
                factor *= 60
            }
            return result
        }
    }

    data class ThumbnailInfo(
        val startTime: Long,
        val endTime: Long,
        val thumb: String
    )
}
