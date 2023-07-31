package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.fragments.AudioTrackCell
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.pattexpattex.kvintakord.music.player.SearchManager
import com.pattexpattex.kvintakord.music.spotify.SpotifyApiManager
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import tornadofx.*

class SearchView : View() {
    val player = find<PlayerManager>()
    private val searchManager = find<SearchManager>()

    override val root = borderpane {
        top = vbox {
            form {
                fieldset {
                    hbox {
                        spacing = 5.0

                        prefWidthProperty().bind(this@borderpane.widthProperty())

                        searchManager.inputField = textfield {
                            hgrow = Priority.ALWAYS
                            prefHeight = 40.0
                            minWidth = 100.0

                            action { searchManager.searchAndDisplay() }
                        }

                        button("Search") {
                            prefHeight = 40.0
                            action { searchManager.searchAndDisplay() }
                        }.addClass(Style.SearchViewDoButton)
                    }

                }

                borderpane {
                    left = hbox {
                        label("Search source:").addClass(Style.SearchViewSourceLabel)

                        searchManager.sourceToggleGroup = togglegroup {
                            spacing = 10.0

                            toggles.setAll(SearchManager.SOURCES.mapEach {
                                    radiobutton(key, value = value) { alignment = Pos.CENTER }
                                        .addClass(Style.SearchViewSourceButton)
                            })

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
            label(searchManager.currentResultsQueryProperty) {
                paddingAll = 10
            }.addClass(Style.SearchViewResultsQueryLabel)

            listview(searchManager.currentResults) {
                setCellFactory { AudioTrackCell().addClass(Style.SearchTrackCell) }

                prefHeightProperty().bind(items.sizeProperty.times(46.0).plus(2))

                placeholder = label("Nothing here.") {
                    alignment = Pos.CENTER
                }.addClass(Style.SearchViewNoResultsLabel)
            }.addClass(Style.QueueNextTrackListView)
        }

    }.addClass(Style.SearchView)

    /*private fun handleSearchException(throwable: Throwable) {
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
    }*/

    companion object {
        private val OPEN_SPOTIFY_CREDENTIALS_BUTTON = ButtonType("Check credentials")
    }
}