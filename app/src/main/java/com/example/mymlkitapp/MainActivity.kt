package com.example.mymlkitapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.ImageReader
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mymlkitlib.FaceDetectionResult
import com.example.mymlkitlib.FaceDetector
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
    }

    private lateinit var previewImageView: ImageView
    private lateinit var imageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var switchCameraButton: Button
    private lateinit var faceDetector: FaceDetector
    private var isFrontCamera: Boolean = false
    private var currentPhotoPath: String? = null

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val imageBitmap = BitmapFactory.decodeFile(currentPhotoPath)
             previewImageView.setImageBitmap(imageBitmap)

            // Realiza a detecção facial
           faceDetector.detectFaces(imageBitmap)
                .thenAccept { resultFaces ->
                    // Processar o resultado da detecção facial
                    sendPhotoAndDetectionInfo(imageBitmap, resultFaces)
                }
                .exceptionally { exception ->
                    // Lidar com uma exceção ocorrida durante a detecção facial
                    null
                }
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        previewImageView = findViewById(R.id.previewImageView)
        imageView = findViewById(R.id.imageView)
        captureButton = findViewById(R.id.captureButton)
        switchCameraButton = findViewById(R.id.switchCameraButton)

        captureButton.setOnClickListener {
            if (hasCameraPermission()) {
                dispatchTakePictureIntent()
            } else {
                requestCameraPermission()
            }
        }

        switchCameraButton.setOnClickListener {
            isFrontCamera = !isFrontCamera
        }

        faceDetector = FaceDetector()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CAMERA_PERMISSION
        )
    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            // Tratar exceção ao criar arquivo de imagem
            null
        }

        // Continue somente se o arquivo foi criado com sucesso
        photoFile?.let {
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                it
            )
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            currentPhotoPath = photoFile.absolutePath
            resultLauncher.launch(takePictureIntent)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        // Crie um nome de arquivo único com base na data e hora atual
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefixo */
            ".jpg", /* sufixo */
            storageDir /* diretório */
        ).apply {
            // Salve o caminho do arquivo como uma variável global
            // currentPhotoPath = absolutePath
        }
    }

    private fun sendPhotoAndDetectionInfo(imageBitmap: Bitmap, result: FaceDetectionResult) {
        // Código para enviar a foto e informações para a API (mock)
        // ...
        Toast.makeText(this, "Foto enviada com sucesso", Toast.LENGTH_SHORT).show()
    }

}