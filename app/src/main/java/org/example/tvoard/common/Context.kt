package org.example.tvoard.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.UserManager
import android.provider.Settings
import android.widget.Toast

val Context.config: Config get() = Config.newInstance(applicationContext.safeStorageContext)

val Context.safeStorageContext: Context
    get() = if (isDeviceInDirectBootMode) {
        createDeviceProtectedStorageContext()
    } else {
        this
    }

val Context.isDeviceInDirectBootMode: Boolean
    get() {
        val userManager = getSystemService(Context.USER_SERVICE) as UserManager
        return !userManager.isUserUnlocked
    }

fun Context.openDeviceSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }

    try {
        startActivity(intent)
    } catch (e: Exception) {
        showErrorToast(e)
    }
}

fun Context.toast(msg: String, length: Int = Toast.LENGTH_SHORT) {
    try {
//        if (isOnMainThread()) {
            doToast(this, msg, length)
//        } else {
//            Handler(Looper.getMainLooper()).post {
//                doToast(this, msg, length)
//            }
//        }
    } catch (_: Exception) {
    }
}

private fun doToast(context: Context, message: String, length: Int) {
    if (context is Activity) {
        if (!context.isFinishing && !context.isDestroyed) {
            Toast.makeText(context, message, length).show()
        }
    } else {
        Toast.makeText(context, message, length).show()
    }
}

fun Context.showErrorToast(msg: String, length: Int = Toast.LENGTH_LONG) {
    toast(String.format(getString(org.example.tvoard.R.string.error), msg), length)
}

fun Context.showErrorToast(exception: Exception, length: Int = Toast.LENGTH_LONG) {
    showErrorToast(exception.toString(), length)
}
