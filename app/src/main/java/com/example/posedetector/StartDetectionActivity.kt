package com.example.posedetector

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.example.posedetector.databinding.ActivityStartDetectionBinding
import com.example.posedetector.helper.currentDate
import com.example.posedetector.helper.formatDateToString
import com.example.posedetector.helper.getOutputDirectory
import com.example.posedetector.helper.utils.BitmapUtils
import com.example.posedetector.objectdetector.ObjectDetectorProcessor
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.io.File
import java.io.IOException

class StartDetectionActivity : PermissionActivity() {

    private lateinit var binding: ActivityStartDetectionBinding
    private var imagePickerType = -1
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String? = null
    private var objectDetectorImageProcessor: VisionImageProcessor? = null

    companion object {
        const val IMAGE_PICKER_EXTENSION = "image/*"
        private val TAG = this.javaClass.canonicalName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityStartDetectionBinding.inflate(layoutInflater)

        setContentView(binding.root)

        binding.btnSelectFromGallery.setOnClickListener {
            onGalleryListener()
        }
        binding.btnCaptureFromCamera.setOnClickListener {
            onCameraDialogListener()
        }
    }

    private fun onCameraDialogListener() {
        imagePickerType = 2
        checkWriteReadCameraPermissions {
            resultLauncher.launch(openCameraIntent())
        }

    }

    private fun onGalleryListener() {
        imagePickerType = 1
        checkWriteReadCameraPermissions {
            browseImage()
        }
    }

    private fun browseImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = IMAGE_PICKER_EXTENSION
        intent.putExtra(
            Intent.EXTRA_MIME_TYPES,
            arrayOf(
                IMAGE_PICKER_EXTENSION
            )
        )
        resultLauncher.launch(intent)
    }

    private fun openCameraIntent(): Intent {
        selectedImageUri = null
        binding.previewImage.setImageBitmap(null)
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                    Log.d("Tag", ex.localizedMessage)
                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.android.fileprovider_pose_detector",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                }
            }
        }
        return cameraIntent
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Create an image file name
        val timeStamp = formatDateToString(currentDate(), "yyyyMMdd_HHmmss")
        val storageDir: File = getOutputDirectory(this, this)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                when (imagePickerType) {
                    1 -> {
                        val intent: Intent? = result.data
                        selectedImageUri = intent?.data
                    }
                    2 -> {
                        currentPhotoPath?.let {
                            if (File(it).exists()) {
                                Log.d(TAG, "FilePath Exist")
                            }
                            selectedImageUri = File(it).toUri()
                        }


                    }
                }

                tryReloadAndDetectInImage()

            }
        }

    private fun tryReloadAndDetectInImage() {
        Log.d(
            TAG,
            "Try reload and detect image"
        )
        try {
            if (selectedImageUri == null) {
                return
            }
            val imageBitmap =
                BitmapUtils.getBitmapFromContentUri(contentResolver, selectedImageUri) ?: return
            binding.graphicOverlay.clear()
            val resizedBitmap = Bitmap.createScaledBitmap(
                imageBitmap,
                (imageBitmap.width),
                (imageBitmap.height),
                true
            )
            binding.previewImage.setImageBitmap(resizedBitmap)

            if (objectDetectorImageProcessor != null) {
                binding.graphicOverlay.setImageSourceInfo(
                    resizedBitmap.width, resizedBitmap.height, /* isFlipped= */false
                )
                objectDetectorImageProcessor?.processBitmap(resizedBitmap, binding.graphicOverlay)
            } else {
                Log.e(
                    TAG,
                    "Null imageProcessor, please check adb logs for imageProcessor creation error"
                )
            }
        } catch (e: IOException) {
            Log.e(
                TAG,
                "Error retrieving saved image"
            )
            selectedImageUri = null
        }
    }

    private fun createImageProcessor() {
        try {
            Log.i(
                TAG,
                "Using Object Detector Processor"
            )
            val builder: ObjectDetectorOptions.Builder =
                ObjectDetectorOptions.Builder()
                    .enableClassification()
                    .setDetectorMode(ObjectDetectorOptions.SINGLE_IMAGE_MODE)
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


    public override fun onPause() {
        super.onPause()
        objectDetectorImageProcessor?.run {
            this.stop()
        }
    }

    public override fun onDestroy() {
        super.onDestroy()
        objectDetectorImageProcessor?.run {
            this.stop()
        }
    }

    public override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume")
        createImageProcessor()
        tryReloadAndDetectInImage()
    }

}