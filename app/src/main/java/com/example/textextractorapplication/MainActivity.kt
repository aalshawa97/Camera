package com.example.textextractorapplication

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
//Optical character recognition engine
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var captureButton: Button
    private lateinit var selectButton: Button
    private val PERMISSION_CODE = 1000
    private val IMAGE_CAPTURE_CODE = 1001
    private val IMAGE_PICK_CODE = 1002
    private var imageUri: Uri? = null
    private lateinit var tessBaseAPI: TessBaseAPI
    private lateinit var extractedTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imageView = findViewById(R.id.imageView)
        captureButton = findViewById(R.id.captureButton)
        selectButton = findViewById(R.id.selectButton)
        //extractedTextView = findViewById(R.id.extractedTextView) // Initialize TextView

        // Initialize Tesseract
        /*
        val dataPath = filesDir.absolutePath + "/tesseract/"
        if (!File(dataPath + "tessdata/").exists()) {
            copyTessDataFiles()
        }*/
        //tessBaseAPI = TessBaseAPI()
        //tessBaseAPI.init(dataPath, "eng")

        captureButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_CODE)
            }
        }

        selectButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), PERMISSION_CODE)
                }
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    openGallery()
                } else {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_CODE)
                }
            }
        }
    }

    private fun extractTextFromImage(uri: Uri){
        /*
        val imageBitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
        tessBaseAPI.setImage(imageBitmap)
        val extractedText = tessBaseAPI.utF8Text
        tessBaseAPI.end()
        displayExtractedText(extractedText)
        */
    }

    private fun displayExtractedText(text: String) {
        extractedTextView.text = text
    }

    private fun copyTessDataFiles() {
        val assetManager = assets
        val tessDataDir = File(filesDir, "tesseract/tessdata")
        if (!tessDataDir.exists()) {
            tessDataDir.mkdirs()
        }
        try {
            val assetFiles = assetManager.list("tessdata")
            assetFiles?.forEach { filename ->
                val file = File(tessDataDir, filename)
                if (!file.exists()) {
                    assetManager.open("tessdata/$filename").use { inputStream ->
                        FileOutputStream(file).use { outputStream ->
                            val buffer = ByteArray(1024)
                            var length: Int
                            while (inputStream.read(buffer).also { length = it } > 0) {
                                outputStream.write(buffer, 0, length)
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun openCamera() {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.TITLE, "New Picture")
            put(MediaStore.Images.Media.DESCRIPTION, "From the Camera")
        }

        // Insert a new record into the MediaStore and get its URI
        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        }

        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, IMAGE_CAPTURE_CODE)
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_LONG).show()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, IMAGE_PICK_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (permissions[0] == Manifest.permission.CAMERA) {
                    openCamera()
                } else if (permissions[0] == Manifest.permission.READ_EXTERNAL_STORAGE || permissions[0] == Manifest.permission.READ_MEDIA_IMAGES) {
                    openGallery()
                }
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                IMAGE_CAPTURE_CODE -> {
                    imageUri?.let {
                        imageView.setImageURI(it)
                        extractTextFromImage(it) // Extract text from the captured image
                    }
                }
                IMAGE_PICK_CODE -> {
                    data?.data?.let { uri ->
                        imageView.setImageURI(uri)
                        extractTextFromImage(uri) // Extract text from the selected image
                    }
                }
            }
        }
    }



}
