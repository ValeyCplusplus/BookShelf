package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import android.app.Activity

class BarcodeAnalyzer (private val onBarcodeDetected: (barcode:String) -> Unit, private val activity: Camera): ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null){
            val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(inputImage).addOnSuccessListener {
                barcodes -> for (barcode in barcodes){
                    onBarcodeDetected(barcode.rawValue ?:"")

                    val intent = Intent()
                    intent.putExtra("barcode", barcode.rawValue)
                    activity.setResult(Activity.RESULT_OK, intent)
                    activity.finish()
                }
            }
                .addOnFailureListener{

                }
                .addOnCompleteListener{
                    imageProxy.close()
                }
        }

    }
}