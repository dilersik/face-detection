package com.example.mymlkitlib

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.CompletableFuture

class FaceDetector {
    private val faceDetector: FaceDetector

    init {
        val options = FaceDetectorOptions.Builder()
            .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
            .build()

        faceDetector = FaceDetection.getClient(options)
    }

    fun detectFaces(bitmap: Bitmap): CompletableFuture<FaceDetectionResult> {
        val inputImage = InputImage.fromBitmap(bitmap, 0)
        val result = CompletableFuture<FaceDetectionResult>()

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                val faceDetectionResult = FaceDetectionResult(faces)
                result.complete(faceDetectionResult)
            }
            .addOnFailureListener { e ->
                Log.e("FaceDetector", "Error detecting faces: ${e.message}")
                result.completeExceptionally(e)
            }

        return result
    }
}

class FaceDetectionResult(val faces: List<Face>)