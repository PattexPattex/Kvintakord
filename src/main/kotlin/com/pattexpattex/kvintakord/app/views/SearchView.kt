package com.pattexpattex.kvintakord.app.views

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.cell.track.AudioTrackCell
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.pattexpattex.kvintakord.music.player.SearchManager
import com.pattexpattex.kvintakord.music.spotify.SpotifyApiManager
import javafx.geometry.Pos
import javafx.scene.control.ButtonType
import javafx.scene.layout.Priority
import tornadofx.*

class SearchView : View() {
    val player by inject<PlayerManager>()
    private val searchManager by inject<SearchManager>()

    override val root = borderpane {
        addClass(Style.SearchView)

        top = vbox {
            form {
                fieldset {
                    hbox {
                        spacing = 5.0

                        prefWidthProperty().bind(this@borderpane.widthProperty())

                        textfield(searchManager.queryProperty) {
                            hgrow = Priority.ALWAYS
                            prefHeight = 40.0
                            minWidth = 100.0

                            action { searchManager.searchAndDisplay() }
                        }

                        button("Search") {
                            addClass(Style.SearchViewDoButton)
                            prefHeight = 40.0
                            action { searchManager.searchAndDisplay() }
                        }
                    }
                }

                borderpane {
                    left = hbox {
                        label("Search source:").addClass(Style.SearchViewSourceLabel)

                        togglegroup {
                            spacing = 10.0

                            for ((key, _) in SearchManager.SOURCES) {
                                radiobutton(key, this, key) {
                                    addClass(Style.SearchViewSourceButton)
                                    alignment = Pos.CENTER
                                }
                            }

                            selectToggle(toggles[0])

                            bind(searchManager.sourceProperty)
                        }
                    }

                    right = button("Update Spotify credentials") {
                        addClass(Style.SearchViewSpotifyAuthButton)
                        action {
                            find<SpotifyApiManager>().openCredentialsUpdateDialog(this@SearchView)
                        }
                    }
                }
            }
        }

        center = vbox {
            prefWidthProperty().bind(this@borderpane.widthProperty())

            label(searchManager.currentResultsQueryProperty) {
                addClass(Style.SearchViewResultsQueryLabel)
                paddingAll = 10
            }

            listview(searchManager.currentResults) {
                addClass(Style.QueueNextTrackListView)
                setCellFactory { AudioTrackCell().addClass(Style.SearchTrackCell) }

                prefHeightProperty().bind(items.sizeProperty.times(46.0).plus(2))

                placeholder = label("Nothing here.") {
                    addClass(Style.SearchViewNoResultsLabel)
                    alignment = Pos.CENTER
                }
            }
        }

    }

    /*private fun handleSearchException(throwable: Throwable) {
        if (throwable is NullPointerException) {
            val apiManager by inject<SpotifyApiManager>()
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