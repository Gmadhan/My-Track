package com.mytrack.utils

import com.mytrack.R
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.mytrack.databinding.CommonBottomSheetBinding

class CommonBottomSheet(var activity: Context, var type:String) : BottomSheetDialogFragment() {
    private lateinit var commonBottomSheetBinding: CommonBottomSheetBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setStyle(STYLE_NORMAL, R.style.MyTransparentBottomSheetDialogTheme);
        commonBottomSheetBinding = CommonBottomSheetBinding.inflate(layoutInflater, container, false)
        init()
        return commonBottomSheetBinding.root
    }

    fun init() {

        commonBottomSheetBinding.txtTitle.text = type
        if (type == getString(R.string.language)) {
            commonBottomSheetBinding.llLanguage.visibility = View.VISIBLE
            commonBottomSheetBinding.llPasswordChange.visibility = View.GONE
        } else {
            commonBottomSheetBinding.llLanguage.visibility = View.GONE
            commonBottomSheetBinding.llPasswordChange.visibility = View.VISIBLE
        }

        commonBottomSheetBinding.rbEnglish.setOnClickListener {
            dismiss()
        }
        commonBottomSheetBinding.rbTamil.setOnClickListener {
            dismiss()
        }
        commonBottomSheetBinding.btnConfirm.setOnClickListener {
            dismiss()
        }
        commonBottomSheetBinding.btnClose.setOnClickListener {
            dismiss()
        }

    }

}