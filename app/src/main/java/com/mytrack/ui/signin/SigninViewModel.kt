package com.mytrack.ui.signin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.model.UserDetail
import com.mytrack.utils.Constants
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import android.util.Base64
import com.mytrack.R
import javax.crypto.Cipher
import java.security.Key

class SigninViewModel(application: Application) : AndroidViewModel(application) {

    private val _loginStatus = MutableLiveData<LoginResult>()
    val loginStatus: LiveData<LoginResult> = _loginStatus

    private val _userData = MutableLiveData<UserDetail?>()
    val userData: LiveData<UserDetail?> = _userData

    sealed class LoginResult {
        object Loading : LoginResult()
        data class Success(val userId: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
        data class ValidationError(val field: String, val messageResId: Int) : LoginResult()
    }

    fun signIn(phone: String, pass: String) {
        if (phone.isEmpty()) {
            _loginStatus.value = LoginResult.ValidationError("phone", R.string.phone_null)
            return
        }
        if (phone.length <= 9) {
            _loginStatus.value = LoginResult.ValidationError("phone", R.string.check_mobileno)
            return
        }
        if (pass.isEmpty()) {
            _loginStatus.value = LoginResult.ValidationError("password", R.string.Password_null)
            return
        }
        if (pass.length < 6) {
            _loginStatus.value = LoginResult.ValidationError("password", R.string.min_password)
            return
        }

        if (!Utils.isNetworkAvailable(getApplication())) {
            _loginStatus.value = LoginResult.Error("Please connect to internet")
            return
        }

        _loginStatus.value = LoginResult.Loading
        
        val database = Firebase.database.reference
        val userRef = database.child("users").child(phone)
        
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserDetail::class.java)
                if (user != null) {
                    try {
                        val decryptedPass = decrypt(user.encryptpwd ?: "")
                        if (decryptedPass == pass) {
                            saveUserSession(user)
                            updateFirebaseInfo(phone)
                            _loginStatus.value = LoginResult.Success(user.userId ?: "")
                        } else {
                            _loginStatus.value = LoginResult.ValidationError("password", R.string.check_password)
                        }
                    } catch (e: Exception) {
                        _loginStatus.value = LoginResult.Error("Encryption error")
                    }
                } else {
                    _loginStatus.value = LoginResult.ValidationError("phone", R.string.mobile_no_not_exit)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _loginStatus.value = LoginResult.Error(error.message)
            }
        })
    }

    private fun saveUserSession(user: UserDetail) {
        val context = getApplication<Application>()
        SessionSave.saveSession(Constants.USERSID, user.userId, context)
        SessionSave.saveSession(Constants.NAME, user.name, context)
        SessionSave.saveSession(Constants.EMAIL, user.email, context)
        SessionSave.saveSession(Constants.MOBILENO, user.phoneno, context)
        SessionSave.saveSession(Constants.IMAGE, user.imageEncoded, context)
        SessionSave.saveSession(Constants.GPSLAT, user.gpsLat.toString(), context)
        SessionSave.saveSession(Constants.GPSLNG, user.gpsLng.toString(), context)
        
        SessionSave.saveSessionInt(Constants.LOGINTYPE, 1, context)
        SessionSave.saveSession(Constants.ISLOGIN, true, context)
        SessionSave.saveSession(Constants.MODELID, Utils.getDeviceInfo(), context)
    }

    private fun updateFirebaseInfo(phone: String) {
        val database = Firebase.database.reference
        val userRef = database.child("users").child(phone)
        
        val deviceInfo = Utils.getDeviceInfo()
        if (deviceInfo.isNotEmpty()) {
            userRef.child("model").setValue(deviceInfo)
        }
        
        val token = SessionSave.getSession(Constants.TOKEN, getApplication())
        if (!token.isNullOrEmpty()) {
            userRef.child("token").setValue(token)
        }
    }

    private fun decrypt(data: String): String {
        return try {
            val key: Key = Utils.generateKey()
            val c = Cipher.getInstance(Utils.ALGO)
            c.init(Cipher.DECRYPT_MODE, key)
            val decodedValue = Base64.decode(data, Base64.DEFAULT)
            val decValue = c.doFinal(decodedValue)
            String(decValue)
        } catch (e: Exception) {
            ""
        }
    }
}
