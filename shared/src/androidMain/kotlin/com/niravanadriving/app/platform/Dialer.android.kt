package com.niravanadriving.app.platform

import android.content.Context
import android.content.Intent
import android.net.Uri

object AndroidContextHolder {
    lateinit var appContext: Context
}

actual fun openDialer(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    AndroidContextHolder.appContext.startActivity(intent)
}
