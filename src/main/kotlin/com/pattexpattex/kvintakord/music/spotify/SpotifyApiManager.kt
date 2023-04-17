package com.pattexpattex.kvintakord.music.spotify

import com.adamratzman.spotify.SpotifyApi
import com.adamratzman.spotify.spotifyAppApi
import com.adamratzman.spotify.utils.runBlockingOnJvmAndNative
import com.pattexpattex.kvintakord.app.SLF4J
import javafx.stage.Modality
import javafx.stage.StageStyle
import tornadofx.*
import java.lang.IllegalArgumentException
import java.util.concurrent.RejectedExecutionException
import java.util.regex.Pattern

class SpotifyApiManager : Controller() {
    @Volatile private var api: SpotifyApi<*,*>? = null
    @Volatile private var validCredentials: Boolean = false

    init {
        reloadApi()
    }

    fun isEnabled() = api != null
    fun search(query: String) = withApi { it.search.searchTrack(query) }
    fun getTrack(url: String) = withApi { it.tracks.getTrack(extractIdFromUrl(url)) }
    fun getPlaylist(url: String) = withApi { it.playlists.getPlaylist(extractIdFromUrl(url)) }
    fun getAlbum(url: String) = withApi { it.albums.getAlbum(extractIdFromUrl(url)) }
    fun getArtist(url: String) = withApi { it.artists.getArtist(extractIdFromUrl(url)) }
    fun getArtistTracks(url: String) = withApi { it.artists.getArtistTopTracks(extractIdFromUrl(url)) }
    fun shutdown() { api?.spotifyApiOptions?.automaticRefresh = false }

    fun openCredentialsUpdateDialog(component: UIComponent) {
        component.dialog("Update Spotify credentials", Modality.APPLICATION_MODAL, StageStyle.DECORATED) {
            prefWidth = 400.0

            val creds = getCredentials()
            val id = stringProperty(creds.first)
            val secret = stringProperty(creds.second)

            field("Application ID") {
                textfield(id)
            }

            field("Application Secret") {
                passwordfield(secret)
            }

            button("Save") {
                action {
                    updateCredentials(id.value, secret.value)
                    reloadApi()
                    close()
                }
            }
        }
    }

    fun areCredentialsValid() = validCredentials

    fun updateCredentials(appId: String, appSecret: String) {
        with(config) {
            set("i" to appId)
            set("s" to appSecret)
            runCatching(::save).exceptionOrNull()?.let { LOG.error("Config save failed", it) }
        }
    }

    private fun getCredentials(): Pair<String, String> {
        return config.string("i", "foo") to config.string("s", "far")
    }

    fun reloadApi() {
        with(config) {
            createApi(
                string("i", "foo"),
                string("s", "far")
            )
        }
    }

    private fun createApi(appId: String, appSecret: String) {
        if (!AUTH_PATTERN.matcher(appId).matches() || !AUTH_PATTERN.matcher(appSecret).matches()) {
            validCredentials = false
            return LOG.warn("Invalid Spotify credentials")
        }

        try {
            api = spotifyAppApi(appId, appSecret) {
                with(options) {
                    testTokenValidity = true
                    afterTokenRefresh = {
                        log.info("Refreshed Spotify token, next refresh in ${it.token.expiresIn} seconds")
                    }
                }
            }.buildRestAction().complete()
            validCredentials = true
            LOG.info("Spotify API successfully authorised")
        } catch (e: Throwable) {
            validCredentials = false
            LOG.warn("Spotify login failed", e)
        }
    }

    private fun <T> withApi(block: suspend (SpotifyApi<*,*>) -> T): Result<T> {
        val funApi = api
        return if (funApi != null) {
            try {
                runBlockingOnJvmAndNative {
                    Result.success(block(funApi))
                }
            } catch (e: Throwable) {
                LOG.warn("Request to API failed", e)
                Result.failure(e)
            }
        } else {
            Result.failure(RejectedExecutionException())
        }
    }

    private fun extractIdFromUrl(url: String): String {
        val matcher = URL_PATTERN.matcher(url)

        if (!matcher.find()) {
            throw IllegalArgumentException("Invalid URL")
        }

        return matcher.group(3)
    }

    companion object {
        private val LOG by SLF4J[SpotifyApiManager::class]
        val AUTH_PATTERN: Pattern = Pattern.compile("[a-z\\d]{32}")
        val URL_PATTERN: Pattern = Pattern.compile("^(?:https?://(?:open\\.)?spotify\\.com|spotify)([/:])(track|artist|playlist|album)\\1([a-zA-Z\\d]+)")
    }
}