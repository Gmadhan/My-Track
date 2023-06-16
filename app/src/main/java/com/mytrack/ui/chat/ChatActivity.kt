package com.mytrack.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.*
import android.widget.AdapterView.OnItemLongClickListener
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import com.android.volley.AuthFailureError
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseListAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import com.mytrack.MySingleton
import com.mytrack.R
import com.mytrack.databinding.FragmentChatBinding
import com.mytrack.model.MessageResponse
import com.mytrack.model.UserDetail
import com.mytrack.utils.Constants
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils.dismissLoader
import com.mytrack.utils.Utils.logger
import com.mytrack.utils.Utils.showloader
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ChatActivity: AppCompatActivity(), View.OnClickListener {

    private lateinit var fragmentChatBinding: FragmentChatBinding
    private var mFirebaseDatabase: DatabaseReference? = null
    private var adapter: FirebaseListAdapter<MessageResponse>? = null
    private var myClip: ClipData? = null
    private var clipboard: ClipboardManager? = null
    private var userId: String? = null
    private var username: String? = null
    private var user_phno: String? = null
    private var mobileno: String? = null
    private var createrName: String? = null
    private var createrNo: String? = null
    private var receiverToken: String? = null
    private var receiverNo: String? = null
    private var receiverName: String? = null
    private val TOPIC = "/topics/"
    private val TAG = "ChatMessage"
    private val FCM_API = "https://fcm.googleapis.com/fcm/send"
    private val serverKey =
        "key=" + "AAAAAXS8IK8:APA91bGX9tof0tOujyLIIzZKT6sHDI13R0NoAygiIZwOrORyqyrSVJjww2F9Jgp0SOfpBtUs8zo7JxNqbtd-_kZFbz5LatDlTwsH_iGuhkWPoP7TGN1G6kyOCp91gvk7WaWYwt6T5Ej1"
    private val contentType = "application/json"
    private val REQUEST_IMAGE = 110

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentChatBinding = FragmentChatBinding.inflate(layoutInflater)
        setContentView(fragmentChatBinding.root)
        init()
    }

    fun init() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        fragmentChatBinding.send.setOnClickListener(this)
        fragmentChatBinding.sendImage.setOnClickListener(this)
        fragmentChatBinding.btnBack.setOnClickListener(this)
        fragmentChatBinding.message.setText("")
        mFirebaseDatabase = Firebase.database.reference.child("chats")
        mobileno = SessionSave.getSession(Constants.MOBILENO,this)
        clipboard =this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        if (intent != null) {
            if (intent.getStringExtra("userId") != null) userId =
                intent.getStringExtra("userId")
            if (intent.getStringExtra("userName") != null) username =
                intent.getStringExtra("userName")
            if (intent.getStringExtra("userMobile") != null) user_phno =
                intent.getStringExtra("userMobile")
            if (intent.getStringExtra("CreaterName") != null) createrName =
                intent.getStringExtra("CreaterName")
            if (intent.getStringExtra("CreaterNo") != null) createrNo =
                intent.getStringExtra("CreaterNo")
            logger(TAG,"userid : $userId : $username : $user_phno : $createrName : $createrNo")
            if (userId != null) chatlist()
        }
        getToken()
        /*TOPIC = TOPIC+createrNo;
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)*/
        if (mobileno.equals(user_phno, ignoreCase = true)) {
            fragmentChatBinding.txtHeaderName.text = createrName
            fragmentChatBinding.txtHeaderMobileno.text = createrNo
            receiverName = username
            receiverNo = createrNo
        } else {
            fragmentChatBinding.txtHeaderName.text = username
            fragmentChatBinding.txtHeaderMobileno.text = user_phno
            receiverName = createrName
            receiverNo = user_phno
        }
        fragmentChatBinding.message.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
        })
    }

    private fun getToken() {
        val firebaseDatabase = Firebase.database.reference.child("users")
        firebaseDatabase.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                for (chidSnap in dataSnapshot.children) {
                    val all = chidSnap.key
                    if (all != null && all.equals(receiverNo, ignoreCase = true)) {
                        try {
                            val userDetail: UserDetail? = chidSnap.getValue(UserDetail::class.java)
                            receiverToken = userDetail!!.token
                            logger(TAG,"token--$receiverToken no $receiverNo", false)
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                logger(TAG, "Failed to read user " + error.toException(), true)
            }
        })
    }

    /**
     * Storage chats in the firebase */
    private fun createchat(messageText: String, username: String?, from: String) {
        try {
            mFirebaseDatabase!!.child(username!!).child(Date().time.toString()).setValue(MessageResponse(messageText, username,from, ""))
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
        mFirebaseDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val user: UserDetail? = dataSnapshot.getValue(UserDetail::class.java)
                logger(TAG,"userdetails--$user")
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                logger(TAG, "Failed to read user "+ error.toException(), true)
            }
        })
    }

    /**
     * get conversion between two users */
    fun chatlist() {
        showloader(this)
        adapter = object : FirebaseListAdapter<MessageResponse>(this, MessageResponse::class.java,
            R.layout.item_message, mFirebaseDatabase!!.child(userId!!)) {
            override fun populateView(v: View, messageData: MessageResponse, position: Int) {
                dismissLoader()
                val messageText: TextView = v.findViewById(R.id.message)
                val messageUser: TextView = v.findViewById(R.id.sender)
                val messageTime: TextView = v.findViewById(R.id.time)
                val imageView: AppCompatImageView = v.findViewById(R.id.image)
                val relativeLayout = v.findViewById<RelativeLayout>(R.id.relative_layout)
                val chatLay = v.findViewById<LinearLayout>(R.id.chat_lay)

                if (messageData.messageText != null && !messageData.messageText.equals("")) {
                    messageText.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                    messageText.text = messageData.messageText
                    messageText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)
                    messageText.setTextColor(resources.getColor(R.color.Black))
                } else if (messageData.imageUrl != null && !messageData.imageUrl.equals("")) {
                    val imageUrl: String = messageData.imageUrl!!
                    imageView.visibility = View.VISIBLE
                    messageText.visibility = View.GONE
                    logger(TAG,"image url $imageUrl")
                    //                    if (imageUrl.startsWith("gs://")) {
                    val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
                    storageReference.downloadUrl.addOnCompleteListener { task: Task<Uri> ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result.toString()
                            Glide.with(this@ChatActivity).load(downloadUrl)
                                .placeholder(R.drawable.ic_loading).error(R.drawable.ic_error)
                                .into(imageView)
                            imageView.setOnClickListener {
                                val intent = Intent(this@ChatActivity, ImageViewActivity::class.java)
                                intent.putExtra("image", downloadUrl)
                                startActivity(intent)
                            }
                        } else {
                            logger(TAG, "Getting download url was not successful." + task.exception, true)
                        }
                    }
                    //                    }
                } else {
                    messageText.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                    messageText.text = getString(R.string.this_message_removed)
                    messageText.setCompoundDrawablesWithIntrinsicBounds(
                        resources.getDrawable(R.drawable.ic_message_deleted,null),
                        null,
                        null,
                        null
                    )
                    messageText.setTextColor(resources.getColor(R.color.HintColor))
                }
                if (messageData.from.equals(mobileno)) {
                    messageText.background = resources.getDrawable(R.drawable.bg_chat_right,null)
                    imageView.background = resources.getDrawable(R.drawable.bg_chat_right,null)
                    messageText.gravity = Gravity.RIGHT
                    chatLay.gravity = Gravity.RIGHT
                    relativeLayout.setPadding(120, 10, 10, 10)
                } else {
                    relativeLayout.setPadding(10, 10, 120, 10)
                    messageText.gravity = Gravity.LEFT
                    chatLay.gravity = Gravity.LEFT
                    messageText.background = resources.getDrawable(R.drawable.bg_chat_left,null)
                    imageView.background = resources.getDrawable(R.drawable.bg_chat_left,null)
                }
                logger(TAG,"messages : " + messageData.messageText.toString() + " -- " + messageData.messageUser)
                // Format the date before showing it
                messageTime.text = DateFormat.format("dd-MMM HH:mm", messageData.messageTime)
            }
        }
        fragmentChatBinding.chatList.adapter = adapter
        fragmentChatBinding.chatList.onItemLongClickListener =
            OnItemLongClickListener { arg0: AdapterView<*>?, arg1: View, pos: Int, id: Long ->
                val textView = arg1.findViewById<TextView>(R.id.message)
                val text = textView.text.toString()
                myClip = ClipData.newPlainText("text", text)
                clipboard!!.setPrimaryClip(myClip!!)
                Toast.makeText(this, "Copied to the clipbroad", Toast.LENGTH_SHORT)
                    .show()
                true
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.send -> {
                val messages: String = fragmentChatBinding.message.text.toString().trim()
                if (!messages.equals("", ignoreCase = true)) {
                    logger(TAG,"mess $messages...")
                    createchat(
                        fragmentChatBinding.message.text.toString(),
                        userId,
                        SessionSave.getSession(Constants.MOBILENO,this).toString()
                    )
                    fragmentChatBinding.message.setText("")
                    val notification = JSONObject()
                    val notifcationBody = JSONObject()
                    try {
                        notifcationBody.put("title", receiverName)
                        notifcationBody.put("message", messages)
                        notification.put("to", "$receiverToken")
                        notification.put("data", notifcationBody)
                    } catch (e: JSONException) {
                        logger(TAG, "onCreate: " + e.message, true)
                    }
                    sendNotification(notification)
                }
            }
            R.id.send_image -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_IMAGE)
            }

            R.id.btn_back -> {
                finish()
            }
        }
    }

    private fun sendNotification(notification: JSONObject) {
        val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
            Method.POST,FCM_API, notification,
            Response.Listener { response: JSONObject ->
                logger(TAG, "onResponse: $response")
            },
            Response.ErrorListener { error: VolleyError? ->
                Toast.makeText(this, "Request error", Toast.LENGTH_LONG).show()
                var gson = Gson()
                logger(TAG, "onErrorResponse: Didn't work " + gson.toJson(error))
            }) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val params: MutableMap<String, String> = HashMap()
                params["Authorization"] = serverKey
                params["Content-Type"] = contentType
                return params
            }
        }
        MySingleton.getInstance(this)!!.addToRequestQueue(jsonObjectRequest)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        logger(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    val uri = data.data
                    logger(TAG, "Uri: " + uri.toString())
                    val time = Date().time.toString()
                    mFirebaseDatabase!!.child(userId!!).child(time).setValue(
                        MessageResponse("",
                            username,
                            SessionSave.getSession(Constants.MOBILENO, this),
                            "")
                    ) { databaseError: DatabaseError?, databaseReference: DatabaseReference ->
                        if (databaseError == null) {
                            val key = databaseReference.key
                            val storageReference = FirebaseStorage.getInstance()
                                .getReference("chats")
                                .child("images")
                                .child(uri!!.lastPathSegment!!)
                            putImageInStorage(storageReference, uri, key, time)
                        } else {
                            logger(TAG, "Unable to write message to database." + databaseError.toException())
                        }
                    }
                }
            }
        }
    }

    private fun putImageInStorage(storageReference: StorageReference, uri: Uri?, key: String?, time: String) {
        storageReference.putFile(uri!!).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                task.result!!.metadata!!.reference!!.downloadUrl
                    .addOnCompleteListener(this) { task1 ->
                        if (task1.isSuccessful) {
                            mFirebaseDatabase!!.child(userId!!).child(time).setValue(MessageResponse("", username, SessionSave.getSession(Constants.MOBILENO, this), task1.result.toString()))
                        }
                    }
            } else {
                logger(TAG, "Image upload task was not successful." + task.exception)
            }
        }
    }

}