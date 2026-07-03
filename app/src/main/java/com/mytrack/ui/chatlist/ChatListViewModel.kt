package com.mytrack.ui.chatlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.*
import com.mytrack.model.ContactsData
import com.mytrack.utils.Constants
import com.mytrack.utils.Notify
import com.mytrack.utils.SessionSave
import java.util.*

class ChatListViewModel(application: Application) : AndroidViewModel(application) {

    private val _contacts = MutableLiveData<List<ContactsData>>()
    val contacts: LiveData<List<ContactsData>> = _contacts

    private val _availableUsers = MutableLiveData<List<String>>()
    val availableUsers: LiveData<List<String>> = _availableUsers

    private val database = FirebaseDatabase.getInstance()
    private val friendListRef = database.getReference("friendlist")
    private val usersRef = database.getReference("users")

    private val userPhno = SessionSave.getSession(Constants.MOBILENO, getApplication()) ?: ""
    private val userName = SessionSave.getSession(Constants.NAME, getApplication()) ?: ""

    init {
        fetchAvailableUsers()
    }

    private fun fetchAvailableUsers() {
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<String>()
                for (child in snapshot.children) {
                    child.key?.let { users.add(it) }
                }
                _availableUsers.value = users
                Notify.mobileArr.clear()
                Notify.mobileArr.addAll(users)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    fun addContact(name: String, phoneno: String) {
        val time = Date().time.toString()
        val contactData = ContactsData(name, phoneno, "0", userName, userPhno)
        
        // Add to both users' lists
        friendListRef.child(userPhno).child(time).setValue(contactData)
        friendListRef.child(phoneno).child(time).setValue(contactData)
    }
}
