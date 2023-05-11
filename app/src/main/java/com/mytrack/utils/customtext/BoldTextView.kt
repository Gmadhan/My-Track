package com.mytrack.utils.customtext

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import android.annotation.SuppressLint
import androidx.core.content.res.ResourcesCompat
import com.mytrack.R

@SuppressLint("AppCompatCustomView")
class BoldTextView : TextView {

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?) : super(context) {
        init()
    }

    fun init() {
        val tf = ResourcesCompat.getFont(context, R.font.bold)
        typeface = tf
    }

}