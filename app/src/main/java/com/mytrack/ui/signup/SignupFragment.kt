package com.mytrack.ui.signup

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mytrack.R
import com.mytrack.databinding.FragmentSignupBinding
import com.mytrack.ui.onboard.OnBoardActivity
import com.mytrack.utils.Notify.createNotificationChannel
import com.mytrack.utils.Utils

class SignupFragment: Fragment() {
    private lateinit var fragmentSignupBinding: FragmentSignupBinding
    private lateinit var viewModel: SignupViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSignupBinding = FragmentSignupBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[SignupViewModel::class.java]
        init()
        observeViewModel()
        return fragmentSignupBinding.root
    }

    private fun observeViewModel() {
        viewModel.signupStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SignupViewModel.SignupResult.Loading -> {
                    Utils.showloader(requireActivity())
                }
                is SignupViewModel.SignupResult.Success -> {
                    fragmentSignupBinding.btnSignup.visibility = View.GONE
                    fragmentSignupBinding.btnSignupSuccess.visibility = View.VISIBLE
                    fragmentSignupBinding.btnSignupSuccess.playAnimation()

                    Handler(Looper.getMainLooper()).postDelayed({
                        Utils.dismissLoader()
                        createNotificationChannel(
                            getString(R.string.Sign_up),
                            "Hi " + fragmentSignupBinding.edtName.text.toString() + " " + getString(R.string.Successfully_signup),
                            requireActivity()
                        )
                        (activity as OnBoardActivity).movetoHome()
                    }, 1400)
                }
                is SignupViewModel.SignupResult.Error -> {
                    Utils.dismissLoader()
                    Utils.showToast(requireActivity(), result.message)
                }
                is SignupViewModel.SignupResult.ValidationError -> {
                    Utils.dismissLoader()
                    when (result.field) {
                        "userName" -> fragmentSignupBinding.edtName.error = getString(result.messageResId)
                        "email" -> fragmentSignupBinding.edtEmail.error = getString(result.messageResId)
                        "mobileNo" -> fragmentSignupBinding.edtMobileNo.error = getString(result.messageResId)
                        "password" -> fragmentSignupBinding.edtSignupPwd.error = getString(result.messageResId)
                        "confirmpassword" -> fragmentSignupBinding.edtSignupCPwd.error = getString(result.messageResId)
                    }
                }
            }
        }
    }

    fun init() {
        fragmentSignupBinding.tbSignupPwd.setOnClickListener {
            fragmentSignupBinding.edtSignupPwd.transformationMethod = if (fragmentSignupBinding.tbSignupPwd.isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        }

        fragmentSignupBinding.tbSignupCPwd.setOnClickListener {
            fragmentSignupBinding.edtSignupCPwd.transformationMethod = if (fragmentSignupBinding.tbSignupCPwd.isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        }

        fragmentSignupBinding.btnSignup.setOnClickListener {
            viewModel.signup(
                fragmentSignupBinding.edtName.text.toString(),
                fragmentSignupBinding.edtEmail.text.toString(),
                fragmentSignupBinding.edtMobileNo.text.toString(),
                fragmentSignupBinding.edtSignupPwd.text.toString(),
                fragmentSignupBinding.edtSignupCPwd.text.toString()
            )
        }
    }
}
