package com.mytrack.ui.signup

import android.app.Application
import android.util.Base64
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.R
import com.mytrack.model.UserDetail
import com.mytrack.utils.Constants
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import javax.crypto.Cipher
import java.security.Key

class SignupViewModel(application: Application) : AndroidViewModel(application) {

    private val _signupStatus = MutableLiveData<SignupResult>()
    val signupStatus: LiveData<SignupResult> = _signupStatus

    sealed class SignupResult {
        object Loading : SignupResult()
        object Success : SignupResult()
        data class Error(val message: String) : SignupResult()
        data class ValidationError(val field: String, val messageResId: Int) : SignupResult()
    }

    fun signup(
        userName: String,
        email: String,
        mobileNo: String,
        password: String,
        confirmpassword: String
    ) {
        if (userName.isEmpty()) {
            _signupStatus.value = SignupResult.ValidationError("userName", R.string.username_null)
            return
        }
        if (email.isEmpty()) {
            _signupStatus.value = SignupResult.ValidationError("email", R.string.email_null)
            return
        }
        if (!Utils.isValidEmail(email)) {
            _signupStatus.value = SignupResult.ValidationError("email", R.string.check_email)
            return
        }
        if (mobileNo.isEmpty()) {
            _signupStatus.value = SignupResult.ValidationError("mobileNo", R.string.phone_null)
            return
        }
        if (mobileNo.length < 10) {
            _signupStatus.value = SignupResult.ValidationError("mobileNo", R.string.check_mobileno)
            return
        }
        if (password.isEmpty()) {
            _signupStatus.value = SignupResult.ValidationError("password", R.string.phone_null) // R.string.password_null?
            return
        }
        if (password.length < 6) {
            _signupStatus.value = SignupResult.ValidationError("password", R.string.min_password)
            return
        }
        if (confirmpassword.isEmpty()) {
            _signupStatus.value = SignupResult.ValidationError("confirmpassword", R.string.confirm_Password_null)
            return
        }
        if (confirmpassword.length < 6) {
            _signupStatus.value = SignupResult.ValidationError("confirmpassword", R.string.min_confirm_password)
            return
        }
        if (confirmpassword != password) {
            _signupStatus.value = SignupResult.ValidationError("confirmpassword", R.string.password_mismatch)
            return
        }

        _signupStatus.value = SignupResult.Loading

        try {
            val encryptpwd = encrypt(confirmpassword)
            
            val context = getApplication<Application>()
            SessionSave.saveSessionInt(Constants.LOGINTYPE, 2, context)
            SessionSave.saveSession(Constants.ISLOGIN, true, context)
            SessionSave.saveSession(Constants.NAME, userName, context)
            SessionSave.saveSession(Constants.EMAIL, email, context)
            SessionSave.saveSession(Constants.MOBILENO, mobileNo, context)
            SessionSave.saveSession(Constants.LANGUAGE, "en", context)
            SessionSave.saveSession(Constants.PIPMODE, false, context)

            val userId = SessionSave.getSession(Constants.USERSID, context) ?: Firebase.database.reference.push().key ?: ""
            if (SessionSave.getSession(Constants.USERSID, context).isNullOrEmpty()) {
                SessionSave.saveSession(Constants.USERSID, userId, context)
            }

            val model = SessionSave.getSession(Constants.MODELID, context) ?: ""
            val token = SessionSave.getSession(Constants.TOKEN, context) ?: ""

            val user = UserDetail(0.0, userName, "", model, 0.0, userId, email, mobileNo, encryptpwd, token)
            
            Firebase.database.reference.child(mobileNo).setValue(user).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signupStatus.value = SignupResult.Success
                } else {
                    _signupStatus.value = SignupResult.Error(task.exception?.message ?: "Signup failed")
                }
            }

        } catch (e: Exception) {
            _signupStatus.value = SignupResult.Error(e.message ?: "An error occurred")
        }
    }

    private fun encrypt(data: String): String {
        val key: Key = Utils.generateKey()
        val c = Cipher.getInstance(Utils.ALGO)
        c.init(Cipher.ENCRYPT_MODE, key)
        val encVal = c.doFinal(data.toByteArray())
        val encodeValue = Base64.encode(encVal, Base64.DEFAULT)
        return String(encodeValue)
    }
}
