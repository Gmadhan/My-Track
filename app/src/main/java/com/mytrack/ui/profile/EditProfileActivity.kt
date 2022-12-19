package com.mytrack.ui.profile

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.R
import com.mytrack.databinding.ActivityEditProfileBinding
import com.mytrack.model.response.UserDetail
import com.mytrack.utils.Constants
import com.mytrack.utils.Notify.createNotificationChannel
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import java.io.IOException

class EditProfileActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var activityEditProfileBinding: ActivityEditProfileBinding
    private var mFirebaseDatabase: DatabaseReference? = null
    val MY_PERMISSIONS_REQUEST_CAMERA = 112
    private var userName = ""
    private var userPicturepath = ""
    private var userGmail = ""
    private var userPhno = ""
    private val model = ""
    private var imageEncoded = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityEditProfileBinding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(activityEditProfileBinding.root)
        init()
    }

    fun init() {
        mFirebaseDatabase = Firebase.database.reference
        activityEditProfileBinding.header.txtHeaderName.text = getString(R.string.edit_profile)
        activityEditProfileBinding.header.btnBack.setOnClickListener {
            finish()
        }
        retriveData()
        userPhno = SessionSave.getSession(Constants.MOBILENO,this).toString()
        Utils.setImage(this, SessionSave.getSession(Constants.IMAGE, this).toString(), activityEditProfileBinding.profileImg)
        activityEditProfileBinding.edtProfileName.setText(SessionSave.getSession(Constants.NAME, this).toString())
        activityEditProfileBinding.edtProfileGmail.setText(SessionSave.getSession(Constants.EMAIL, this).toString())
        activityEditProfileBinding.edtProfilePhno.setText(SessionSave.getSession(Constants.MOBILENO, this).toString())

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
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            ActivityCompat.requestPermissions(
                                this,
                                arrayOf(
                                    Manifest.permission.READ_EXTERNAL_STORAGE,
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ),
                                MY_PERMISSIONS_REQUEST_CAMERA
                            )
//                            fragmentProfileBinding.profileImg.performClick()
                        } else {
                            pickImage()
                        }
                    } else {
                        pickImage()
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }

            R.id.btnSubmit -> {
                userName = activityEditProfileBinding.edtProfileName.text.toString()
                userGmail = activityEditProfileBinding.edtProfileGmail.text.toString()
                userPhno = activityEditProfileBinding.edtProfilePhno.text.toString()

                if (userName.isNullOrEmpty()) {
                    Utils.showToast(this, "Please enter user Name")
                } else if (userGmail.isNullOrEmpty()) {
                    Utils.showToast(this, "Please enter user MailID")
                }  else if (userPhno.isNullOrEmpty()) {
                    Utils.showToast(this, "Please enter user Mobile Number")
                } else {
                    SessionSave.saveSession(Constants.NAME, userName, this)
                    SessionSave.saveSession(Constants.EMAIL, userGmail, this)
                    updateUser(userName, userGmail, model, imageEncoded)
                    createNotificationChannel("Update", "Hi $userName, Updated user profile successfully ", this)
                }
            }

        }

    }

    private fun updateUser(name: String, email: String, model: String, imageEncoded: String) {
        // updating the user via child nodes
        if (!model.isNullOrEmpty())
            mFirebaseDatabase!!.child(userPhno).child("model").setValue(model)
        if (!imageEncoded.isNullOrEmpty()) {
            mFirebaseDatabase!!.child(userPhno).child("imageEncoded").setValue(imageEncoded)
            SessionSave.saveSession(Constants.IMAGE,imageEncoded,this)
        }

        mFirebaseDatabase!!.child(userPhno).child("name").setValue(name)
        mFirebaseDatabase!!.child(userPhno).child("email").setValue(email)
        mFirebaseDatabase!!.child(userPhno).child("gps_Lat").setValue(SessionSave.getSession(Constants.GPSLAT, this))
        mFirebaseDatabase!!.child(userPhno).child("gps_Lng").setValue(SessionSave.getSession(Constants.GPSLNG, this))
        Utils.showToast(this, getString(R.string.updated_successfully))

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

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor: Cursor = this.contentResolver.query(
                        selectedImage!!,
                        filePathColumn,
                        null,
                        null,
                        null
                    )!!
                    if (cursor != null) {
                        cursor.moveToFirst()
                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        userPicturepath = cursor.getString(columnIndex)
                        cursor.close()
                        Utils.setImage(this, userPicturepath, activityEditProfileBinding.profileImg)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun retriveData() {
        mFirebaseDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    for (chidSnap in dataSnapshot.children) {
                        val all = chidSnap.key
                        userPhno = SessionSave.getSession(Constants.MOBILENO, this@EditProfileActivity).toString()
                        if (all != null && all.equals(userPhno, ignoreCase = true)) {
                            val userDetail: UserDetail? = chidSnap.getValue(UserDetail::class.java)
                            if (userDetail != null) {
                                SessionSave.saveSession(Constants.NAME, userDetail.name!!,this@EditProfileActivity)
                                SessionSave.saveSession(Constants.EMAIL, userDetail.email!!,this@EditProfileActivity)!!
                                if (!imageEncoded.equals("", ignoreCase = true))
                                    SessionSave.saveSession(Constants.IMAGE, userDetail.imageEncoded!!,this@EditProfileActivity)

                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {

            }
        })

    }

}