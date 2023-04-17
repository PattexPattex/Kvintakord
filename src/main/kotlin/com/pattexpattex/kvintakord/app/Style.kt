package com.pattexpattex.kvintakord.app

import javafx.geometry.Pos
import javafx.scene.paint.Color
import javafx.scene.paint.CycleMethod
import javafx.scene.paint.LinearGradient
import javafx.scene.paint.Paint
import javafx.scene.paint.RadialGradient
import javafx.scene.paint.Stop
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.transform.Transform
import tornadofx.*

class Style : Stylesheet() {
    companion object {
        //val GenericContextMenu by cssclass()
        //val GenericContextMenuItem by cssclass()
        val GenericTrackNameLabel by cssclass()
        val GenericTrackAuthorLabel by cssclass()

        val TrackControlMixerCombobox by cssclass()
        val TrackControlButton by cssclass()
        val TrackControlTimeLabel by cssclass()
        val TrackControl by cssclass()

        val ViewSelectionTabPane by cssclass()

        val QueueView by cssclass()
        val TrackCell by cssclass()
        val CurrentTrackCell by cssclass()
        val QueuedTrackCell by cssclass()
        val SearchTrackCell by cssclass()
        val TrackCellButton by cssclass()
        val QueueCurrentTrackLabel by cssclass()
        val QueueCurrentTrackListView by cssclass()
        val QueueNextTrackLabel by cssclass()
        val QueueNextTrackListView by cssclass()
        val QueueListViewPlaceholder by cssclass()

        val SearchView by cssclass()
        val SearchViewDoButton by cssclass()
        val SearchViewSpotifyAuthButton by cssclass()
        val SearchViewNoResultsLabel by cssclass()
        val SearchViewResultsQueryLabel by cssclass()
        val SearchViewSourceLabel by cssclass()
        val SearchViewSourceButton by cssclass()
    }

    object Colors {
        val Mono1 =         "#abb2bf".asColor()
        val Mono2 =         "#828997".asColor()
        val Mono3 =         "#5c6370".asColor()

        val White =         "#b4b7b8".asColor()
        val Cyan =          "#56b6c2".asColor()
        val Blue =          "#61afef".asColor()
        val Purple =        "#c678dd".asColor()
        val Green =         "#98c379".asColor()
        val Red1 =          "#e06c75".asColor()
        val Red2 =          "#be5046".asColor()
        val Orange1 =       "#d19a66".asColor()
        val Orange2 =       "#e5c07b".asColor()

        val SyntaxFG =      "#abb2bf".asColor()
        val SyntaxBG =      "#282c34".asColor()
        val SyntaxGutter =  "#636d83".asColor()
        val SyntaxGuide =   "#abb2bf".asColor()
        val SyntaxAccent =  "#528bff".asColor()

        fun all() = listOf(Mono1, Mono2, Mono3, Cyan, Blue, Purple, Green, Red1, Red2, Orange1, Orange2, SyntaxFG, SyntaxBG, SyntaxGutter, SyntaxGuide, SyntaxAccent)
        private fun String.asColor(): Color = Color.web(this)
    }

    init {
        root {
            backgroundColor += Colors.SyntaxBG
            baseColor = Colors.SyntaxBG
        }

        button {
            textFill = Colors.SyntaxFG
        }

        tab {
            textFill = Colors.SyntaxFG
            backgroundColor += Colors.SyntaxBG

            and(selected) {
                fontWeight = FontWeight.BOLD
                backgroundColor += Colors.SyntaxBG
            }
        }

        ViewSelectionTabPane {
            //textFill = Colors.SyntaxFG
            //fontSize = 14.px
        }

        contextMenu {
            backgroundColor += Colors.SyntaxBG
            baseColor = Colors.SyntaxBG
            borderColor += box(Colors.SyntaxGutter)
            textFill = Colors.SyntaxFG
        }

        TrackControl {
            backgroundColor += LinearGradient(0.0, 100.0, 0.0, 0.0, false, CycleMethod.NO_CYCLE,
                Stop(1.0, Colors.Mono3.darker()), Stop(.0, Colors.SyntaxBG))
            baseColor = Colors.SyntaxBG

            TrackControlButton {
                alignment = Pos.CENTER
                prefWidth = 40.px
                textFill = Colors.SyntaxFG
            }

            TrackControlMixerCombobox {
                prefWidth = 140.px
            }

            TrackControlTimeLabel {
                textFill = Colors.SyntaxFG
            }
        }

        QueueView {}

        GenericTrackNameLabel {
            textFill = Colors.SyntaxFG
            fontWeight = FontWeight.BOLD
        }

        GenericTrackAuthorLabel {
            textFill = Colors.SyntaxFG
        }

        QueueCurrentTrackListView {
            backgroundColor += Colors.SyntaxGutter.darker()

            TrackCell {
                backgroundColor += Colors.SyntaxGutter.darker()
            }

            and(hover) {
                backgroundColor += Colors.SyntaxGutter
            }
        }

        QueueCurrentTrackLabel {
            fontSize = 16.px
            fontWeight = FontWeight.BOLD
            textFill = Colors.SyntaxFG
        }

        QueueNextTrackListView {
            backgroundColor += Colors.SyntaxBG
        }

        QueueNextTrackLabel {
            fontSize = 16.px
            textFill = Colors.SyntaxFG
        }

        TrackCell {
            backgroundColor += Colors.SyntaxBG
            baseColor = Colors.SyntaxBG

            label {
                textFill = Colors.SyntaxFG
            }

            TrackCellButton {
                alignment = Pos.CENTER
                prefWidth = 40.px
                textFill = Colors.SyntaxFG
            }

            and(hover) {
                backgroundColor += Colors.SyntaxGutter
                baseColor = Colors.SyntaxGutter

                s(hyperlink, label) {
                    textFill = Colors.SyntaxFG.brighter()
                }

                TrackCellButton {
                    textFill = Colors.SyntaxFG.brighter()
                }
            }
        }

        QueueListViewPlaceholder {
            fontStyle = FontPosture.ITALIC
            textFill = Colors.SyntaxGutter
        }

        SearchView {}

        SearchViewDoButton {
            fontWeight = FontWeight.BOLD
        }

        SearchViewSpotifyAuthButton {
            //fontStyle = FontPosture.ITALIC
            textFill = Colors.Green
            //textFill = LinearGradient(0.0, 0.0, 200.0, 0.0, false, CycleMethod.NO_CYCLE, Stop(1.0, Colors.Green), Stop(.0, Colors.SyntaxFG))
        }

        SearchViewSourceLabel {
            fontWeight = FontWeight.BOLD
            textFill = Colors.SyntaxFG
        }

        SearchViewSourceButton {
            textFill = Colors.SyntaxFG

            and(selected) {
                fontWeight = FontWeight.BOLD
                //textFill = Colors.SyntaxFG.brighter()
            }
        }

        SearchViewResultsQueryLabel {
            fontSize = 20.px
            fontWeight = FontWeight.BOLD
            textFill = Colors.SyntaxFG.brighter()
        }

        SearchViewNoResultsLabel {
            fontStyle = FontPosture.ITALIC
            textFill = Colors.SyntaxGutter
        }
    }
}