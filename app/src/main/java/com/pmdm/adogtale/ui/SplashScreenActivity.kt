package com.pmdm.adogtale.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.pmdm.adogtale.R
import com.pmdm.adogtale.utils.FirebaseUtil
import com.squareup.picasso.Picasso
import org.json.JSONObject

class SplashScreenActivity : AppCompatActivity() {
    private lateinit var btnChatNow: Button
    private lateinit var btnChatLater: Button
    private lateinit var countdownTextView: TextView
    private lateinit var iv1: ImageView
    private lateinit var iv2: ImageView
    private lateinit var tv1: TextView
    private lateinit var tv2: TextView
    private val firebaseUtil: FirebaseUtil = FirebaseUtil()

    // Splash screen duration
    private val SPLASH_DISPLAY_LENGTH: Long = 1000000

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        btnChatNow = findViewById(R.id.btnChatNow)
        btnChatLater = findViewById(R.id.btnChatLater)
        countdownTextView = findViewById(R.id.countdownTextView)
        iv1 = findViewById(R.id.iv1)
        iv2 = findViewById(R.id.iv2)
        tv1 = findViewById(R.id.tv1)
        tv2 = findViewById(R.id.tv2)

        // To load images in imageview
        val im1 = intent.getStringExtra("pic_original")
        val im2 = intent.getStringExtra("pic_target")

        // Original and target images are loaded
        if (!im1.isNullOrEmpty()) {
            Picasso.get()
                .load(im1)
                .fit()
                .centerCrop()
                .into(iv1)
        }

        if (!im2.isNullOrEmpty()) {
            Picasso.get()
                .load(im2)
                .fit()
                .centerCrop()
                .into(iv2)
        }

        //Original and target names are displayed
        val profile1 = intent.getStringExtra("profile_original")
        val profile2 = intent.getStringExtra("profile_target")
        tv1.setText(profile1)
        tv2.setText(profile2)

        val targetEmail = intent.getStringExtra("targetEmail") as String
        Log.i("splash", "me he ejecutado")

        btnChatNow.setOnClickListener {

            firebaseUtil.getCurrentUser { currentUser ->
                firebaseUtil.putAllMatchesToChecked(currentUser.email)
            }

            finish()
            val intent = Intent(this@SplashScreenActivity, ChatActivity::class.java)

            firebaseUtil.getOtherUser(targetEmail) { user ->
                val replyObj = JSONObject()
                replyObj.put("userId", user.userId)
                replyObj.put("username", user.username)
                replyObj.put("email", user.email)
                Log.i("SplashScreenActivity", "intent reply: " + replyObj.toString())
                intent.putExtra("reply", replyObj.toString())
                startActivity(intent)
            }

        }

        btnChatLater.setOnClickListener {
            val mainIntent = Intent(this@SplashScreenActivity, CardSwipeActivity::class.java)
            startActivity(mainIntent)
            finish()
        }

        Handler().postDelayed({
            val mainIntent = Intent(this@SplashScreenActivity, CardSwipeActivity::class.java)
            startActivity(mainIntent)
            finish()
        }, SPLASH_DISPLAY_LENGTH)
    }
}
