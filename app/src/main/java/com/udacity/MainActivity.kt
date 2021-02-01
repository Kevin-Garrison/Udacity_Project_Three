package com.udacity

import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.udacity.loadingbutton.ButtonState
import com.udacity.loadingbutton.LoadingButton
import com.udacity.network.NetStatus
import com.udacity.network.Status
import com.udacity.notifications.sendNotification
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.io.File
import java.util.concurrent.Executors.newSingleThreadExecutor

class MainActivity : AppCompatActivity() {

    private var downloadID: Long = 0

    private var filesDownloaded = 0
    private var fileUrl: String? = null
    private var fileName: String? = null
    private var fileSize: Long = 0L

    lateinit var netStatus: NetStatus

    lateinit var radioGroup: RadioGroup
    lateinit var selectedRadio: View

    lateinit var editCustomUrl: EditText

    lateinit var customButton: LoadingButton

    private lateinit var notificationManager: NotificationManager

    private var downloadComplete: Boolean = true

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        } // Do nothing

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if(Patterns.WEB_URL.matcher(s).matches()) {
                fileUrl = s.toString()
                customButton.setState(ButtonState.Download)
            } else {
                customButton.setState(ButtonState.Idle)
            }
        }

        override fun afterTextChanged(s: Editable?) {
        } // Do nothing
    }

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            val action = intent?.action

            if (downloadID == id) {

                if (action.equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                    downloadComplete = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        registerReceiver(receiver, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        radioGroup = findViewById(R.id.radio_group)

        editCustomUrl = findViewById(R.id.editCustomUrl)
        editCustomUrl.addTextChangedListener(textWatcher)

        customButton = findViewById(R.id.custom_button)
        customButton.setState(ButtonState.Test)

        customButton.setOnClickListener {
            download()
        }

        netStatus = NetStatus(this.application)
        netStatus.startNetworkCallback()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        cloud_image.isVisible = (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT)
    }

    fun onRadioButtonClicked(view: View) {
        selectedRadio = view as RadioButton

        var url: String = ""

        var valid = false

        if (radio_project == selectedRadio) {
            url = getString(R.string.udacity_project_url)
            fileName = getString(R.string.udacity_download_description)
            fileSize = 149613
            valid = true
        }
        if (radio_glide == selectedRadio) {
            url = getString(R.string.glide_project_url)
            fileName = getString(R.string.glide_download_description)
            fileSize = 33412626
            valid = true
        }
        if (radio_retrofit == selectedRadio) {
            url = getString(R.string.retrofit_project_url)
            fileName = getString(R.string.retrofit_download_description)
            fileSize = 626414
            valid = true
        }

        if(valid) editCustomUrl.setText(url)
    }

    private fun download() {

        if (!Status.isNetworkConnected) {
            showToast("Please Connect To A Network")
            return
        }

        if (fileUrl != null) {

            if(!fileUrl!!.startsWith("https://")){
                fileUrl = "https://"+ fileUrl
            }

            customButton.setState(ButtonState.Loading)
            downloadComplete = false

            createChannel()

            downloadFile()
        } else {
            customButton.setState(ButtonState.Test)
            showToast(getString(R.string.please_select_file))
        }
    }

    private fun createChannel() {

        notificationManager = ContextCompat.getSystemService(
            applicationContext,
            NotificationManager::class.java
        ) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                getString(R.string.download_notification_channel_id),
                getString(R.string.download_notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply { setShowBadge(false) }

            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.enableVibration(true)
            notificationChannel.description = "$fileName has finished downloading"

            val notificationManager = getSystemService(NotificationManager::class.java)

            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun downloadFile() {
        val dir = File(getExternalFilesDir(null), "/ND940C3")

        if (!dir.exists()) {
            dir.mkdirs()
        }

        val downloadManager = getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val request =
            DownloadManager.Request(Uri.parse(fileUrl))
                .setTitle(getString(R.string.app_name))
                .setDescription(getString(R.string.app_description))
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                .setRequiresCharging(false)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)
                .setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/ND940C3/$fileName"
                )
        downloadID = downloadManager.enqueue(request)

        val total = fileSize

        val mRunnable = Runnable() {
            while (!downloadComplete) {

                val cursor =
                    downloadManager.query(DownloadManager.Query().setFilterById(downloadID))
                if (cursor.moveToFirst()) {
                    when (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
                        DownloadManager.STATUS_FAILED -> {
                            customButton.setCurrentPosition(1F)
                            downloadComplete = true
                            customButton.setState(ButtonState.Completed)
                            notificationManager.sendNotification(
                                fileName.toString(),
                                applicationContext,
                                getString(R.string.status_failed)
                            )
                        }
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            customButton.setCurrentPosition(1F)
                            downloadComplete = true
                             notificationManager.sendNotification(
                                fileName.toString(),
                                applicationContext,
                                getString(R.string.status_passed)
                            )
                            filesDownloaded++
                        }
                        DownloadManager.STATUS_RUNNING -> {
                            if(total >= 3000000) {
                                val downloaded: Float = cursor.getFloat(
                                    cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                                )
                                val progress = downloaded / total
                                customButton.setCurrentPosition(progress)
                            }
                        }
                    }
                }
            }
        }
        val mExecutor = newSingleThreadExecutor()
        mExecutor.execute(mRunnable)
    }

    private fun showToast(text: String) {
        val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
        toast.show()
    }
}
