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
    private val TAG = "ProfileFragment"

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
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        fragmentProfileBinding.header.txtHeaderName.text = requireActivity().getString(R.string.profile)
        fragmentProfileBinding.header.btnBack.visibility = View.GONE
        fragmentProfileBinding.txtProfileName.text = Utils.capitalizeWords(SessionSave.getSession(Constants.NAME, requireActivity()).toString())
        Utils.setImage(requireActivity(), SessionSave.getSession(Constants.IMAGE, context)!!, fragmentProfileBinding.inUserimage.ivUser)

        fragmentProfileBinding.btnChangePassword.setOnClickListener(this)
        fragmentProfileBinding.btnLanguage.setOnClickListener(this)
        fragmentProfileBinding.btnLogout.setOnClickListener(this)
        fragmentProfileBinding.inUserimage.ivUser.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v?.id) {

            R.id.ivUser -> {
                val i = Intent(requireActivity(), EditProfileActivity::class.java)
                startActivity(i)
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

}