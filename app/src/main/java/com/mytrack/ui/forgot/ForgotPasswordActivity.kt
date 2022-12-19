package com.mytrack.ui.forgot

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.view.View
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.database.*
import com.mytrack.R
import com.mytrack.databinding.ActivityForgotpwdBinding
import com.mytrack.model.response.UserDetail
import com.mytrack.ui.onboard.OnBoardActivity
import com.mytrack.utils.Notify.createNotificationChannel
import com.mytrack.utils.Utils
import com.mytrack.utils.Utils.toString
import java.security.Key
import java.util.*
import javax.crypto.Cipher

class ForgotPasswordActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var activityForgotpwdBinding: ActivityForgotpwdBinding
    private var mFirebaseDatabase: DatabaseReference? = null
    private var TAG: String? = "ForgotPasswordActivity"
    private var phoneno: String? = null
    private var otp: String? = null
    private var password: String? = null
    private var confirmPassword: String? = null
    private var verifyOTP: String? = null
    private val random = Random()
    private var OTPResult: Int = 0
    private var bln_password = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityForgotpwdBinding = ActivityForgotpwdBinding.inflate(layoutInflater)
        setContentView(activityForgotpwdBinding.root)
        init()
    }

    fun init() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        activityForgotpwdBinding.header.txtHeaderName.text = getString(R.string.forgot_pwd)
        activityForgotpwdBinding.header.btnBack.setOnClickListener { onBackPressed() }

        activityForgotpwdBinding.tbPwd.setOnClickListener(this)
        activityForgotpwdBinding.tbCpwd.setOnClickListener(this)
        activityForgotpwdBinding.btnVerify.setOnClickListener(this)
        activityForgotpwdBinding.btnSubmit.setOnClickListener(this)
    }

    private fun retrivedata() {
        mFirebaseDatabase!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    for (chidSnap in dataSnapshot.children) {
                        val all = chidSnap.key
                        if (all != null && all == activityForgotpwdBinding.edtUserID.text.toString()) {
                            val ud: UserDetail? = chidSnap.getValue(UserDetail::class.java)
                            phoneno = ud!!.phoneno
                            validate(phoneno!!)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {}
        })
    }

    @SuppressLint("DefaultLocale")
    private fun validate(user: String) {
        OTPResult++
        if (!user.isNullOrEmpty() && OTPResult == 1) {
            otp = String.format("%04d", random.nextInt(10000))
            createNotificationChannel(
                getString(R.string.name),
                getString(R.string.description) + " " + otp,
                this
            )
            activityForgotpwdBinding.progressBar.visibility = View.INVISIBLE
            activityForgotpwdBinding.edtOTP.visibility = View.VISIBLE
            activityForgotpwdBinding.btnVerify.visibility = View.GONE
            activityForgotpwdBinding.btnSubmit.visibility = View.VISIBLE
        }
    }

    private fun passwordupdate(password: String) {
        phoneno = activityForgotpwdBinding.edtUserID.text.toString()
        if (!password.equals("", ignoreCase = true)) {
            mFirebaseDatabase!!.child(phoneno!!).child("encryptpwd").setValue(password)
            val i = Intent(this, OnBoardActivity::class.java)
            i.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(i)
            Utils.dismissLoader()
            finish()
        }
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btn_submit -> {
                Utils.showloader(this)
                verifyOTP = activityForgotpwdBinding.edtOTP.text.toString()
                if (!verifyOTP.isNullOrEmpty() && verifyOTP!!.length == 4) {
                    if (otp == verifyOTP) {
                        if (activityForgotpwdBinding.llPwd.visibility == View.GONE && activityForgotpwdBinding.llCpwd.visibility == View.GONE) {
                            activityForgotpwdBinding.llPwd.visibility = View.VISIBLE
                            activityForgotpwdBinding.llCpwd.visibility = View.VISIBLE
                            bln_password = true
                            Utils.dismissLoader()
                        } else {
                            bln_password = true
                            if (bln_password && activityForgotpwdBinding.llPwd.visibility == View.VISIBLE && activityForgotpwdBinding.llCpwd.visibility == View.VISIBLE) {
                                password = activityForgotpwdBinding.edtPwd.text.toString()
                                confirmPassword = activityForgotpwdBinding.edtConfirmPwd.text.toString()
                                if (password.isNullOrEmpty()) {
                                    activityForgotpwdBinding.edtPwd.error = getString(R.string.Password_null)
                                    Utils.dismissLoader()
                                } else if (confirmPassword.isNullOrEmpty()) {
                                    activityForgotpwdBinding.edtConfirmPwd.error = getString(R.string.confirm_Password_null)
                                    Utils.dismissLoader()
                                } else if (password!!.length < 6) {
                                    activityForgotpwdBinding.edtPwd.error = getString(R.string.min_password)
                                    Utils.dismissLoader()
                                } else if (confirmPassword.isNullOrEmpty()) {
                                    activityForgotpwdBinding.edtConfirmPwd.error = getString(R.string.min_confirm_password)
                                    Utils.dismissLoader()
                                } else if (confirmPassword != password) {
                                    activityForgotpwdBinding.edtConfirmPwd.error = getString(R.string.password_mismatch)
                                    Utils.dismissLoader()
                                } else {
                                    try {
                                        activityForgotpwdBinding.progressBar.visibility = View.VISIBLE
                                        activityForgotpwdBinding.progressBar.playAnimation()
                                        encrypt(password!!)
                                    } catch (e: java.lang.Exception) {
                                        e.printStackTrace()
                                        Utils.dismissLoader()
                                    }
                                }
                            }
                        }
                    } else {
                        activityForgotpwdBinding.edtOTP.error = getString(R.string.check_otp)
                        Utils.dismissLoader()
                    }
                }
            }

            R.id.btn_verify -> {
                activityForgotpwdBinding.progressBar.visibility = View.VISIBLE
                activityForgotpwdBinding.progressBar.playAnimation()
                phoneno = activityForgotpwdBinding.edtUserID.text.toString()
                if (phoneno!!.length == 10)
                    retrivedata()
                else if (phoneno!!.length <= 9)
                    activityForgotpwdBinding.edtUserID.error = getString(R.string.check_mobileno)
                else if (phoneno.isNullOrEmpty())
                    activityForgotpwdBinding.edtUserID.error = getString(R.string.phone_null)
            }

            R.id.tb_pwd -> {
                if (activityForgotpwdBinding.tbPwd.isChecked)
                    activityForgotpwdBinding.edtPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                else
                    activityForgotpwdBinding.edtPwd.transformationMethod = PasswordTransformationMethod.getInstance()
            }

            R.id.tb_pwd -> {
                if (activityForgotpwdBinding.tbCpwd.isChecked)
                    activityForgotpwdBinding.edtConfirmPwd.transformationMethod = HideReturnsTransformationMethod.getInstance()
                else
                    activityForgotpwdBinding.edtConfirmPwd.transformationMethod = PasswordTransformationMethod.getInstance()
            }

        }

    }

    @Throws(java.lang.Exception::class)
    fun encrypt(data: String) {
        val key: Key = Utils.generateKey()
        @SuppressLint("GetInstance")
        val c = Cipher.getInstance(Utils.ALGO)
        c.init(Cipher.ENCRYPT_MODE, key)
        val encVal = c.doFinal(data.toByteArray())
        val encodeValue = Base64.encode(encVal, Base64.DEFAULT)
        activityForgotpwdBinding.btnSubmit.visibility = View.GONE
        activityForgotpwdBinding.btnVerify.visibility = View.GONE
        activityForgotpwdBinding.progressBar.visibility = View.INVISIBLE
        activityForgotpwdBinding.success.visibility = View.VISIBLE
        passwordupdate(String(encodeValue))
    }

}