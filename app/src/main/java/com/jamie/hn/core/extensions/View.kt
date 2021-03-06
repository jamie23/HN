package com.jamie.hn.core.extensions

import android.view.View

fun View.visibleOrInvisible(visible: Boolean) {
    visibility = when (visible) {
        true -> View.VISIBLE
        false -> View.INVISIBLE
    }
}

fun View.visibleOrGone(visible: Boolean) {
    visibility = when (visible) {
        true -> View.VISIBLE
        false -> View.GONE
    }
}
