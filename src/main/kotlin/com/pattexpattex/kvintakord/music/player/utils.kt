package com.pattexpattex.kvintakord.music.player

import com.adamratzman.spotify.SpotifyRestAction
import com.pattexpattex.kvintakord.music.player.Executors.spotifySearchExecutor
import com.sedmelluq.lava.common.tools.DaemonThreadFactory
import java.util.concurrent.*
import java.util.concurrent.Executors

fun toReadableTime(ms: Number) = when (ms) {
    Long.MAX_VALUE, -1L -> "--:--:--"
    else -> "%02d:%02d:%02d".format(
        TimeUnit.MILLISECONDS.toHours(ms.toLong()),
        TimeUnit.MILLISECONDS.toMinutes(ms.toLong()) % TimeUnit.HOURS.toMinutes(1),
        TimeUnit.MILLISECONDS.toSeconds(ms.toLong()) % TimeUnit.MINUTES.toSeconds(1))
}

@JvmName("toReadableTimeExt")
fun Number?.toReadableTime() = toReadableTime(this ?: -1)

object Executors {
    val scheduledExecutor: ScheduledExecutorService = Executors.newScheduledThreadPool(10, DaemonThreadFactory("util"))
    val spotifySearchExecutor: ExecutorService = Executors.newCachedThreadPool(DaemonThreadFactory("spotify-search"))

    fun stopAll() {
        scheduledExecutor.shutdown()
        spotifySearchExecutor.shutdown()
    }
}

fun <T> SpotifyRestAction<T>.toCompletableFuture(): CompletableFuture<T> =
    CompletableFuture.supplyAsync(::complete, spotifySearchExecutor)