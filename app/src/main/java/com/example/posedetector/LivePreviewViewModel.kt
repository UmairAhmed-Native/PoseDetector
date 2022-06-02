package com.example.posedetector

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.concurrent.ExecutionException

class LivePreviewViewModel() : ViewModel() {

    private val cameraProviderLiveData = MutableLiveData<ProcessCameraProvider>()

    fun getProcessCameraProvider(context: Context): LiveData<ProcessCameraProvider?> {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    cameraProviderLiveData.setValue(cameraProviderFuture.get())
                } catch (e: ExecutionException) {
                    // Handle any errors (including cancellation) here.
                    Log.e(
                        LivePreviewActivity.TAG,
                        "Unhandled exception",
                        e
                    )
                } catch (e: InterruptedException) {
                    Log.e(
                        LivePreviewActivity.TAG,
                        "Unhandled exception",
                        e
                    )
                }
            },
            ContextCompat.getMainExecutor(context)
        )
        return cameraProviderLiveData
    }


}
