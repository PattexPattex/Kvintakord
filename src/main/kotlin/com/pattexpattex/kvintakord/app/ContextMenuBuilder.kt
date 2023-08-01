package com.pattexpattex.kvintakord.app

import com.pattexpattex.kvintakord.music.adapter.AudioTrackAdapter
import com.pattexpattex.kvintakord.music.player.QueueManager
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

    fun currentTrack(target: EventTarget, track: AudioTrackAdapter) {
        target.contextmenu {

        }
    }

    fun queuedTrack(target: EventTarget, track: AudioTrackAdapter) {
        target.contextmenu {
            item("Play now") {
                action {
                    queueManager.skipToTrack(queueManager.queue.indexOf(track))
                }
            }
            item("Remove") {
                action {
                    queueManager.removeFromQueue(track)
                }
            }
        }.style {
            backgroundColor += Style.Colors.SyntaxBG
            baseColor = Style.Colors.SyntaxBG
            borderColor += box(Style.Colors.SyntaxFG)
        }
    }

    fun searchTrack(target: EventTarget, track: AudioTrackAdapter) {
        target.contextmenu {
            item("Play now") {
                action {
                    queueManager.playNow(track)
                }
            }
            item("Add to queue") {
                action {
                    queueManager.addToQueue(track)
                }
            }
        }
    }

    private val queueManager get() = find<QueueManager>()
}