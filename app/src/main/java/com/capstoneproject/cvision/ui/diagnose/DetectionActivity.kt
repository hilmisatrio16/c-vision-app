package com.capstoneproject.cvision.ui.diagnose

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import com.capstoneproject.cvision.R
import com.capstoneproject.cvision.data.model.predict.RequestPredict
import com.capstoneproject.cvision.data.model.predict.ResponsePredict
import com.capstoneproject.cvision.databinding.ActivityDetectionBinding
import com.capstoneproject.cvision.ui.diagnose.CameraActivity.Companion.CAMERAX_RESULT
import com.capstoneproject.cvision.utils.Result
import com.capstoneproject.cvision.utils.reduceFileImage
import com.capstoneproject.cvision.utils.uriToFile

import java.nio.ByteBuffer
import java.nio.ByteOrder

class DetectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetectionBinding
    private var currentImageUri: Uri? = null
    private var token: String = ""

    private val detectionVM by viewModels<DetectionViewModel> {
        DetectionViewModelFactory.getInstance(this)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(
                    this,
                    resources.getString(R.string.request_granted),
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    this,
                    resources.getString(R.string.request_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun allPermissionsGranted() =
        ContextCompat.checkSelfPermission(
            this,
            REQUIRED_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!allPermissionsGranted()) {
            requestPermissionLauncher.launch(REQUIRED_PERMISSION)
        }

        detectionVM.getTokenUser().observe(this, Observer {
            if (it.isNotEmpty()) {
                token = it
            }
        })

        binding.btnTakeCamera.setOnClickListener { startCameraX() }
        binding.btnImageGallery.setOnClickListener { startGallery() }
        binding.btnDetection.setOnClickListener {

            if (currentImageUri != null && token.isNotEmpty()) {
                predictImageCataract()

            } else {
                Toast.makeText(this, "Please Selected image", Toast.LENGTH_SHORT).show()
            }

        }

        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun convertUriToBitmap(context: Context, uri: Uri): Bitmap? {

        var bitmapImage: Bitmap? = null
        try {
            val contentResolver: ContentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            bitmapImage = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ERROR", e.message.toString())
        }
        return bitmapImage

    }


    private fun predictImageCataract() {
        currentImageUri?.let { uri ->
            val eyeImageFile = uriToFile(uri, this)
            val tokenUser = token

            detectionVM.predictCataract(RequestPredict(tokenUser, eyeImageFile))
                .observe(this, Observer {
                    if (it != null) {
                        when (it) {
                            is Result.Success -> {
                                binding.lottieLoading.visibility = View.GONE
                                showResult(it.data)
                            }

                            is Result.Loading -> {
                                binding.lottieLoading.visibility = View.VISIBLE
                            }

                            is Result.Error -> {
                                binding.lottieLoading.visibility = View.GONE
                                Toast.makeText(this, it.error, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })

        }
    }


    private fun showResult(dataRespon: ResponsePredict) {
        val resultDialog = ResultDiagnosisDialogFragment(
            currentImageUri!!,
            dataRespon
        )
        resultDialog.show(supportFragmentManager, ResultDiagnosisDialogFragment.TAG)
    }


    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri

            showImage()
        } else {
            Log.d("Photo Picker", "No media or image selected")
        }
    }


    private fun startCameraX() {
        val intent = Intent(this, CameraActivity::class.java)
        launcherIntentCameraX.launch(intent)
    }

    private val launcherIntentCameraX = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == CAMERAX_RESULT) {
            currentImageUri = it.data?.getStringExtra(CameraActivity.EXTRA_CAMERAX_IMAGE)?.toUri()
            showImage()
        }
    }

    private fun showImage() {
        currentImageUri?.let {
            binding.imageEye.setImageURI(it)
        }
    }

    companion object {
        private const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
    }
}