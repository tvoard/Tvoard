package org.example.tvoard.common

import android.content.Context
import org.example.tvoard.R

class Config(context: Context) {
    fun Context.getStrokeColor(): Int {
        return resources.getColor(R.color.md_grey_800, theme)
    }

    fun Context.getSharedPrefs() = getSharedPreferences(PREFS_KEY, Context.MODE_PRIVATE)

    protected val prefs = context.getSharedPrefs()
}