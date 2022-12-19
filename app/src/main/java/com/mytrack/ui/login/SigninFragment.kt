package com.mytrack.ui.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.R
import com.mytrack.databinding.FragmentSigninBinding
import com.mytrack.model.response.UserDetail
import com.mytrack.ui.forgot.ForgotPasswordActivity
import com.mytrack.ui.onboard.OnBoardActivity
import com.mytrack.utils.*
import com.mytrack.utils.Notify.alertView
import com.mytrack.utils.Utils.getDeviceInfo
import com.mytrack.utils.Utils.logger
import com.mytrack.utils.Utils.toString
import java.security.Key
import javax.crypto.Cipher


class SigninFragment : Fragment() {
    private lateinit var fragmentSigninBinding: FragmentSigninBinding
    private lateinit var mFirebaseDatabase: DatabaseReference
    private var TAG: String = "SigninFragment"
    private var mobileno: String? = null
    private var userID: String? = null
    private var password: String? = null
    private var encryptpwd: String? = null
    private var decryptpwd: String? = null
    private var userDetail: UserDetail? = null
    var i = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentSigninBinding = FragmentSigninBinding.inflate(layoutInflater, container, false)
        init()
        return fragmentSigninBinding.root
    }

    fun init() {
        fragmentSigninBinding.edtUserID.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                val phno = s.toString()
                if (phno.length == 10) {

                }
                /*else if (phno.length <= 9 || phno.length > 10) {
                    fragmentSigninBinding.edtUserID.error = getString(R.string.check_mobileno)
                }*/
            }
        })

        fragmentSigninBinding.tbSigninPwd.setOnClickListener {
            fragmentSigninBinding.edtPassword.transformationMethod =
                if (fragmentSigninBinding.tbSigninPwd.isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
        }
        FirebaseApp.initializeApp(requireActivity())

        fragmentSigninBinding.btnSignin.setOnClickListener {
            Retrivedata()
        }

        fragmentSigninBinding.btnForgetPwd.setOnClickListener {
            val i = Intent(requireActivity(), ForgotPasswordActivity::class.java)
            startActivity(i)
        }
    }

    private fun signinValidate() {
        if (Utils.isNetworkAvailable(requireActivity())) {
            if (fragmentSigninBinding.edtUserID.text.isNullOrEmpty())
                fragmentSigninBinding.edtUserID.error = getString(R.string.phone_null)
            else if (fragmentSigninBinding.edtUserID.text.toString().length <= 9)
                fragmentSigninBinding.edtUserID.error = getString(R.string.check_mobileno)
            else if (fragmentSigninBinding.edtPassword.text.isNullOrEmpty())
                fragmentSigninBinding.edtPassword.error = getString(R.string.Password_null)
            else if (fragmentSigninBinding.edtPassword.text.toString().length < 6)
                fragmentSigninBinding.edtPassword.error = getString(R.string.min_password)
            else if (!Notify.mobileArr.contains(fragmentSigninBinding.edtUserID.text.toString()))
                fragmentSigninBinding.edtUserID.error = getString(R.string.mobile_no_not_exit)
            else if (fragmentSigninBinding.edtPassword.text.toString() != decryptpwd)
                fragmentSigninBinding.edtPassword.error = getString(R.string.check_password)
            else {
                Utils.showloader(requireActivity())
                SessionSave.saveSession(
                    Constants.MOBILENO,
                    fragmentSigninBinding.edtUserID.text.toString(),
                    requireActivity()
                )
                SessionSave.saveSessionInt(Constants.LOGINTYPE, 1, requireActivity())
                SessionSave.saveSession(Constants.ISLOGIN, true, requireActivity())
                SessionSave.saveSession(Constants.USERSID, userID, requireActivity())
                SessionSave.saveSession(Constants.MODELID, getDeviceInfo(), requireActivity())
                fragmentSigninBinding.btnSignin.visibility = View.GONE
                fragmentSigninBinding.btnSigninSuccess.visibility = View.VISIBLE
                fragmentSigninBinding.btnSigninSuccess.playAnimation()
                if (!getDeviceInfo().equals("", ignoreCase = true))
                    mFirebaseDatabase!!.child(mobileno!!).child("model").setValue(getDeviceInfo())
                if (!Notify.DeviceToken.isNullOrEmpty())
                    mFirebaseDatabase!!.child(mobileno!!).child("token")
                        .setValue(Notify.DeviceToken)

                Handler(Looper.getMainLooper()).postDelayed({
                    Utils.dismissLoader()
                    (activity as OnBoardActivity).movetoHome()
                }, 1400)
            }
        } else {
            alertView(
                getString(R.string.Network_Connection),
                requireActivity(),
                "",
                0,
                getString(R.string.please_connect_internet),
                getString(R.string.Ok),
                ""
            )
        }
    }

    private fun Retrivedata() {
        val mFirebaseInstance = Firebase.database.reference
        mFirebaseDatabase = mFirebaseInstance.child("users").child(fragmentSigninBinding.edtUserID.text.toString())
        mFirebaseInstance.child("app").setValue("My Track")
        mFirebaseDatabase.child("phoneno").setValue(fragmentSigninBinding.edtUserID.text.toString())
        mFirebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    var phoneNo: String = ""
                    Notify.mobileArr.clear()
                    for (chidSnap in dataSnapshot.children) {
                        phoneNo = chidSnap.value.toString()
                        Notify.mobileArr.add(phoneNo)
                        if (phoneNo != null && phoneNo.equals(
                                fragmentSigninBinding.edtUserID.text.toString(),
                                ignoreCase = true
                            )
                        ) {
                            logger(TAG, "all data : $dataSnapshot")
                            var userData: UserDetail? = dataSnapshot.getValue(UserDetail::class.java)
                            mobileno = userData?.phoneno
                            password = userData?.encryptpwd
                            userID = userData?.userId
                            setUserDetails(userData!!)
                            try {
                                crypt(password, false)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                logger(TAG, "error == > $databaseError")
            }
        })
    }

    fun setUserDetails (userData: UserDetail) {
        SessionSave.saveSession(Constants.USERSID, userData.userId, requireActivity())
        SessionSave.saveSession(Constants.NAME, userData.name, requireActivity())
        SessionSave.saveSession(Constants.EMAIL, userData.email, requireActivity())
        SessionSave.saveSession(Constants.MOBILENO, userData.phoneno, requireActivity())
        SessionSave.saveSession(Constants.TOKEN, userData.token, requireActivity())
        SessionSave.saveSession(Constants.IMAGE, userData.imageEncoded, requireActivity())
        SessionSave.saveSession(Constants.MODELID, userData.model, requireActivity())
        SessionSave.saveSession(Constants.GPSLAT, userData.gpsLat.toString(), requireActivity())
        SessionSave.saveSession(Constants.GPSLNG, userData.gpsLng.toString(), requireActivity())
    }

    @Throws(java.lang.Exception::class)
    fun crypt(data: String?, encode: Boolean) {
        val key: Key = Utils.generateKey()
        @SuppressLint("GetInstance") val c = Cipher.getInstance(Utils.ALGO)
        if (encode) {
            c.init(Cipher.ENCRYPT_MODE, key)
            val encVal = c.doFinal(data!!.toByteArray())
            val encodeValue = Base64.encode(encVal, Base64.DEFAULT)
            encryptpwd = String(encodeValue)
        } else {
            c.init(Cipher.DECRYPT_MODE, key)
            val decordedValue = Base64.decode(data, Base64.DEFAULT)
            val decValue = c.doFinal(decordedValue)
            decryptpwd = String(decValue)
            i++
            if (i == 1) {
                i = 0
                signinValidate()
            }
        }
    }

}