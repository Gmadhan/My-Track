package com.mytrack.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.mytrack.R
import com.mytrack.databinding.ActivityEditProfileBinding
import com.mytrack.utils.Notify.createNotificationChannel
import com.mytrack.utils.Utils
import java.io.IOException

class EditProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var activityEditProfileBinding: ActivityEditProfileBinding
    private lateinit var viewModel: ProfileViewModel
    val MY_PERMISSIONS_REQUEST_CAMERA = 112
    private var imageEncoded = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityEditProfileBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(activityEditProfileBinding.root)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        init()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.userData.observe(this) { user ->
            user?.let {
                activityEditProfileBinding.edtProfileName.setText(it.name)
                activityEditProfileBinding.edtProfileGmail.setText(it.email)
                activityEditProfileBinding.edtProfilePhno.setText(it.phoneno)
                Utils.setImage(this, it.imageEncoded ?: "", activityEditProfileBinding.profileImg)
            }
        }

        viewModel.updateStatus.observe(this) { result ->
            when (result) {
                is ProfileViewModel.UpdateResult.Loading -> {
                    // Show progress if needed
                }
                is ProfileViewModel.UpdateResult.Success -> {
                    Utils.showToast(this, getString(R.string.updated_successfully))
                    createNotificationChannel("Update", "Hi ${activityEditProfileBinding.edtProfileName.text}, Updated user profile successfully ", this)
                }
                is ProfileViewModel.UpdateResult.Error -> {
                    Utils.showToast(this, result.message)
                }
            }
        }
    }

    fun init() {
        activityEditProfileBinding.header.txtHeaderName.text = getString(R.string.edit_profile)
        activityEditProfileBinding.header.btnBack.setOnClickListener {
            finish()
        }

        activityEditProfileBinding.profileImg.setOnClickListener(this)
        activityEditProfileBinding.btnSubmit.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.profile_img -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) !== PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            ) !== PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ),
                                MY_PERMISSIONS_REQUEST_CAMERA
                            )
                        } else {
                            pickImage()
                        }
                    } else {
                        pickImage()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            R.id.btnSubmit -> {
                val name = activityEditProfileBinding.edtProfileName.text.toString()
                val email = activityEditProfileBinding.edtProfileGmail.text.toString()

                if (name.isEmpty()) {
                    Utils.showToast(this, "Please enter user Name")
                } else if (email.isEmpty()) {
                    Utils.showToast(this, "Please enter user MailID")
                } else {
                    viewModel.updateUser(name, email, if (imageEncoded.isNotEmpty()) imageEncoded else null)
                }
            }

        }

    }

    fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        resultImageLauncher.launch(intent)
    }

    private var resultImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val selectedImage = result.data!!.data
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
                        imageEncoded = Utils.encodeBitmapAndSaveToFirebase(bitmap)
                        activityEditProfileBinding.profileImg.setImageBitmap(bitmap)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
}
