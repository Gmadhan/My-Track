package com.mytrack.ui.signin

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mytrack.R
import com.mytrack.databinding.FragmentSigninBinding
import com.mytrack.ui.forgot.ForgotPasswordActivity
import com.mytrack.ui.onboard.OnBoardActivity
import com.mytrack.utils.Notify
import com.mytrack.utils.Utils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod

class SigninFragment : Fragment() {
    private lateinit var fragmentSigninBinding: FragmentSigninBinding
    private lateinit var viewModel: SigninViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSigninBinding = FragmentSigninBinding.inflate(layoutInflater, container, false)
        viewModel = ViewModelProvider(this)[SigninViewModel::class.java]
        init()
        observeViewModel()
        return fragmentSigninBinding.root
    }

    private fun observeViewModel() {
        viewModel.loginStatus.observe(viewLifecycleOwner) { result ->
            when (result) {
                is SigninViewModel.LoginResult.Loading -> {
                    Utils.showloader(requireActivity())
                }
                is SigninViewModel.LoginResult.Success -> {
                    Utils.dismissLoader()
                    fragmentSigninBinding.btnSignin.visibility = View.GONE
                    fragmentSigninBinding.btnSigninSuccess.visibility = View.VISIBLE
                    fragmentSigninBinding.btnSigninSuccess.playAnimation()
                    Handler(Looper.getMainLooper()).postDelayed({
                        (activity as OnBoardActivity).movetoHome()
                    }, 1500)
                }
                is SigninViewModel.LoginResult.Error -> {
                    Utils.dismissLoader()
                    Notify.alertView(
                        getString(R.string.Network_Connection),
                        requireActivity(),
                        "",
                        0,
                        result.message,
                        getString(R.string.Ok),
                        ""
                    )
                }
                is SigninViewModel.LoginResult.ValidationError -> {
                    Utils.dismissLoader()
                    when (result.field) {
                        "phone" -> fragmentSigninBinding.edtUserID.error = getString(result.messageResId)
                        "password" -> fragmentSigninBinding.edtPassword.error = getString(result.messageResId)
                    }
                }
            }
        }
    }

    fun init() {
        fragmentSigninBinding.tbSigninPwd.setOnClickListener {
            fragmentSigninBinding.edtPassword.transformationMethod =
                if (fragmentSigninBinding.tbSigninPwd.isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        }

        fragmentSigninBinding.btnSignin.setOnClickListener {
            val phone = fragmentSigninBinding.edtUserID.text.toString()
            val pass = fragmentSigninBinding.edtPassword.text.toString()
            viewModel.signIn(phone, pass)
        }

        fragmentSigninBinding.btnForgetPwd.setOnClickListener {
            val i = Intent(requireActivity(), ForgotPasswordActivity::class.java)
            startActivity(i)
        }
    }
}
