package com.example.posedetector.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AngleInfo(
    var angleName: String? = null,
    var angleValue: Int? = null
) : Parcelable {
}