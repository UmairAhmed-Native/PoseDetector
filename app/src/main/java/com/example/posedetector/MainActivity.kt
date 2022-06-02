package com.example.posedetector

import android.content.Intent
import android.os.Bundle
import com.example.posedetector.databinding.ActivityMainBinding

class MainActivity : PermissionActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDetectOnStillImage.setOnClickListener {
            checkWriteReadCameraPermissions {
                navigateToStillImageDetection()
            }
        }

        binding.btnDetectOnLivePreview.setOnClickListener {
            checkWriteReadCameraPermissions {
                navigateToLivePreviewDetection()
            }

        }
    }

    private fun navigateToLivePreviewDetection() {
        startActivity(
            Intent(
                this@MainActivity,
                LivePreviewActivity::class.java
            )
        )
    }

    private fun navigateToStillImageDetection() {
        startActivity(
            Intent(
                this@MainActivity,
                StillImageDetectionActivity::class.java
            )
        )
    }

}