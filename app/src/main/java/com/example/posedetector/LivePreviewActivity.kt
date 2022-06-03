package com.example.posedetector

import android.graphics.Bitmap
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
import com.example.posedetector.helper.getAngle
import com.example.posedetector.model.AngleInfo
import com.example.posedetector.objectdetector.ObjectDetectorProcessor
import com.google.mlkit.common.MlKitException
import com.example.posedetector.posedetector.PoseDetectorProcessor
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions

class LivePreviewActivity : PermissionActivity() {

    companion object {
        val TAG = this.javaClass.canonicalName
    }

    private lateinit var binding: ActivityLivePreviewBinding
    private val livePreviewViewModel: LivePreviewViewModel by viewModels()
    private var cameraProvider: ProcessCameraProvider? = null
    private var previewUseCase: Preview? = null
    private var analysisUseCase: ImageAnalysis? = null
    private var objectDetectorProcessing: VisionImageProcessor? = null
    private var poseDetectorProcessing: VisionImageProcessor? = null
    private var needUpdateGraphicOverlayImageSourceInfo = false
    private var cameraSelector: CameraSelector? = null
    private var poseDetector: PoseDetector? = null

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
                bindAllCameraUseCases()
            }
    }

    private fun bindAllCameraUseCases() {
        if (cameraProvider != null) {
            // As required by CameraX API, unbinds all use cases before trying to re-bind any of them.
            cameraProvider?.unbindAll()
            bindPreviewUseCase()
            bindAnalysisUseCase()
        }
    }

    private fun bindPreviewUseCase() {
        /*  if (!PreferenceUtils.isCameraLiveViewportEnabled(this)) {
              return
          }*/
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        val builder = Preview.Builder()
        previewUseCase = builder.build()
        previewUseCase?.setSurfaceProvider(binding.previewView.surfaceProvider)
        cameraProvider?.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            previewUseCase
        )
    }

    private fun bindAnalysisUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        if (objectDetectorProcessing != null) {
            objectDetectorProcessing!!.stop()
        }

        try {
            Log.i(TAG, "Using Object Detector Processor")
            val objectDetectorBuilder: ObjectDetectorOptions.Builder =
                ObjectDetectorOptions.Builder()
                    .enableClassification()
                    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
            objectDetectorProcessing =
                ObjectDetectorProcessor(
                    this,
                    objectDetectorBuilder.build()
                )
            (objectDetectorProcessing as ObjectDetectorProcessor).objectDetectorSuccession =
                ::objectDetection

        } catch (e: Exception) {
            Log.e(TAG, "Can not create image processor: ObjectDetectorProcessor", e)
            Toast.makeText(
                applicationContext,
                "Can not create image processor: " + e.localizedMessage,
                Toast.LENGTH_LONG
            )
                .show()
            return
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
                objectDetectorProcessing?.processImageProxy(imageProxy, binding.graphicOverlay)
//                poseDetectorProcessing?.processImageProxy(imageProxy, binding.graphicOverlay)

            } catch (e: MlKitException) {
                Log.e(TAG, "Failed to process image. Error: " + e.localizedMessage)
                Toast.makeText(applicationContext, e.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }
        }
        cameraProvider!!.bindToLifecycle(/* lifecycleOwner= */ this,
            cameraSelector!!,
            analysisUseCase
        )
    }

    public override fun onResume() {
        super.onResume()
        initializePoseDetector()
        bindAllCameraUseCases()
    }

    override fun onPause() {
        super.onPause()

        objectDetectorProcessing?.run { this.stop() }
        poseDetectorProcessing?.run { this.stop() }
    }

    public override fun onDestroy() {
        super.onDestroy()
        objectDetectorProcessing?.run { this.stop() }
        poseDetectorProcessing?.run { this.stop() }
    }

    private fun objectDetection(originalCameraImage: Bitmap?) {
        originalCameraImage?.let {
            poseDetection(it)
        }
    }

    private fun poseDetection(bitmap: Bitmap) {
        poseDetector
            ?.process(InputImage.fromBitmap(bitmap, 0))
            ?.addOnSuccessListener { pose ->

                poseDetection(pose)
            }
    }

    private fun poseDetection(pose: Pose?) {
        pose?.let { _pose ->

            binding.txtWristLeftAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
            ).toString()

            binding.txtWristRightAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.LEFT_THUMB),
                _pose.getPoseLandmark(PoseLandmark.LEFT_WRIST),
                _pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
            ).toString()

            binding.txtElbowLeftAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.LEFT_WRIST),
                _pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW),
                _pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
            ).toString()

            binding.txtElbowRightAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
            ).toString()

            binding.txtShoulderLeftAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW),
                _pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                _pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
            ).toString()

            binding.txtShoulderRightAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
            ).toString()

            binding.txtHipLeftAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER),
                _pose.getPoseLandmark(PoseLandmark.LEFT_HIP),
                _pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
            ).toString()

            binding.txtHipRightAngle.text = getAngle(
                _pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_HIP),
                _pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
            ).toString()


        }
    }


    private fun initializePoseDetector() {
        val poseDetectorBuilder = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE)

        poseDetector = PoseDetection.getClient(poseDetectorBuilder.build())
    }
}