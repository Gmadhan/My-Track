package com.mytrack.ui.chat

import com.mytrack.R
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.size
import androidx.fragment.app.Fragment
import com.firebase.ui.database.FirebaseListAdapter
import com.google.firebase.database.*
import com.mytrack.databinding.FragmentContactBinding
import com.mytrack.model.ContactsData
import com.mytrack.model.UserDetail
import com.mytrack.utils.Constants
import com.mytrack.utils.Notify
import com.mytrack.utils.SessionSave
import com.mytrack.utils.Utils
import com.mytrack.utils.Utils.logger
import java.util.*

class ChatListFragment: Fragment(), View.OnClickListener {

    private lateinit var fragmentContactBinding: FragmentContactBinding
    private var mFirebaseDatabase: DatabaseReference? = null
    private var contactDialog: Dialog? = null
    private var user_name: String? = null
    private var user_phno: String? = null
    private var selected_Id: String? = null
    private val TAG = "ChatListFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentContactBinding = FragmentContactBinding.inflate(layoutInflater, container, false)
        init()
        return fragmentContactBinding.root
    }

    private fun init() {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        val mFirebaseInstance = FirebaseDatabase.getInstance()
        mFirebaseDatabase = mFirebaseInstance.getReference("friendlist")
        user_phno = SessionSave.getSession(Constants.MOBILENO, requireActivity())
        user_name = SessionSave.getSession(Constants.NAME, requireActivity())

        fragmentContactBinding.inHeader.txtHeaderName.text = getString(R.string.contacts)
        fragmentContactBinding.btnAddContact.setOnClickListener(this)
        fragmentContactBinding.inHeader.btnBack.visibility = View.GONE

        try {
            Notify.mobileArr.clear()
            val databaseReference = mFirebaseInstance.getReference("users")
            databaseReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                    try {
                        for (chidSnap in dataSnapshot.children) {
                            val all = chidSnap.key
                            Notify.mobileArr.add(all!!)
                            logger(TAG,"contactlist " + Notify.mobileArr.toString())
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                override fun onCancelled(@NonNull databaseError: DatabaseError) {}
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
        contactlistAdapter()
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_add_contact -> addcontactAlert(requireActivity())
        }
    }

    fun addcontactAlert(mcontext: Context?) {
        try {
            if (contactDialog != null && contactDialog!!.isShowing) contactDialog!!.dismiss()
            val view = View.inflate(mcontext, R.layout.add_contact_alert, null)
            contactDialog = Dialog(mcontext!!, R.style.DialogFragmentTheme)
            contactDialog!!.setContentView(view)
            contactDialog!!.setCancelable(true)
            val window = contactDialog!!.window
            val wlp = window!!.attributes
            wlp.gravity = Gravity.CENTER
            wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_BLUR_BEHIND.inv()
            window.attributes = wlp
            contactDialog!!.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)
            contactDialog!!.show()
            val name: AppCompatEditText = contactDialog!!.findViewById(R.id.contact_name)
            val mobile: AppCompatEditText = contactDialog!!.findViewById(R.id.mobile_no)
            val cancel: TextView = contactDialog!!.findViewById(R.id.cancel)
            val save: TextView = contactDialog!!.findViewById(R.id.save)

            mobile.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable) {}
            })

            cancel.setOnClickListener { v -> contactDialog!!.dismiss() }

            save.setOnClickListener { v ->
                if (name.text.toString() == "")
                    name.error = getString(R.string.username_null)
                else if (mobile.text.toString() == "")
                    mobile.error = getString(R.string.phone_null)
                else if (name.text.toString().length < 4)
                    name.error = getString(R.string.check_name)
                else if (mobile.text.toString().length < 10 || mobile.text.toString().length > 15)
                    mobile.error = getString(R.string.check_mobileno)
                else if (!Notify.mobileArr.contains(mobile.text.toString()))
                    mobile.error = getString(R.string.mobile_no_not_exit) else {
                    createContact(
                        name.text.toString(),
                        mobile.text.toString(),
                        user_name,
                        user_phno
                    )
                    contactDialog!!.dismiss()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createContact(
        name: String,
        phoneno: String,
        createrName: String?,
        createrNo: String?
    ) {
        try {
            val random = Random()
            val n = random.nextInt(50)
            val id = n.toString()
            val time = Date().time.toString()
            val contactData = ContactsData( name, phoneno, id, createrName, createrNo)
            mFirebaseDatabase!!.child(user_phno!!).child(time).setValue(contactData)
            mFirebaseDatabase!!.child(phoneno).child(time).setValue(contactData)
            addUserChangeListener()
            //            listView.notifyAll();
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * User data change listener
     */
    private fun addUserChangeListener() {
        mFirebaseDatabase!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                val user: UserDetail? = dataSnapshot.getValue(UserDetail::class.java)
                logger(TAG,"userdetails--$user")
                contactDialog!!.dismiss()
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                logger(TAG, "Failed to read user "+error.toException(), true)
            }
        })
    }

    private fun contactlistAdapter() {
        Utils.showloader(requireActivity())
        val adapter: FirebaseListAdapter<ContactsData> = object : FirebaseListAdapter<ContactsData>(requireActivity(), ContactsData::class.java,
            R.layout.item_contact, mFirebaseDatabase!!.child(user_phno!!).orderByChild("name")) {
            @SuppressLint("SetTextI18n")
            override fun populateView(v: View, model: ContactsData, position: Int) {
                // Get references to the views of message.xml
                val username: TextView = v.findViewById(R.id.txt_name)
                val contact_logo: TextView = v.findViewById(R.id.logo)
                Utils.dismissLoader()
                if (user_phno.equals(model.mobileno.toString(), ignoreCase = true)) {
                    if (model.createrName != null && !model.createrName.equals("")) {
                        contact_logo.text = model.createrName!![0].toString().uppercase(Locale.getDefault())
                        username.text = Utils.capitalizeWords(model.createrName!!)
                    }
                } else {
                    if (model.name != null && !model.name.equals("")) {
                        contact_logo.text = model.name!![0].toString().uppercase(Locale.getDefault())
                        username.text = Utils.capitalizeWords(model.name!!)
                    }
                }
                username.setOnClickListener {
                    selected_Id = model.Id
                    val intent = Intent(requireActivity(), ChatActivity::class.java)
                    intent.putExtra("userId", selected_Id)
                    intent.putExtra("userMobile", model.mobileno)
                    intent.putExtra("CreaterName", Utils.capitalizeWords(model.createrName!!))
                    intent.putExtra("CreaterNo", model.createrNo)
                    intent.putExtra("userName", Utils.capitalizeWords(model.name!!))
                    startActivity(intent)
                    logger(TAG,"contacts-- " + username.text.toString() + ": " + selected_Id.toString() + ": " + model.mobileno)
                }

                if(model != null) {
                    setNoData(false)
                } else {
                    setNoData(true)
                }
            }

            override fun onCancelled(databaseError: DatabaseError?) {
                super.onCancelled(databaseError)
                Utils.dismissLoader()
                setNoData(true)
            }
        }
        fragmentContactBinding.contactList.adapter = adapter
    }

    fun setNoData(show: Boolean) {
        fragmentContactBinding.contactList.visibility = if(show) View.GONE else View.VISIBLE
        fragmentContactBinding.inNodata.rlNodata.visibility = if(show) View.VISIBLE else View.GONE
    }

}