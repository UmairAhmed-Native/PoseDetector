package com.example.posedetector

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.posedetector.databinding.ActivityLivePreviewBinding
import com.example.posedetector.objectdetector.ObjectDetectorProcessor
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions

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
    private var objectDetectorImageProcessor: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private lateinit var cameraSelector:CameraSelector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLivePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
       cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
        livePreviewViewModel.getProcessCameraProvider(this@LivePreviewActivity)
            .observe(
                this
            ) { provider ->
                cameraProvider = provider
                initializeObjectDetector()
                bindUnbindAllCameraUseCases()
            }
    }

    private fun bindUnbindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider?.unbindAll()
            bindCamera()
            bindAnalysisUseCase()
        }
    }

    private fun bindCamera() {
        if (cameraProvider == null) {
            return
        }
        if (cameraPreview != null) {
            cameraProvider!!.unbind(cameraPreview)
        }

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

        val builder = ImageAnalysis.Builder()
        analysisUseCase = builder.build()

        needUpdateGraphicOverlayImageSourceInfo = true

        analysisUseCase?.setAnalyzer(
            // imageProcessor.processImageProxy will use another thread to run the detection underneath,
            // thus we can just runs the analyzer itself on main thread.
            ContextCompat.getMainExecutor(this)
        ) { imageProxy: ImageProxy ->
            if (needUpdateGraphicOverlayImageSourceInfo) {
                val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                if (rotationDegrees == 0 || rotationDegrees == 180) {
                    binding.graphicOverlay.setImageSourceInfo(
                        imageProxy.width,
                        imageProxy.height,
                        false
                    )
                } else {
                    binding.graphicOverlay.setImageSourceInfo(
                        imageProxy.height,
                        imageProxy.width,
                        false
                    )
                }
                needUpdateGraphicOverlayImageSourceInfo = false
            }
            try {
                imageProcessor?.processImageProxy(imageProxy, binding.graphicOverlay)
            } catch (e: MlKitException) {
                Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        cameraProvider?.bindToLifecycle(/* lifecycleOwner= */
            this, cameraSelector, analysisUseCase)

    }

    private fun initializeObjectDetector() {
        try {
            Log.i(
                TAG,
                "Using Object Detector Processor"
            )
            val builder: ObjectDetectorOptions.Builder =
                ObjectDetectorOptions.Builder()
                    .enableClassification()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            objectDetectorImageProcessor =
                ObjectDetectorProcessor(
                    this,
                    builder.build()
                )
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Can not create image processor: OBJECT DETECTION",
                e
            )
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.message,
                Toast.LENGTH_LONG
            )
                .show()
        }
    }

    public override fun onResume() {
        super.onResume()
        initializeObjectDetector()
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