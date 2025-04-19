package com.example.rice

import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.InputStream

/**
 * The main activity for the rice disease detection application.
 * It allows users to select an image, capture a photo, paste from the clipboard,
 * and then runs a TensorFlow Lite model to detect potential rice diseases.
 */
@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {

    private lateinit var resultText: TextView
    private lateinit var imageView: ImageView
    private lateinit var detectButton: Button
    private lateinit var pickImageButton: Button
    private lateinit var takePhotoButton: Button
    private lateinit var clearButton: Button
    private lateinit var pasteButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var pickButton: ImageButton
    private lateinit var takeButton: ImageButton
    private lateinit var PASTEButton: ImageButton
    private lateinit var ClearButton: ImageButton
    private lateinit var DetectButton: ImageButton

    private var selectedBitmap: Bitmap? = null
    private val CAMERA_PERMISSION_CODE = 100

    // Initialize TensorFlow Lite Model
    private lateinit var tensorflowLiteModel: TensorFlowLiteModel

    // Activity result launcher for picking an image from gallery
    private val pickImage: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { loadImageFromUri(it) }
        }

    // Activity result launcher for taking a photo with the camera
    private val takePhoto: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val imageBitmap = result.data?.extras?.get("data") as? Bitmap
                imageBitmap?.let {
                    selectedBitmap = it
                    imageView.setImageBitmap(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize views
        resultText = findViewById(R.id.result_text)
        imageView = findViewById(R.id.imageView)
        detectButton = findViewById(R.id.detect_button)
        pickImageButton = findViewById(R.id.pick_image_button)
        takePhotoButton = findViewById(R.id.take_photo_button)
        clearButton = findViewById(R.id.clearButton)
        progressBar = findViewById(R.id.progressBar)
        pasteButton = findViewById(R.id.paste_button)
        pickButton = findViewById(R.id.pick_button)
        takeButton = findViewById(R.id.take_button)
        PASTEButton = findViewById(R.id.Paste_button)
        ClearButton = findViewById(R.id.clear_button)
        DetectButton = findViewById(R.id.Detect_button)

        // Initialize TensorFlow Lite Model
        tensorflowLiteModel = TensorFlowLiteModel(this)

        // Set up click listeners
        imageView.setOnClickListener { pickImage.launch("image/*") }
        pickImageButton.setOnClickListener { pickImage.launch("image/*") }
        pickButton.setOnClickListener { pickImage.launch("image/*") } // Consider if this is redundant

        detectButton.setOnClickListener { detectDisease() }
        DetectButton.setOnClickListener { detectDisease() } // Consider if this is redundant

        takePhotoButton.setOnClickListener { requestCameraPermission() }
        takeButton.setOnClickListener { requestCameraPermission() } // Consider if this is redundant

        clearButton.setOnClickListener { clearImage() }
        ClearButton.setOnClickListener { clearImage() } // Consider if this is redundant

        pasteButton.setOnClickListener { pasteImageFromClipboard() }
        PASTEButton.setOnClickListener { pasteImageFromClipboard() } // Consider if this is redundant
    }

    /**
     * Loads a bitmap image from the given URI and sets it to the image view.
     * Handles potential exceptions during image loading.
     *
     * @param uri The URI of the image to load.
     */
    private fun loadImageFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            inputStream?.let {
                selectedBitmap = BitmapFactory.decodeStream(it)
                imageView.setImageURI(uri)
                it.close()
            } ?: run {
                Toast.makeText(this, "Could not open image stream!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Pastes an image from the clipboard into the image view.
     * It checks if the clipboard contains an image URI and handles potential errors.
     */
    private fun pasteImageFromClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        if (!clipboard.hasPrimaryClip()) {
            Toast.makeText(this, "Clipboard is empty!", Toast.LENGTH_SHORT).show()
            return
        }

        val clipData = clipboard.primaryClip
        val item = clipData?.getItemAt(0)
        val pastedImageUri = item?.uri

        if (pastedImageUri != null) {
            val contentResolver = applicationContext.contentResolver
            val mimeType = contentResolver.getType(pastedImageUri)
            if (mimeType?.startsWith("image/") == true) {
                loadImageFromUri(pastedImageUri)
                Toast.makeText(this, "Image pasted successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Clipboard does not contain an image!", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "No valid image found in clipboard!", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Requests camera permission if it's not already granted.
     */
    private fun requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
        } else {
            openCamera()
        }
    }

    /**
     * Opens the camera app to capture a new image.
     */
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePhoto.launch(cameraIntent)
    }

    /**
     * Runs the disease detection process on the selected bitmap.
     * It disables the detect button, shows the progress bar,
     * calls the TensorFlow Lite model, and updates the UI with the results.
     */
//    private fun detectDisease() {
//        if (selectedBitmap == null) {
//            resultText.text = "PLEASE SELECT AN IMAGE FIRST!"
//            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        detectButton.isEnabled = false
//        progressBar.visibility = View.VISIBLE
//        resultText.text = ""
//
//        Handler(Looper.getMainLooper()).postDelayed({
//            try {
//                // Log image size
//                println("Selected Bitmap: Width = ${selectedBitmap!!.width}, Height = ${selectedBitmap!!.height}")
//
//                // Check if the image is a leaf
//                if (!tensorflowLiteModel.isLeafImage(selectedBitmap!!)) {
//                    resultText.text = "Please upload a valid leaf image."
//                    Toast.makeText(this, "Not a leaf image!", Toast.LENGTH_SHORT).show()
//                    return@postDelayed
//                }
//
//                // Preprocess image
//                val features = tensorflowLiteModel.extractFeatures(selectedBitmap!!)
//
//                // Predict using the model
//                val predictedClass = tensorflowLiteModel.predictWithMetaModel(features)
//
//                // Display result
//                resultText.text = "Detected Disease: ${getClassLabel(predictedClass)}"
//            } catch (e: Exception) {
//                resultText.text = "Error during detection!"
//                Toast.makeText(this, "Detection failed: ${e.message}", Toast.LENGTH_LONG).show()
//                e.printStackTrace()  // Log full error details
//            } finally {
//                progressBar.visibility = View.GONE
//                detectButton.isEnabled = true
//            }
//        }, 2000)
//    }
    private fun detectDisease() {
        if (selectedBitmap == null) {
            resultText.text = "PLEASE SELECT AN IMAGE FIRST!"
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show()
            return
        }

        detectButton.isEnabled = false
        progressBar.visibility = View.VISIBLE
        resultText.text = ""

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                val isLeaf = tensorflowLiteModel.isLeaf(selectedBitmap!!)
                if (!isLeaf) {
                    resultText.text = "Please upload a leaf image!"
                    Toast.makeText(this, "The image is not a leaf!", Toast.LENGTH_LONG).show()
                    return@postDelayed
                }

                val features = tensorflowLiteModel.extractFeatures(selectedBitmap!!)
                val predictedClass = tensorflowLiteModel.predictWithMetaModel(features)
                resultText.text = "Detected Disease: ${getClassLabel(predictedClass)}"

            } catch (e: Exception) {
                resultText.text = " ${e.message}"
//                Toast.makeText(this, "Detection failed: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                progressBar.visibility = View.GONE
                detectButton.isEnabled = true
            }
        }, 1000)
    }

    // Function to check if the image is a leaf



    /**
     * Clears the displayed image and resets the result text to its initial state.
     */
    private fun clearImage() {
        imageView.setImageResource(R.drawable.rice4)
        resultText.text = "OUTPUT:"
        resultText.setTypeface(null, Typeface.BOLD) // Make the text bold
        selectedBitmap = null
    }

    /**
     * Handles the result of the camera permission request.
     * If permission is granted, it opens the camera; otherwise, it shows a toast message.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                    this,
                    "Camera permission is required to take photos",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getClassLabel(predictedClass: Int): String {
        val labels = arrayOf(
            "Bacterial Leaf Blight", "Brown Spot", "Healthy Rice Leaf", "Leaf Blast",
            "Leaf Scald", "Narrow Brown Leaf Spot", "Neck Blast", "Rice Hispa",
            "Sheath Blight", "Tungro"
        )
        return labels.getOrElse(predictedClass) { "Unknown" }
    }
    /**
     * Releases the resources held by the TensorFlow Lite model when the activity is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()
        tensorflowLiteModel.close() // Release resources held by the model
    }
}

