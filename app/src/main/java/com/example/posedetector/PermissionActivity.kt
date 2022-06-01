package com.example.posedetector

import android.Manifest
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.posedetector.helper.permission.PermissionHelper
import com.example.posedetector.helper.permission.RunTimePermissionListener

open class PermissionActivity : AppCompatActivity() {

    private lateinit var permissionHelper: PermissionHelper

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

    }

    fun checkWriteReadCameraPermissions(kFunction0: () -> Unit) {
        permissionHelper = PermissionHelper(this)
        permissionHelper.requestPermission(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
            ), object : RunTimePermissionListener {

                override
                fun permissionGranted() {

                    kFunction0()
                }

                override
                fun permissionDenied() {

                }
            })

    }
}