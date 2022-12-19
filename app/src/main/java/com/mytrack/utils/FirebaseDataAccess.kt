package com.mytrack.utils

import android.content.Context
import androidx.annotation.NonNull
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.mytrack.model.response.UserDetail
import com.mytrack.utils.Utils.toString

object FirebaseDataAccess {
    private var mFirebaseDatabase: DatabaseReference? = null
//    var userDetail: UserDetail? = null
    val TAG = "FirebaseDataAccess"

    fun retriveUserIDData(context: Context, userID: String): UserDetail? {
        mFirebaseDatabase = Firebase.database.reference.child("users").child(userID)
        Firebase.database.reference.child("app").setValue("My Track")
        mFirebaseDatabase!!.child("phoneno").setValue(userID)
        var userDetail: UserDetail? = null
        Firebase.database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    var phoneNo: String = ""
                    Notify.mobileArr.clear()
                    for (chidSnap in dataSnapshot.children) {
                        phoneNo = chidSnap.value.toString()
                        Notify.mobileArr.add(phoneNo)
                        if (phoneNo != null && phoneNo.equals(userID, ignoreCase = true)) {
                            Utils.logger(TAG, "all data : $dataSnapshot")
                            userDetail = dataSnapshot.getValue(UserDetail::class.java)!!
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {
                Utils.logger(TAG, "error == > $databaseError")
            }
        })
        Utils.logger(TAG, "all data : $userDetail")
        return userDetail!!
    }

    fun retriveAllData(context: Context): UserDetail? {
        var userDetail: UserDetail? = null
        Firebase.database.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    for (chidSnap in dataSnapshot.children) {
                        val all = chidSnap.key
                        if (all != null) {
                            userDetail = chidSnap.getValue(UserDetail::class.java)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull databaseError: DatabaseError) {

            }
        })

        return userDetail!!
    }

    fun updateDataBase(context: Context, userID: String, userDetails: UserDetail): UserDetail? {
        var userDetail: UserDetail? = null
        Firebase.database.reference.child(userID).setValue(userDetails)
        Firebase.database.reference.child(userID).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(@NonNull dataSnapshot: DataSnapshot) {
                try {
                    userDetail = dataSnapshot.getValue(UserDetail::class.java)
                    Utils.logger(TAG, "userdetails-- $userDetail")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCancelled(@NonNull error: DatabaseError) {
                Utils.logger(TAG, "Failed to read userDetail " + error.toException(), true)
            }
        })
        return userDetail!!
    }

}