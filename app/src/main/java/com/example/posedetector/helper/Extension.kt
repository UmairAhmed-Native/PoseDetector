package com.example.posedetector.helper

import android.app.Activity
import android.content.Context
import com.example.posedetector.R
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


fun formatDateToString(time: Date?, formate: String): String {

    val sdf = SimpleDateFormat(formate)
    val selectedDate = sdf.format(time)
    return selectedDate
}

fun currentDate(): Date {
    val date = Calendar.getInstance()
    return date.time
}

fun getOutputDirectory(context: Context, activity: Activity): File {
    val appContext = context.applicationContext
    val mediaDir = activity.externalMediaDirs.firstOrNull()?.let {
        File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
    }
    return if(mediaDir != null && mediaDir.exists())
        mediaDir else appContext.filesDir
}
