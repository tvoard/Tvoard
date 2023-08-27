package org.example.tvoard.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import org.example.tvoard.R
import org.example.tvoard.common.KEYBOARD_HEIGHT_MULTIPLIER_LARGE
import org.example.tvoard.common.KEYBOARD_HEIGHT_MULTIPLIER_MEDIUM
import org.example.tvoard.common.KEYBOARD_HEIGHT_MULTIPLIER_SMALL
import org.example.tvoard.common.config
import org.example.tvoard.common.openDeviceSettings
import org.example.tvoard.databinding.ActivitySettingsBinding
import java.util.Locale

class SettingsActivity : Activity() {

    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        setupLanguage()
        setupKeyboardLanguage()
        setupKeyboardHeightMultiplier()
    }

    private fun setupLanguage() {
        binding.settingsLanguage.text = Locale.getDefault().displayLanguage
        binding.settingsLanguage.setOnClickListener {
            try {
                Intent(Settings.ACTION_APP_LOCALE_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                    startActivity(this)
                }
            } catch (e: Exception) {
                openDeviceSettings()
            }
        }
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

    private fun setupVibrateOnKeypress() {
        binding.settingsVibrateOnKeypress.isChecked = config.vibrateOnKeypress
        binding.settingsVibrateOnKeypressHolder.setOnClickListener {
            binding.settingsVibrateOnKeypress.toggle()
            config.vibrateOnKeypress = binding.settingsVibrateOnKeypress.isChecked
        }
    }

    private fun setupShowPopupOnKeypress() {
        binding.settingsShowPopupOnKeypress.isChecked = config.showPopupOnKeypress
        binding.settingsShowPopupOnKeypressHolder.setOnClickListener {
            binding.settingsShowPopupOnKeypress.toggle()
            config.showPopupOnKeypress = binding.settingsShowPopupOnKeypress.isChecked
        }
    }

    private fun setupShowKeyBorders() {
        binding.settingsShowKeyBorders.isChecked = config.showKeyBorders
        binding.settingsShowKeyBordersHolder.setOnClickListener {
            binding.settingsShowKeyBorders.toggle()
            config.showKeyBorders = binding.settingsShowKeyBorders.isChecked
        }
    }

    private fun setupShowNumbersRow() {
        binding.settingsShowNumbersRow.isChecked = config.showNumbersRow
        binding.settingsShowNumbersRowHolder.setOnClickListener {
            binding.settingsShowNumbersRow.toggle()
            config.showNumbersRow = binding.settingsShowNumbersRow.isChecked
        }
    }
}
