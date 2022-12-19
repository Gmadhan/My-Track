package com.mytrack.ui.profile

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.R
import com.mytrack.databinding.FragmentProfileBinding
import com.mytrack.model.response.UserDetail
import com.mytrack.ui.MainActivity
import com.mytrack.utils.CommonBottomSheet
import com.mytrack.utils.Constants
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import com.mytrack.utils.Utils.encodeBitmapAndSaveToFirebase
import java.io.IOException

class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var fragmentProfileBinding: FragmentProfileBinding
    private var loading: LottieAnimationView? = null
    private var mFirebaseDatabase: DatabaseReference? = null

    private var user_name = ""
    private var user_picturePath = ""
    private var user_gmail = ""
    private var user_phno = ""
    private var userId = ""
    private val model = ""
    private var imageEncoded = ""

    val MY_PERMISSIONS_REQUEST_CAMERA = 112
    private val RESULT_LOAD_IMAGE = 1

    private var gps_lat: Double? = null
    private var gps_lng: Double? = null
    private val imageUri: Uri? = null
    private val TAG = "profileAct"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentProfileBinding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        init()
        return fragmentProfileBinding.root
    }

    fun init() {
        fragmentProfileBinding.header.txtHeaderName.text =
            requireActivity().getString(R.string.profile)
        fragmentProfileBinding.header.btnBack.visibility = View.GONE

        fragmentProfileBinding.txtProfileName.text = Utils.capitalizeWords(
            SessionSave.getSession(Constants.NAME, requireActivity()).toString()
        )
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        mFirebaseDatabase = Firebase.database.reference.child("users")

        fragmentProfileBinding.btnChangePassword.setOnClickListener(this)
        fragmentProfileBinding.btnLanguage.setOnClickListener(this)
        fragmentProfileBinding.btnLogout.setOnClickListener(this)
        /*fragmentProfileBinding.profileImg.setOnClickListener(this)
        fragmentProfileBinding.btnSubmit.setOnClickListener(this)*/

        Retrivedata()

    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.profile_img -> {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        if (ActivityCompat.checkSelfPermission(
                                requireActivity(),
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            ) !== PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                                requireActivity(),
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
                                requireActivity(),
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
                /*userId = SessionSave.getSession("UserId", requireActivity())!!
                user_name = fragmentProfileBinding.edtProfileId.text.toString()
                user_gmail = fragmentProfileBinding.edtProfileGmail.text.toString()
                user_phno = fragmentProfileBinding.edtProfilePhno.text.toString()

                if (user_name != "")
                    SessionSave.saveSession("Name", user_name, requireActivity())
                if (user_gmail != "")
                    SessionSave.saveSession("Email", user_gmail, requireActivity())
                if (user_phno != "")
                    SessionSave.saveSession("Mobileno", user_phno, requireActivity())
                if (userId != "")
                    updateUser(user_name, user_gmail, gps_lat, gps_lng, model, imageEncoded)

                createNotificationChannel("Update", "Hi $user_name, Updated user profile successfully ", requireActivity())
                Toast.makeText(requireActivity(), getString(R.string.updated_successfully), Toast.LENGTH_SHORT).show()*/
            }

            R.id.btnLanguage -> {
                openBottomSheet(getString(R.string.language))
            }

            R.id.btnChangePassword -> {
                openBottomSheet(getString(R.string.change_password))
            }

            R.id.btnLogout -> {
                Utils.showToast(requireActivity(), getString(R.string.logged_out_successfully))
                SessionSave.clearAllSession(requireActivity())
                SessionSave.saveSession(Constants.ISLOGIN, false, requireActivity())
                (activity as MainActivity).movetoLogin()
            }

        }
    }

    fun openBottomSheet(type: String) {
        val bottomSheet = CommonBottomSheet(requireActivity(), type)
        bottomSheet.setStyle(
            DialogFragment.STYLE_NORMAL,
            R.style.MyTransparentBottomSheetDialogTheme
        )
        bottomSheet.isCancelable = true
        bottomSheet.show(parentFragmentManager, type)
    }

    fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_PICK
        resultImageLauncher.launch(intent)
        /*startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Picture"
            ), RESULT_LOAD_IMAGE
        )*/
    }

    private var resultImageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            try {
                if (result.resultCode == RESULT_OK && result.data != null) {
                    val selectedImage = result.data!!.data
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                            requireActivity().contentResolver,
                            selectedImage
                        )
                        imageEncoded = encodeBitmapAndSaveToFirebase(bitmap)

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                    val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
                    val cursor: Cursor = requireActivity().contentResolver.query(
                        selectedImage!!,
                        filePathColumn,
                        null,
                        null,
                        null
                    )!!
                    if (cursor != null) {
                        cursor.moveToFirst()
                        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
                        user_picturePath = cursor.getString(columnIndex)
                        cursor.close()
//                    Utils.loadImage(
//                        fragmentProfileBinding.profileImg,
//                        user_picturePath,
//                        requireActivity().resources.getDrawable(R.drawable.ic_profileimage, null),
//                        null
//                    )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    private fun updateUser(
        name: String,
        email: String,
        gps_lat: Double?,
        gps_lng: Double?,
        model: String,
        imageEncoded: String
    ) {
        if (!TextUtils.isEmpty(name))
            mFirebaseDatabase!!.child(user_phno).child("name").setValue(name)
        if (!TextUtils.isEmpty(email))
            mFirebaseDatabase!!.child(user_phno).child("email").setValue(email)
        if (gps_lat != null)
            mFirebaseDatabase!!.child(user_phno).child("gps_Lat").setValue(gps_lat)
        if (gps_lng != null)
            mFirebaseDatabase!!.child(user_phno).child("gps_Lng").setValue(gps_lng)
        if (model != "")
            mFirebaseDatabase!!.child(user_phno).child("model").setValue(model)
        if (imageEncoded != "")
            mFirebaseDatabase!!.child(user_phno).child("imageEncoded").setValue(imageEncoded)
    }

    private fun Retrivedata() {
        mFirebaseDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    for (chidSnap in dataSnapshot.children) {
                        val all = chidSnap.key
                        user_phno =
                            SessionSave.getSession(Constants.MOBILENO, requireActivity()).toString()
                        if (all != null && all.equals(user_phno, ignoreCase = true)) {
                            val ud: UserDetail? = chidSnap.getValue(UserDetail::class.java)
                            if (ud != null) {
                                user_gmail = ud.email!!
                                user_name = ud.name!!
                                user_phno = ud.phoneno!!
                                imageEncoded = ud.imageEncoded!!
                                if (!imageEncoded.equals("", ignoreCase = true)) {
                                    imageEncoded = SessionSave.getSession(Constants.IMAGE, requireActivity())!!
                                    Utils.setImage(
                                        requireActivity(),
                                        SessionSave.getSession(Constants.IMAGE, context)!!,
                                        fragmentProfileBinding.inUserimage.ivUser
                                    )
                                }
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