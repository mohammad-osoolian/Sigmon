package com.example.sigmonserver

import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import com.example.sigmonserver.ui.theme.SigmonServerTheme

class MainActivity : ComponentActivity() {
    private val SMS_PERMISSIONS_REQUEST_CODE = 1
    private lateinit var receiver: SmsReceiver
    private lateinit var logsTextView: TextView
    private lateinit var clearbutton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        logsTextView = findViewById(R.id.logTextView)
        clearbutton = findViewById(R.id.clearLogs)
        clearbutton.setOnClickListener(){
            logsTextView.text = "--------------------- logs ----------------------"
        }
        receiver = SmsReceiver(Handler(), logsTextView)
        registerReceiver(receiver, IntentFilter("android.provider.Telephony.SMS_RECEIVED"))



        ActivityCompat.requestPermissions(this, arrayOf(
            android.Manifest.permission.RECEIVE_SMS,
            android.Manifest.permission.SEND_SMS,
            android.Manifest.permission.READ_SMS
        ), SMS_PERMISSIONS_REQUEST_CODE)
    }
}
