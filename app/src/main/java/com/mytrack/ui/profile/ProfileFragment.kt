package com.mytrack.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mytrack.R
import com.mytrack.databinding.FragmentProfileBinding
import com.mytrack.ui.MainActivity
import com.mytrack.utils.CommonBottomSheet
import com.mytrack.utils.Constants
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils

class ProfileFragment : Fragment(), View.OnClickListener {
    private lateinit var fragmentProfileBinding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentProfileBinding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]
        init()
        observeViewModel()
        return fragmentProfileBinding.root
    }

    private fun observeViewModel() {
        viewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                fragmentProfileBinding.txtProfileName.text = Utils.capitalizeWords(it.name ?: "")
                Utils.setImage(requireActivity(), it.imageEncoded ?: "", fragmentProfileBinding.inUserimage.ivUser)
            }
        }
    }

    fun init() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        fragmentProfileBinding.header.txtHeaderName.text = requireActivity().getString(R.string.profile)
        fragmentProfileBinding.header.btnBack.visibility = View.GONE

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
                viewModel.logout()
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