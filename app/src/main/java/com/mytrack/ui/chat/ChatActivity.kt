package com.mytrack.ui.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatImageView
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.firebase.ui.database.FirebaseListAdapter
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.mytrack.R
import com.mytrack.databinding.FragmentChatBinding
import com.mytrack.model.MessageResponse
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
    private lateinit var viewModel: ChatViewModel
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
    private val TAG = "ChatMessage"
    private val REQUEST_IMAGE = 110

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentChatBinding = FragmentChatBinding.inflate(layoutInflater)
        setContentView(fragmentChatBinding.root)
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        init()
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.receiverToken.observe(this) { token ->
            receiverToken = token
        }
    }

    fun init() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        fragmentChatBinding.send.setOnClickListener(this)
        fragmentChatBinding.sendImage.setOnClickListener(this)
        fragmentChatBinding.btnBack.setOnClickListener(this)
        fragmentChatBinding.message.setText("")
        mobileno = SessionSave.getSession(Constants.MOBILENO,this)
        clipboard =this.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
        
        if (intent != null) {
            userId = intent.getStringExtra("userId")
            username = intent.getStringExtra("userName")
            user_phno = intent.getStringExtra("userMobile")
            createrName = intent.getStringExtra("CreaterName")
            createrNo = intent.getStringExtra("CreaterNo")
            if (userId != null) chatlist()
        }
        
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

        receiverNo?.let { viewModel.fetchReceiverToken(it) }
    }

    fun chatlist() {
        showloader(this)
        val mFirebaseDatabase = FirebaseDatabase.getInstance().reference.child("chats")
        adapter = object : FirebaseListAdapter<MessageResponse>(this, MessageResponse::class.java,
            R.layout.item_message, mFirebaseDatabase.child(userId!!)) {
            override fun populateView(v: View, messageData: MessageResponse, position: Int) {
                dismissLoader()
                val messageText: TextView = v.findViewById(R.id.message)
                val messageTime: TextView = v.findViewById(R.id.time)
                val imageView: AppCompatImageView = v.findViewById(R.id.image)
                val relativeLayout = v.findViewById<RelativeLayout>(R.id.relative_layout)
                val chatLay = v.findViewById<LinearLayout>(R.id.chat_lay)

                if (!messageData.messageText.isNullOrEmpty()) {
                    messageText.visibility = View.VISIBLE
                    imageView.visibility = View.GONE
                    messageText.text = messageData.messageText
                    messageText.setTextColor(resources.getColor(R.color.Black))
                } else if (!messageData.imageUrl.isNullOrEmpty()) {
                    val imageUrl: String = messageData.imageUrl!!
                    imageView.visibility = View.VISIBLE
                    messageText.visibility = View.GONE
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
                        }
                    }
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
                messageTime.text = DateFormat.format("dd-MMM HH:mm", messageData.messageTime)
            }
        }
        fragmentChatBinding.chatList.adapter = adapter
        fragmentChatBinding.chatList.onItemLongClickListener =
            AdapterView.OnItemLongClickListener { _, arg1, _, _ ->
                val textView = arg1.findViewById<TextView>(R.id.message)
                val text = textView.text.toString()
                myClip = ClipData.newPlainText("text", text)
                clipboard!!.setPrimaryClip(myClip!!)
                Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                true
            }
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.send -> {
                val messages: String = fragmentChatBinding.message.text.toString().trim()
                if (messages.isNotEmpty()) {
                    viewModel.sendMessage(userId!!, messages, mobileno!!)
                    fragmentChatBinding.message.setText("")
                    
                    val notification = JSONObject()
                    val notifcationBody = JSONObject()
                    try {
                        notifcationBody.put("title", receiverName)
                        notifcationBody.put("message", messages)
                        notification.put("to", "$receiverToken")
                        notification.put("data", notifcationBody)
                        viewModel.sendNotification(notification)
                    } catch (e: JSONException) {
                        logger(TAG, "sendNotification error: " + e.message, true)
                    }
                }
            }
            R.id.send_image -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.type = "image/*"
                startActivityForResult(intent, REQUEST_IMAGE)
            }
            R.id.btn_back -> finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE && resultCode == RESULT_OK && data?.data != null) {
            viewModel.sendImage(userId!!, username, mobileno!!, data.data!!)
        }
    }
}
