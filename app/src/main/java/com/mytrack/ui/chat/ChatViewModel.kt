package com.mytrack.ui.chat

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.mytrack.MySingleton
import com.mytrack.model.MessageResponse
import com.mytrack.model.UserDetail
import com.mytrack.utils.Constants
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils.logger
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "ChatViewModel"
    private val database = Firebase.database.reference
    private val chatsRef = database.child("chats")
    private val usersRef = database.child("users")
    
    private val _receiverToken = MutableLiveData<String?>()
    val receiverToken: LiveData<String?> = _receiverToken

    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey = "key=AAAAAXS8IK8:APA91bGX9tof0tOujyLIIzZKT6sHDI13R0NoAygiIZwOrORyqyrSVJjww2F9Jgp0SOfpBtUs8zo7JxNqbtd-_kZFbz5LatDlTwsH_iGuhkWPoP7TGN1G6kyOCp91gvk7WaWYwt6T5Ej1"
    private val contentType = "application/json"

    fun fetchReceiverToken(receiverNo: String) {
        usersRef.child(receiverNo).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val user = snapshot.getValue(UserDetail::class.java)
                _receiverToken.value = user?.token
            }

            override fun onCancelled(error: DatabaseError) {
                logger(TAG, "Failed to read user token: ${error.message}")
            }
        })
    }

    fun sendMessage(userId: String, messageText: String, from: String) {
        val time = Date().time.toString()
        chatsRef.child(userId).child(time).setValue(MessageResponse(messageText, userId, from, ""))
    }

    fun sendImage(userId: String, username: String?, from: String, uri: Uri) {
        val time = Date().time.toString()
        val storageReference = FirebaseStorage.getInstance().getReference("chats/images/${uri.lastPathSegment}")
        
        storageReference.putFile(uri).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                storageReference.downloadUrl.addOnCompleteListener { downloadTask ->
                    if (downloadTask.isSuccessful) {
                        chatsRef.child(userId).child(time).setValue(
                            MessageResponse("", username, from, downloadTask.result.toString())
                        )
                    }
                }
            }
        }
    }

    fun sendNotification(notification: JSONObject) {
        val jsonObjectRequest = object : JsonObjectRequest(
            Method.POST, FCM_API, notification,
            Response.Listener { response -> logger(TAG, "Notification sent: $response") },
            Response.ErrorListener { error -> logger(TAG, "Notification error: ${error.message}") }
        ) {
            override fun getHeaders(): Map<String, String> {
                return mapOf(
                    "Authorization" to serverKey,
                    "Content-Type" to contentType
                )
            }
        }
        MySingleton.getInstance(getApplication())?.addToRequestQueue(jsonObjectRequest)
    }
}
