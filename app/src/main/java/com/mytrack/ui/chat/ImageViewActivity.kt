package com.mytrack.ui.chat

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.mytrack.R
import com.mytrack.databinding.ActivityImageBinding
import kotlin.math.max
import kotlin.math.min


class ImageViewActivity: AppCompatActivity() {
    lateinit var activityImageBinding: ActivityImageBinding
    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var imageUrl: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityImageBinding = ActivityImageBinding.inflate(layoutInflater)
        setContentView(activityImageBinding.root)
        init()
    }

    fun init() {
        activityImageBinding.header.btnBack.setOnClickListener { finish() }
        scaleGestureDetector = ScaleGestureDetector(this, ScaleListener(activityImageBinding.ivChatImage,1.0f))

        try {
            if (intent != null) {
                imageUrl = intent.getStringExtra("image").toString()
            }

            Glide.with(this@ImageViewActivity).load(imageUrl)
                .placeholder(R.drawable.ic_loading).error(R.drawable.ic_error)
                .into(activityImageBinding.ivChatImage)
        } catch (e:Exception) {
            e.printStackTrace()
        }
    }

    override fun onTouchEvent(motionEvent: MotionEvent?): Boolean {
        scaleGestureDetector!!.onTouchEvent(motionEvent!!)
        return true
    }

    class ScaleListener(private var image: ImageView, private var mScaleFactor: Float) : SimpleOnScaleGestureListener() {

        override fun onScale(scaleGestureDetector: ScaleGestureDetector): Boolean {
            mScaleFactor = scaleGestureDetector.scaleFactor
            mScaleFactor = max(0.1f, min(mScaleFactor, 10.0f))
            image.scaleX = mScaleFactor
            image.scaleY = mScaleFactor
            return true
        }
    }

}