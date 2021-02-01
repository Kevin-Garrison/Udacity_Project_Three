package com.udacity

import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.udacity.notifications.cancelNotifications
import kotlinx.android.synthetic.main.activity_detail.*
import kotlinx.android.synthetic.main.content_detail.*

class DetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)

        button_dismiss.setOnClickListener {
            goBack()
        }

        filename.text = intent.getStringExtra("fileName").toString()
        status.text = intent.getStringExtra("status").toString()

        if(status.text == getString(R.string.status_passed)) {
            status.setTextColor(Color.GREEN)
        } else {
            status.setTextColor(Color.RED)
        }

        val notificationManager =
            ContextCompat.getSystemService(
                this,
                NotificationManager::class.java
            ) as NotificationManager
        notificationManager.cancelNotifications()

    }

    private fun goBack() {
        val  mainActivity = Intent(this, MainActivity::class.java)
        startActivity(mainActivity)
    }
}
