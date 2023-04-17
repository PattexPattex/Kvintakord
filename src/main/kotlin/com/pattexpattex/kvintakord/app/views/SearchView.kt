package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.fragments.AudioTrackCell
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.pattexpattex.kvintakord.music.spotify.SpotifyApiManager
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.Alert
import javafx.scene.control.ButtonType
import javafx.scene.input.KeyCode
import javafx.scene.layout.Priority
import tornadofx.*

class SearchView : View() {
    val player = find<PlayerManager>()
    private val input = stringProperty()
    private val source = stringProperty(player.searchableSources.keys.first())
    private var resultsSource = stringProperty(source.value)
    private val results = listProperty(arrayListOf<AudioTrack>().asObservable())
    private val resultsQuery = stringProperty()

    override val root = borderpane {
        top = vbox {
            form {
                fieldset {
                    hbox {
                        spacing = 5.0

                        prefWidthProperty().bind(this@borderpane.widthProperty())

                        textfield(input) {
                            hgrow = Priority.ALWAYS
                            prefHeight = 40.0
                            minWidth = 100.0
                            setOnKeyPressed {
                                if (it.code == KeyCode.ENTER) {
                                    handleSearchAction()
                                }
                            }
                        }

                        button("Search") {
                            prefHeight = 40.0
                            action { handleSearchAction() }
                        }.addClass(Style.SearchViewDoButton)
                    }

                }

                borderpane {
                    left = hbox {
                        label("Search source:").addClass(Style.SearchViewSourceLabel)

                        togglegroup(source as ObservableValue<Any>) {
                            spacing = 10.0
                            player.searchableSources.forEach {
                                radiobutton(it.key, group = this@togglegroup, value = it.key) {
                                    alignment = Pos.CENTER
                                }.addClass(Style.SearchViewSourceButton)
                            }

                            selectToggle(toggles[0])
                        }
                    }

                    right = button("Update Spotify credentials") {
                        action {
                            find<SpotifyApiManager>().openCredentialsUpdateDialog(this@SearchView)
                        }
                    }.addClass(Style.SearchViewSpotifyAuthButton)
                }
            }
        }

        center = vbox {
            prefWidthProperty().bind(this@borderpane.widthProperty())
            label(resultsQuery) {
                paddingAll = 10
            }.addClass(Style.SearchViewResultsQueryLabel)

            listview<AudioTrack>(results) {
                setCellFactory { AudioTrackCell().addClass(Style.SearchTrackCell) }
                prefHeightProperty().bind(items.sizeProperty.times(46.0).plus(2))

                placeholder = label("Nothing here.") {
                    alignment = Pos.CENTER
                }.addClass(Style.SearchViewNoResultsLabel)
            }.addClass(Style.QueueNextTrackListView)
        }

    }.addClass(Style.SearchView)

    private fun handleSearchAction() {
        if (input.valueSafe.isEmpty()) {
            return
        }

        resultsSource.set(source.value)
        player.search(input.valueSafe, source.value).thenAccept {
            runLater {
                resultsQuery.set(it.first)
                results.setAll(it.second)
            }
        }.exceptionally {
            runLater {
                handleSearchException(it.cause ?: it)
            }
            null
        }
        input.value = null
    }

    private fun handleSearchException(throwable: Throwable) {
        if (throwable is NullPointerException) {
            val apiManager = find<SpotifyApiManager>()
            val showSpotifyAuth = resultsSource.value == "Spotify" && !apiManager.areCredentialsValid()

            alert(
                type = Alert.AlertType.INFORMATION,
                header = throwable.message ?: "No results.",
                content = if (showSpotifyAuth) { "You are trying to use Spotify, are you sure that you are logged in?" } else null,
                buttons = if (showSpotifyAuth) { arrayOf(OPEN_SPOTIFY_CREDENTIALS_BUTTON, ButtonType.CLOSE) } else arrayOf(ButtonType.CLOSE),
                title = "Whoops."
            ) {
                if (it.text == OPEN_SPOTIFY_CREDENTIALS_BUTTON.text) {
                    apiManager.openCredentialsUpdateDialog(this@SearchView)
                }

                close()
            }
        } else {
            val (header, content) = if (throwable is FriendlyException) {
                throwable.message to throwable.cause?.toString()
            } else {
                throwable.message to throwable.toString()
            }

            alert(
                type = Alert.AlertType.WARNING,
                header = header ?: "Something went wrong.",
                content = content,
                title = "Whoops."
            )
        }
    }

    companion object {
        private val OPEN_SPOTIFY_CREDENTIALS_BUTTON = ButtonType("Check credentials")
    }
}