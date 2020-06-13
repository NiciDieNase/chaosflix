package de.nicidienase.chaosflix.common.mediadata

import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.hamcrest.number.OrderingComparison.greaterThan
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class ThumbnailParserTest {

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build()
    }

    @Disabled
    @Test
    fun test() {
        val parse = ThumbnailParser(okHttpClient).parse("https://static.media.ccc.de/media/events/gpn/gpn19/67-hd.thumbnails.vtt")
        assertThat(parse.size, greaterThan(999))
    }

    @Test
    fun timestampConverter() {
        assertThat(ThumbnailParser.timestampToMillis("00:00:00.500"), Matchers.`is`(500L))
    }

    @Test
    fun timestampConverter2() {
        assertThat(ThumbnailParser.timestampToMillis("00:00:00.500"), Matchers.`is`(500L))
    }
    @Test
    fun timestampConverter3() {
        assertThat(ThumbnailParser.timestampToMillis("00:01:00.000"), Matchers.`is`(60 * 1000L))
    }
    @Test
    fun timestampConverter4() {
        assertThat(ThumbnailParser.timestampToMillis("01:00:00.000"), Matchers.`is`(60 * 60 * 1000L))
    }
    @Test
    fun timestampConverter5() {
        assertThat(ThumbnailParser.timestampToMillis("03:05:11.111"), Matchers.`is`(11111111L))
    }
}
