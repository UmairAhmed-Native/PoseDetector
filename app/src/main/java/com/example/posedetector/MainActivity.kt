package com.example.posedetector

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.posedetector.databinding.ActivityMainBinding

class MainActivity : PermissionActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnStart.setOnClickListener {
            checkWriteReadCameraPermissions {
                navigateToStartDetection()
            }
        }
    }

    private fun navigateToStartDetection() {
        startActivity(
            Intent(
                this@MainActivity,
                StartDetectionActivity::class.java
            )
        )
    }

}