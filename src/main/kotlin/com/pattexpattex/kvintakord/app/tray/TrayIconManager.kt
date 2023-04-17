package com.pattexpattex.kvintakord.app.tray

import com.dustinredmond.fxtrayicon.FXTrayIcon
import com.pattexpattex.kvintakord.music.player.Executors
import com.pattexpattex.kvintakord.music.player.LoopMode
import com.pattexpattex.kvintakord.music.player.PlayerManager
import com.pattexpattex.kvintakord.music.player.ShuffleMode
import javafx.application.Platform
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.CheckMenuItem
import javafx.scene.control.MenuItem
import tornadofx.Controller
import tornadofx.onChange
import tornadofx.runLater
import java.awt.CheckboxMenuItem
import java.awt.Menu

class TrayIconManager : Controller() {
    private val player by lazy { find<PlayerManager>() }

    private val trayIcon by lazy {
        FXTrayIcon.Builder(primaryStage, TrayIconManager::class.java.getResource("/icon.png")).apply {
            toolTip("Kvintakord")
            menuItem("Pause track") { updatePauseMenuItem(player.togglePaused()) }
            menuItem("Skip track") { player.musicManager.skipTrack() }
            menuItem("Restart track") { player.musicManager.restartTrackProgress() }
            separator()
            menu("Loop",
                innerCheckMenuItem("Off") { player.musicManager.setLoop(LoopMode.OFF); updateLoopMenuItems(LoopMode.OFF) },
                innerCheckMenuItem("All") { player.musicManager.setLoop(LoopMode.ALL); updateLoopMenuItems(LoopMode.ALL) },
                innerCheckMenuItem("Single") { player.musicManager.setLoop(LoopMode.SINGLE); updateLoopMenuItems(LoopMode.SINGLE) }
            )
            checkMenuItem("Shuffle") { player.musicManager.incShuffle(); updateShuffleMenuItem(player.musicManager.shuffleMode.value) }
            separator()
            menuItem("Stop") { player.stop() }
            separator()
            addExitMenuItem("Exit")
        }.build()
    }

    fun create() {
        Platform.setImplicitExit(false)
        primaryStage.setOnCloseRequest {
            primaryStage.hide()
        }

        player.paused.onChange {
            updatePauseMenuItem(it)
        }

        player.musicManager.loopMode.onChange {
            updateLoopMenuItems(it)
        }

        player.musicManager.shuffleMode.onChange {
            updateShuffleMenuItem(it)
        }

        trayIcon.show()

        Executors.scheduledExecutor.execute {
            while (!trayIcon.isShowing) {
                Thread.onSpinWait()
            }

            runLater {
                updatePauseMenuItem(player.paused.value)
                updateLoopMenuItems(player.musicManager.loopMode.value)
                updateShuffleMenuItem(player.musicManager.shuffleMode.value)
            }
        }
    }

    private fun innerMenuItem(label: String, eventHandler: EventHandler<ActionEvent>): MenuItem {
        return MenuItem(label).apply { onAction = eventHandler }
    }

    private fun innerCheckMenuItem(label: String, eventHandler: EventHandler<ActionEvent>): MenuItem {
        return CheckMenuItem(label).apply { onAction = eventHandler }
    }

    private fun updateLoopMenuItems(value: LoopMode?) {
        for (i in 0..2) {
            ((trayIcon.getMenuItem(4) as? Menu)?.getItem(i) as? CheckboxMenuItem)?.apply { state = label.equals(value?.formatted, true) }
        }
    }

    private fun updateShuffleMenuItem(value: ShuffleMode?) {
        (trayIcon.getMenuItem(5) as? CheckboxMenuItem)?.apply { state = value == ShuffleMode.ON }
    }

    private fun updatePauseMenuItem(value: Boolean?) {
        trayIcon.getMenuItem(0).label = if (value == true) "Play track" else "Pause track"
    }
}