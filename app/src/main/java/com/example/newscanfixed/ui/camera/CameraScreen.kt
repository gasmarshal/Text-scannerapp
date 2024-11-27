package com.example.newscanfixed.ui.camera

import android.content.ContentValues
import android.graphics.*
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.content.Context
import android.os.Environment
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CameraScreen() {
    CameraContent()
}

@Composable
private fun CameraContent() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = cameraProviderFuture.get()
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var detectedText by remember { mutableStateOf("No text detected yet..") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text("Text Scanner") }) },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.Black)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        val previewView = PreviewView(context).apply {
                            layoutParams = LinearLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        val preview = Preview.Builder().build()
                        imageCapture = ImageCapture.Builder()
                            .setDefaultResolution(Size(1280, 720))
                            .build()

                        val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA

                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageCapture
                            )
                            preview.setSurfaceProvider(previewView.surfaceProvider)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        previewView
                    }
                )
            }

            Text(
                text = detectedText,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color.White)
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )

            Button(
                onClick = {
                    val file = createFile(context)

                    if (imageCapture == null) {
                        Log.e("CameraDebug", "ImageCapture is null!")
                        detectedText = "Error: Camera not ready!"
                        return@Button
                    }

                    imageCapture?.takePicture(
                        ImageCapture.OutputFileOptions.Builder(file).build(),
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageSavedCallback {
                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                Log.d("CameraDebug", "Image saved successfully: ${file.absolutePath}")

                                processImage(context, file) { recognizedText ->
                                    detectedText = recognizedText

                                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                                    val adjustedBitmap = adjustBitmapOrientation(file.absolutePath, bitmap)
                                    val bitmapWithText = overlayTextOnImage(adjustedBitmap, detectedText)

                                    val fileWithText = File(file.parent, "IMG_WITH_TEXT_${file.name}")
                                    FileOutputStream(fileWithText).use { out ->
                                        bitmapWithText.compress(Bitmap.CompressFormat.JPEG, 100, out)
                                    }
                                    Log.d("CameraDebug", "Image with text saved: ${fileWithText.absolutePath}")

                                    val values = ContentValues().apply {
                                        put(MediaStore.Images.Media.DISPLAY_NAME, fileWithText.name)
                                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TextScans")
                                    }

                                    val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                                    uri?.let {
                                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                                            fileWithText.inputStream().copyTo(outputStream)
                                        }
                                        Log.d("CameraDebug", "Image with text added to gallery: $uri")
                                    }
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                Log.e("CameraDebug", "Error capturing image: ${exception.message}")
                                detectedText = "Error capturing image: ${exception.message}"
                            }
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Capture Text")
            }
        }
    }
}

private fun createFile(context: Context): File {
    val dir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "TextScans")
    if (!dir.exists()) dir.mkdirs()
    val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
    return File(dir, "IMG_$timeStamp.jpg")
}

private fun adjustBitmapOrientation(filePath: String, bitmap: Bitmap): Bitmap {
    val exif = ExifInterface(filePath)
    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED)

    return when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bitmap, 90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bitmap, 180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bitmap, 270f)
        else -> bitmap
    }
}

private fun rotateBitmap(bitmap: Bitmap, angle: Float): Bitmap {
    val matrix = Matrix()
    matrix.postRotate(angle)
    return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
}

private fun processImage(context: Context, file: File, onDetectedText: (String) -> Unit) {
    val image = InputImage.fromFilePath(context, android.net.Uri.fromFile(file))
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    recognizer.process(image)
        .addOnSuccessListener { visionText ->
            onDetectedText(visionText.text)
        }
        .addOnFailureListener { e ->
            onDetectedText("Error processing image: ${e.message}")
        }
}

private fun overlayTextOnImage(image: Bitmap, text: String): Bitmap {
    val newBitmap = image.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(newBitmap)

    val textColor = com.example.newscanfixed.ui.theme.White.toArgb()
    val backgroundColor = com.example.newscanfixed.ui.theme.PurpleGrey40.toArgb()

    val paint = Paint().apply {
        color = textColor
        textSize = (image.height * 0.03).toFloat() // Ukuran teks responsif
        isAntiAlias = true
    }

    val backgroundPaint = Paint().apply {
        color = backgroundColor
        alpha = 150 // Transparansi untuk latar belakang teks
    }

    val maxWidth = image.width * 0.9f // Maksimum lebar teks (90% dari lebar gambar)
    val x = (image.width * 0.05f) // Margin kiri (5% dari lebar gambar)
    val lineHeight = paint.textSize + 10f // Jarak antar baris
    var y = (image.height * 0.05).toFloat() // Margin atas (5% dari tinggi gambar)

    // Pisahkan teks panjang menjadi baris-baris berdasarkan lebar maksimum
    val words = text.split(" ")
    val wrappedLines = mutableListOf<String>()
    var currentLine = StringBuilder()

    for (word in words) {
        val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
        if (paint.measureText(testLine) <= maxWidth) {
            currentLine.append(if (currentLine.isEmpty()) word else " $word")
        } else {
            wrappedLines.add(currentLine.toString())
            currentLine = StringBuilder(word)
        }
    }
    if (currentLine.isNotEmpty()) {
        wrappedLines.add(currentLine.toString())
    }

    // Gambar setiap baris teks ke canvas
    for (line in wrappedLines) {
        val textWidth = paint.measureText(line)
        // Gambar latar belakang untuk setiap baris teks
        canvas.drawRect(x - 10, y - paint.textSize, x + textWidth + 10, y + 10, backgroundPaint)
        // Gambar teks
        canvas.drawText(line, x, y, paint)
        y += lineHeight

        // Hentikan jika teks melampaui tinggi gambar
        if (y + lineHeight > image.height) break
    }

    return newBitmap
}

