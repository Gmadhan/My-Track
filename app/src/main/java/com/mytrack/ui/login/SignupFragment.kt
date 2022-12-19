package com.mytrack.ui.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.R
import com.mytrack.databinding.FragmentSignupBinding
import com.mytrack.model.response.UserDetail
import com.mytrack.ui.onboard.OnBoardActivity
import com.mytrack.utils.Constants
import com.mytrack.utils.Notify.createNotificationChannel
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import java.security.Key
import javax.crypto.Cipher

class SignupFragment: Fragment() {
    private lateinit var fragmentSignupBinding: FragmentSignupBinding
//    private var mFirebaseDatabase: DatabaseReference? = null

    private var TAG: String = "SignupFragment"
    private var userName: String? = null
    private var email: String? = null
    private var mobileNo: String? = null
    private var password: String? = null
    private var confirmpassword: String? = null
    private var encryptpwd: String? = null
    private var decryptpwd: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSignupBinding = FragmentSignupBinding.inflate(layoutInflater, container, false)
        init()
        return fragmentSignupBinding.root
    }

    fun init() {
        try {
            val mFirebaseInstance = Firebase.database.reference
            mFirebaseInstance.child("app").setValue("My Track")
//            mFirebaseDatabase = mFirebaseInstance
        } catch (e: Exception) {
            e.printStackTrace()
        }

        fragmentSignupBinding.edtMobileNo.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val phno = s.toString()
                if (phno.length == 10) {

                }
            }
        })

        fragmentSignupBinding.tbSignupPwd.setOnClickListener {
            fragmentSignupBinding.edtSignupPwd.transformationMethod = if (fragmentSignupBinding.tbSignupPwd.isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        }

        fragmentSignupBinding.tbSignupCPwd.setOnClickListener {
            fragmentSignupBinding.edtSignupCPwd.transformationMethod = if (fragmentSignupBinding.tbSignupCPwd.isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        }

        fragmentSignupBinding.btnSignup.setOnClickListener {
            signupValidate()
        }
    }

    private fun signupValidate() {
        userName = fragmentSignupBinding.edtName.text.toString()
        email = fragmentSignupBinding.edtEmail.text.toString()
        mobileNo = fragmentSignupBinding.edtMobileNo.text.toString()
        password = fragmentSignupBinding.edtSignupPwd.text.toString()
        confirmpassword = fragmentSignupBinding.edtSignupCPwd.text.toString()
        if (userName.isNullOrEmpty())
            fragmentSignupBinding.edtName.error = getString(R.string.username_null)
        else if (email.isNullOrEmpty())
            fragmentSignupBinding.edtEmail.error = getString(R.string.email_null)
        else if (mobileNo.isNullOrEmpty())
            fragmentSignupBinding.edtMobileNo.error = getString(R.string.phone_null)
        else if (mobileNo!!.length < 10)
            fragmentSignupBinding.edtMobileNo.error = getString(R.string.check_mobileno)
        else if (password.isNullOrEmpty())
            fragmentSignupBinding.edtSignupPwd.error = getString(R.string.phone_null)
        else if (password!!.length < 6)
            fragmentSignupBinding.edtSignupPwd.error = getString(R.string.min_password)
        else if (confirmpassword.isNullOrEmpty())
            fragmentSignupBinding.edtSignupCPwd.error = getString(R.string.confirm_Password_null)
        else if (confirmpassword!!.length < 6)
            fragmentSignupBinding.edtSignupCPwd.error = getString(R.string.min_confirm_password)
        else if (confirmpassword != password)
            fragmentSignupBinding.edtSignupCPwd.error = getString(R.string.password_mismatch)
        else if (!Utils.isValidEmail(email!!))
            fragmentSignupBinding.edtEmail.error = getString(R.string.check_email)
        else if (fragmentSignupBinding.edtMobileNo.error == null) {
            Utils.showloader(requireActivity())
            try {
                crypt(confirmpassword, true)
            } catch (e: Exception) {
                e.printStackTrace()
                Utils.dismissLoader()
            }
            SessionSave.saveSessionInt(Constants.LOGINTYPE, 2, requireActivity())
            SessionSave.saveSession(Constants.ISLOGIN, true, requireActivity())
            SessionSave.saveSession(Constants.NAME, userName, requireActivity())
            SessionSave.saveSession(Constants.EMAIL, email, requireActivity())
            SessionSave.saveSession(Constants.MOBILENO, mobileNo, requireActivity())
            SessionSave.saveSession(Constants.LANGUAGE, "en", requireActivity())
            SessionSave.saveSession(Constants.PIPMODE, false, requireActivity())
            fragmentSignupBinding.btnSignup.visibility = View.GONE
            fragmentSignupBinding.btnSignupSuccess.visibility = View.VISIBLE
            fragmentSignupBinding.btnSignupSuccess.playAnimation()

            Handler(Looper.getMainLooper()).postDelayed({
                createUser(
                    userName!!,
                    email!!,
                    mobileNo!!,
                    0.0,
                    0.0,
                    SessionSave.getSession(Constants.USERSID, requireActivity())!!,
                    SessionSave.getSession(Constants.MODELID, requireActivity())!!,
                    "",
                    encryptpwd!!,
                    SessionSave.getSession(Constants.TOKEN, requireActivity())!!
                )
            }, 5000)
            Handler(Looper.getMainLooper()).postDelayed({
                Utils.dismissLoader()
                createNotificationChannel(getString(R.string.Sign_up), "Hi " + userName + " " + getString(R.string.Successfully_signup), requireActivity())
                (activity as OnBoardActivity).movetoHome()
            }, 1400)
        } else {
            Utils.showToast(requireActivity(), getString(R.string.something_went_wrong))
        }

    }

    private fun createUser(name: String, email: String, phoneno: String, gpsLat: Double, gpsLng: Double, userId: String, model: String, imageEncoded: String, Encryptpwd: String, Token: String) {
        // TODO
        // In real apps this userId should be fetched
        // by implementing firebase auth
        var userId: String? = userId
        try {
            if (TextUtils.isEmpty(userId)) {
                userId = Firebase.database.reference.push().key
                try {
                    if (userId != null)
                        SessionSave.saveSession(Constants.USERSID, userId, requireActivity())
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
//            val user = userdetails(name, email, phoneno, gpsLat, gpsLng, userId, model, imageEncoded, Encryptpwd, Token)
            val user = UserDetail(gpsLng, name, imageEncoded, model, gpsLat, userId, email, phoneno, Encryptpwd, Token)
            Firebase.database.reference.child(mobileNo!!).setValue(user)
            addUserChangeListener()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * User data change listener
     */
    private fun addUserChangeListener() {
        // User data change listener
        Firebase.database.reference.child(mobileNo!!).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    val user: UserDetail? = dataSnapshot.getValue(UserDetail::class.java)
                    Utils.logger(TAG, "userdetails--$user")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Utils.logger(TAG, "Failed to read user " + error.toException(), true)
            }
        })
    }

    @Throws(java.lang.Exception::class)
    fun crypt(data: String?, encode: Boolean) {
        val key: Key = Utils.generateKey()
        @SuppressLint("GetInstance") val c = Cipher.getInstance(Utils.ALGO)
        if(encode) {
            c.init(Cipher.ENCRYPT_MODE, key)
            val encVal = c.doFinal(data!!.toByteArray())
            val encodeValue = Base64.encode(encVal, Base64.DEFAULT)
            encryptpwd = String(encodeValue)
        } else {
            c.init(Cipher.DECRYPT_MODE, key)
            val decordedValue = Base64.decode(data, Base64.DEFAULT)
            val decValue = c.doFinal(decordedValue)
            decryptpwd = String(decValue)
        }
    }

}