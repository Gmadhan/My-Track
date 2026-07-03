package com.mytrack.ui.profile

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

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val _userData = MutableLiveData<UserDetail?>()
    val userData: LiveData<UserDetail?> = _userData

    private val _updateStatus = MutableLiveData<UpdateResult>()
    val updateStatus: LiveData<UpdateResult> = _updateStatus

    sealed class UpdateResult {
        object Loading : UpdateResult()
        object Success : UpdateResult()
        data class Error(val message: String) : UpdateResult()
    }

    private val database = Firebase.database.reference
    private var userPhno: String = ""

    init {
        userPhno = SessionSave.getSession(Constants.MOBILENO, getApplication()) ?: ""
        if (userPhno.isNotEmpty()) {
            fetchUserData()
        }
    }

    private fun fetchUserData() {
        database.child(userPhno).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserDetail::class.java)
                _userData.value = user
                user?.let {
                    saveUserToSession(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun saveUserToSession(user: UserDetail) {
        val context = getApplication<Application>()
        SessionSave.saveSession(Constants.NAME, user.name ?: "", context)
        SessionSave.saveSession(Constants.EMAIL, user.email ?: "", context)
        if (!user.imageEncoded.isNullOrEmpty()) {
            SessionSave.saveSession(Constants.IMAGE, user.imageEncoded, context)
        }
    }

    fun updateUser(name: String, email: String, imageEncoded: String?) {
        if (userPhno.isEmpty()) return

        _updateStatus.value = UpdateResult.Loading

        val updates = mutableMapOf<String, Any>()
        updates["name"] = name
        updates["email"] = email
        imageEncoded?.let {
            updates["imageEncoded"] = it
        }
        
        // Also update GPS if available in session
        val context = getApplication<Application>()
        SessionSave.getSession(Constants.GPSLAT, context)?.let { updates["gps_Lat"] = it }
        SessionSave.getSession(Constants.GPSLNG, context)?.let { updates["gps_Lng"] = it }

        database.child(userPhno).updateChildren(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                _updateStatus.value = UpdateResult.Success
                SessionSave.saveSession(Constants.NAME, name, context)
                SessionSave.saveSession(Constants.EMAIL, email, context)
                imageEncoded?.let { SessionSave.saveSession(Constants.IMAGE, it, context) }
            } else {
                _updateStatus.value = UpdateResult.Error(task.exception?.message ?: "Update failed")
            }
        }
    }

    fun logout() {
        val context = getApplication<Application>()
        SessionSave.clearAllSession(context)
        SessionSave.saveSession(Constants.ISLOGIN, false, context)
    }
}
