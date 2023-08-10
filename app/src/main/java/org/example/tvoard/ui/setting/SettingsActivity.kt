package org.example.tvoard.ui.setting

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.annotation.ChecksSdkIntAtLeast
import org.example.tvoard.R
import org.example.tvoard.common.KEYBOARD_HEIGHT_MULTIPLIER_LARGE
import org.example.tvoard.common.KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM
import org.example.tvoard.common.KEYBOARD_HEIGHT_MULTIPLIER_SMALL

class SettingsActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
    }

    override fun onResume() {
        super.onResume()

        setupLanguage()
        setupKeyboardLanguage()
        setupKeyboardHeightMultiplier()
    }

    private fun setupLanguage() {
        @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
        fun isNougatPlus() = Build.VERSION.SDK_INT >= 24
    }

    private fun setupKeyboardLanguage() {
    }

    private fun setupKeyboardHeightMultiplier() {
    }

    private fun getKeyboardHeightMultiplierText(multiplier: Int): String {
        return when (multiplier) {
            KEYBOARD_HEIGHT_MULTIPLIER_SMALL -> getString(R.string.small)
            KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM -> getString(R.string.medium)
            KEYBOARD_HEIGHT_MULTIPLIER_LARGE -> getString(R.string.large)
            else -> getString(R.string.small)
        }
    }
}
