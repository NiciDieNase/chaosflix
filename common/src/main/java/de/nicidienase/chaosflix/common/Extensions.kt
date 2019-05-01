package de.nicidienase.chaosflix.common

import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity

fun Fragment.checkPermission(permission: String, requestCode: Int, action: () -> Unit) {
    if (ContextCompat.checkSelfPermission(requireContext(), permission)
            != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(permission),
                requestCode)
    } else {
        action()
    }
}

fun AppCompatActivity.checkPermission(permission: String, requestCode: Int, action: () -> Unit) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
            ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED) {
        requestPermissions(arrayOf(permission),
                    requestCode)
    } else {
        action()
    }
}