package com.example.posedetector.helper

import android.app.Activity
import android.content.Context
import com.example.posedetector.R
import com.google.mlkit.vision.pose.PoseLandmark
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt


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


fun getAngle(
    firstPoint: PoseLandmark?,
    midPoint: PoseLandmark?,
    lastPoint: PoseLandmark?
): Int {
    var result = 0
    lastPoint?.let { lastPt ->
        firstPoint?.let { firstPt ->
            midPoint?.let { midPt ->
                result = Math.toDegrees(
                    (
                            atan2(
                                lastPt.position.y - midPt.position.y,
                                lastPt.position.x - midPt.position.x
                            )
                                    - atan2(
                                firstPt.position.y - midPt.position.y,
                                firstPt.position.x - midPt.position.x
                            )
                            ).toDouble()
                ).roundToInt()

                result = abs(result) // Angle should never be negative
                if (result > 180) {
                    result =
                        (360 - result)// Always get the acute representation of the angle
                }
            }
        }
    }

    return result
}
