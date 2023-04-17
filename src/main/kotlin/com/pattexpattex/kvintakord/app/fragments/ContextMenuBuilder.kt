package com.pattexpattex.kvintakord.app.fragments

import com.pattexpattex.kvintakord.app.Style
import com.pattexpattex.kvintakord.app.openUrl
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import javafx.beans.property.StringProperty
import javafx.beans.value.ObservableValue
import javafx.event.EventTarget
import javafx.scene.control.Hyperlink
import javafx.scene.input.Clipboard
import tornadofx.*


object ContextMenuBuilder {
    fun hyperlink(hyperlink: Hyperlink, uri: String?) {
        hyperlink.contextmenu {
            item("Copy address") {
                action {
                    uri?.let { Clipboard.getSystemClipboard().putString(it) }
                }
            }

            item("Open in browser") {
                action {
                    uri?.let { openUrl(it) }
                }
            }
        }
    }

    fun hyperlink(hyperlink: Hyperlink, uri: ObservableValue<String?>) {
        hyperlink.contextmenu {
            item("Copy address") {
                action {
                    uri.value?.let { Clipboard.getSystemClipboard().putString(it) }
                }
            }

            item("Open in browser") {
                action {
                    uri.value?.let { openUrl(it) }
                }
            }
        }
    }

    fun currentTrack(target: EventTarget, track: AudioTrack) {
        target.contextmenu {

        }
    }

    fun queuedTrack(target: EventTarget, track: AudioTrack) {
        target.contextmenu {
            item("Play now") {
                action {
                    player.musicManager.skipTrack(player.musicManager.queue.indexOf(track))
                }
            }
            item("Remove") {
                action {
                    player.musicManager.removeFromQueue(track)
                }
            }
        }.style {
            backgroundColor += Style.Colors.SyntaxBG
            baseColor = Style.Colors.SyntaxBG
            borderColor += box(Style.Colors.SyntaxFG)
        }
    }

    fun searchTrack(target: EventTarget, track: AudioTrack) {
        target.contextmenu {
            item("Play now") {
                action {
                    player.musicManager.playNow(track)
                }
            }
            item("Add to queue") {
                action {
                    player.musicManager.addToQueue(track)
                }
            }
        }
    }

    private val player get() = find<PlayerManager>()
}