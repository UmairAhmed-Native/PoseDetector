package com.example.posedetector.helper.permission

import android.Manifest
import android.R
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.text.Html
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat

class PermissionHelper(private val activity: Activity) {

    private var arrayListPermission: ArrayList<PermissionBean>? = null
    private lateinit var arrayPermissions: Array<String?>
    private var runTimePermissionListener: RunTimePermissionListener? = null

    fun requestPermission(
        permissions: Array<String>,
        runTimePermissionListener: RunTimePermissionListener?
    ) {
        this.runTimePermissionListener = runTimePermissionListener
        arrayListPermission = ArrayList()
        for (permission in permissions) {
            val permissionBean = PermissionBean()
            if (ContextCompat.checkSelfPermission(
                    activity,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                permissionBean.isAccept = true
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && permission == Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                    permissionBean.isAccept = true
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && permission == Manifest.permission.MANAGE_EXTERNAL_STORAGE) {
                    if (Environment.isExternalStorageManager()) {
                        permissionBean.isAccept = true
                    } else {
                        permissionBean.isAccept = false
                        permissionBean.permission = permission
                        arrayListPermission!!.add(permissionBean)
                        callManageAccessActivity()
                    }
                } else {
                    permissionBean.isAccept = false
                    permissionBean.permission = permission
                    arrayListPermission!!.add(permissionBean)
                }
            }
        }
        if (arrayListPermission!!.size <= 0) {
            runTimePermissionListener!!.permissionGranted()
            return
        }
        arrayPermissions = arrayOfNulls(arrayListPermission!!.size)
        for (i in arrayListPermission!!.indices) {
            arrayPermissions[i] = arrayListPermission!![i].permission
        }
        activity.requestPermissions(arrayPermissions, 10)
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private fun callManageAccessActivity() {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    private fun callSettingActivity() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    private fun checkUpdate() {
        var isGranted = true
        var deniedCount = 0
        for (i in arrayListPermission!!.indices) {
            if (!arrayListPermission!![i].isAccept) {
                isGranted = false
                deniedCount++
            }
        }
        if (isGranted) {
            if (runTimePermissionListener != null) {
                runTimePermissionListener!!.permissionGranted()
            }
        } else {
            if (runTimePermissionListener != null) {
                runTimePermissionListener!!.permissionDenied()
            }
        }
    }

    private fun updatePermissionResult(permissions: String, grantResults: Int) {
        for (i in arrayListPermission!!.indices) {
            if (arrayListPermission!![i].permission == permissions) {
                arrayListPermission!![i].isAccept = grantResults == 0
                break
            }
        }
    }


    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        for (i in permissions.indices) {
            updatePermissionResult(permissions[i], grantResults[i])
        }
        checkUpdate()
    }


}