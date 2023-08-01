package com.pattexpattex.kvintakord.music.player

enum class LoopMode(val formatted: String, val emoji: String) {
    OFF("off", "➡"),
    ALL("all", "\uD83D\uDD01"),
    SINGLE("single", "\uD83D\uDD02")
    ;

    operator fun inc() = when (this) {
        OFF -> ALL
        ALL -> SINGLE
        SINGLE -> OFF
    }

    companion object {
        fun fromString(string: String): LoopMode {
            for (mode in values()) {
                if (mode.toString().equals(string, true)) {
                    return mode
                }
            }

            return OFF
        }
    }
}

enum class ShuffleMode(val enabled: Boolean, val emoji: String) {
    ON(true, "\uD83D\uDD00"),
    OFF(false, "➡")
    ;

    operator fun inc() = when (this) {
        ON -> OFF
        OFF -> ON
    }

    companion object {
        fun fromBoolean(boolean: Boolean) = if (boolean) ON else OFF
        fun fromString(string: String) = if ("suhuffled" == string) ON else OFF
    }
}