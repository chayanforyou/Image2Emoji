package me.chayan.image2emoji.utils

import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils


object AnimationUtil {

    fun start(view: View, id: Int, startOffset: Long) {
        val loadAnimation: Animation = AnimationUtils.loadAnimation(view.context, id)
        loadAnimation.startOffset = startOffset
        view.startAnimation(loadAnimation)
    }
}