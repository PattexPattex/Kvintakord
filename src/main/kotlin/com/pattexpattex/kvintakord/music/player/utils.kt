package com.pattexpattex.kvintakord.music.player

import com.adamratzman.spotify.SpotifyRestAction
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.lava.common.tools.DaemonThreadFactory
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

inline fun <reified T> AudioTrack.getUserData(): T? = getUserData(T::class.java)

val AudioTrack.metadata get() = getUserData<TrackMetadata>()

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
    val scheduledExecutor = Executors.newScheduledThreadPool(1, DaemonThreadFactory("util"))
    val spotifySearchExecutor = Executors.newCachedThreadPool(DaemonThreadFactory("spotify-search"))

    fun stopAll() {
        scheduledExecutor.shutdown()
        spotifySearchExecutor.shutdown()
    }
}

fun <T> SpotifyRestAction<T>.toCompletableFuture(): CompletableFuture<T> =
    CompletableFuture.supplyAsync(::complete, com.pattexpattex.kvintakord.music.player.Executors.spotifySearchExecutor)