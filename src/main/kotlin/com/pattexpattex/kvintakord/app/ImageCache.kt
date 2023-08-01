package com.pattexpattex.kvintakord.app

import javafx.scene.image.Image

object ImageCache {
    private val cachedImages = LimitedHashSet<Image>(100)

    fun getImage(url: String?): Image? {
        if (url == null) {
            return null
        }

        return cachedImages.find { it.url == url }
            ?: Image(url, .0, 40.0, true, true, true)
                .also { cachedImages.add(it) }
    }
}