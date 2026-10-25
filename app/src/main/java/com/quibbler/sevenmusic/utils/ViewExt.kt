package com.quibbler.sevenmusic.utils

import android.view.View

/**
 * Extension functions for Android View classes.
 * Simplifies common UI operations in activities and fragments.
 */

/**
 * Set the visibility of a view to VISIBLE or GONE based on a boolean condition.
 *
 * @param visible true to show, false to hide
 */
fun View.visibleOrGone(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.GONE
}

/**
 * Set the visibility of a view to VISIBLE or INVISIBLE based on a boolean condition.
 *
 * @param visible true to show, false to make invisible
 */
fun View.visibleOrInvisible(visible: Boolean) {
    visibility = if (visible) View.VISIBLE else View.INVISIBLE
}

/**
 * Execute an action if the view is not null.
 * Useful for safely operating on optional views.
 *
 * @param action the block to execute with the view as receiver
 */
fun View?.safeAction(action: View.() -> Unit) {
    this?.action()
}

/**
 * Debounce rapid click events on a view.
 * Prevents accidental double-taps on buttons and interactive elements.
 *
 * @param delayMs the minimum interval between click events in milliseconds
 * @param action the click action to perform
 */
fun View.setDebouncedOnClickListener(delayMs: Long = 500, action: (View) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener { v ->
        val now = System.currentTimeMillis()
        if (now - lastClickTime >= delayMs) {
            lastClickTime = now
            action(v)
        }
    }
}
