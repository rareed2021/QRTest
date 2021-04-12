package com.test.qrtest

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.util.forEach
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.text.TextRecognizer
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.text.TextBlock
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.test.qrtest.databinding.ActivityMainBinding
import java.lang.StringBuilder




class MainActivity : AppCompatActivity() {
    lateinit var detector :BarcodeDetector
    lateinit var textDetector: TextRecognizer
    lateinit var binding: ActivityMainBinding
    lateinit var processor: Detector.Processor<Barcode>
    lateinit var textProcessor : Detector.Processor<TextBlock>
    lateinit var camera:CameraSource
    lateinit var textCamera:CameraSource
    var isTaking = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun stopCamera() {
        isTaking=false
        camera.stop()
        textCamera.stop()
        binding.buttonQr.visibility= View.VISIBLE
        binding.buttonText.visibility = View.VISIBLE
        binding.buttonStop.visibility= View.GONE
    }

    @SuppressLint("MissingPermission")
    private fun startCamera(camera: CameraSource) {
        binding.buttonQr.visibility= View.GONE
        binding.buttonText.visibility = View.GONE
        binding.buttonStop.visibility= View.VISIBLE
        camera.start(binding.surfaceImage.holder)
    }

    fun init(){
        textProcessor = object :Detector.Processor<TextBlock>{
            override fun release() {
                camera.stop()
            }

            override fun receiveDetections(p0: Detector.Detections<TextBlock>) {
                val builder = StringBuilder()
                p0.detectedItems.forEach { id, item ->
                    Log.d(TAG, "receiveDetections: ${item.language}")
                    builder.append(item.value)
                    builder.append("\n")
                }
                binding.textData.text = builder.toString()
            }
        }
        processor = object:Detector.Processor<Barcode>{
            override fun release() {
                camera.stop()
            }

            override fun receiveDetections(p0: Detector.Detections<Barcode>) {
                val builder = StringBuilder()
                p0.detectedItems.forEach{ id,item->
                    val url = item.email
                    Log.d(TAG, "receiveDetections: $url ${url?.address}")
                    builder.append(item.rawValue)
                    builder.append("\n")
                }
                binding.textData.text = builder.toString()
            }
        }
        detector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.QR_CODE or Barcode.UPC_A or Barcode.CODABAR or Barcode.UPC_E)
            .build().apply {
                setProcessor(processor)
            }

        if(!detector.isOperational){
            Log.d(TAG, "Error setting up detector")
            Toast.makeText(this, "Could not set up detector", Toast.LENGTH_SHORT).show()
            return
        }

        textDetector = TextRecognizer.Builder(this)
                .build().apply {
                    setProcessor(textProcessor)
                }

        camera = CameraSource.Builder(applicationContext, detector)
            .setFacing(CameraSource.CAMERA_FACING_BACK)
            .setRequestedPreviewSize(1600,1024)
            .setRequestedFps(15.0f)
                .setAutoFocusEnabled(true)
            .build()

        textCamera = CameraSource.Builder(this,textDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600,1024)
                .setRequestedFps(12.0f)
                .setAutoFocusEnabled(true)
                .build()

        binding.buttonQr.setOnClickListener {
//            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_CODE)
//            if(isTaking)
//                stopCamera()
//            else
            handleQR()
        }
        binding.buttonText.setOnClickListener {
            handleText()
        }
        binding.buttonStop.setOnClickListener {
            stopCamera()
        }
    }

    private fun handleText() {
        Dexter.withContext(this)
                .withPermission(Manifest.permission.CAMERA)
                .withListener(object:PermissionListener{
                    override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                        startCamera(textCamera)
                    }

                    override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                        
                    }

                    override fun onPermissionRationaleShouldBeShown(p0: PermissionRequest?, p1: PermissionToken?) {
                        
                    }

                }).check()
    }

    private fun handleQR() {
        Dexter.withContext(this)
            .withPermission(Manifest.permission.CAMERA)
            .withListener(object : PermissionListener {
                @SuppressLint("MissingPermission")
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    startCamera(camera)

                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Log.d(TAG, "onPermissionDenied: ")
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    Log.d(TAG, "onPermissionRationaleShouldBeShown: ")
                }

            })
            .check()
    }

    private fun takeQR() {
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK){
            if(requestCode== REQUEST_CODE){
                val bmp = data?.extras?.get("data") as? Bitmap
                bmp?.also {
                }
            }
        }
    }



    companion object{
        const val REQUEST_CODE = 4
        private const val TAG = "myapp"
    }


}