package com.pattexpattex.kvintakord.app.views.trackcontrol

import com.pattexpattex.kvintakord.app.Style
import javafx.geometry.Pos
import tornadofx.View
import tornadofx.addClass
import tornadofx.hbox

class TrackControlView : View("TrackControls") {
    override val root = hbox {
        addClass(Style.TrackControl)
        alignment = Pos.CENTER
        prefHeight = 100.0

        add(find<CurrentTrackTitleView>())
        add(find<CurrentTrackControlView>())
        add(find<SoundControlView>())
    }
}
