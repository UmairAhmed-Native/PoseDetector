package com.example.posedetector

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.example.posedetector.databinding.ActivityLivePreviewBinding
import com.google.android.gms.common.images.Size
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class LivePreviewActivity : PermissionActivity() {

    companion object {
        val TAG = this.javaClass.canonicalName
    }

    private lateinit var binding: ActivityLivePreviewBinding
    private val livePreviewViewModel: LivePreviewViewModel by viewModels()
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraPreview: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var imageProcessor: VisionImageProcessor? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        livePreviewViewModel.getProcessCameraProvider(this@LivePreviewActivity)
            .observe(
                this
            ) { provider ->
                cameraProvider = provider
                bindUnbindAllCameraUseCases()
            }
    }

    private fun bindUnbindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider!!.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (cameraPreview != null) {
            cameraProvider!!.unbind(cameraPreview)
        }
        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        val builder = Preview.Builder()
        cameraPreview = builder.build()
        cameraPreview?.setSurfaceProvider(binding.previewView.surfaceProvider)
        cameraProvider?.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector,
            cameraPreview
        )
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider?.unbind(analysisUseCase)
        }
        if (imageProcessor != null) {
            imageProcessor!!.stop()
        }
    }

    public override fun onResume() {
        super.onResume()
        bindUnbindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()

        imageProcessor?.run { this.stop() }
    }

    public override fun onDestroy() {
        super.onDestroy()
        imageProcessor?.run { this.stop() }
    }
}