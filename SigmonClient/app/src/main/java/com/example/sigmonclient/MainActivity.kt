package com.example.sigmonclient
import android.os.Bundle
import androidx.activity.ComponentActivity
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val PERMISSIONS_REQUEST_CODE = 123
    private var monitoring_running = 0
    private val START_BUTTON_TEXT = "Start"
    private val STOP_BUTTON_TEXT = "Stop"
    private val SERVICE_START_MESSAGE = "Service Started"
    private val SERVICE_STOP_MESSAGE = "Service Stopped"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!hasPermissions()){
            requestPermissions()
        }

        val button = findViewById<Button>(R.id.registerButton)
        button.setOnClickListener(){
            if (this.monitoring_running == 1){
                stopSignalMonitoringService()
                monitoring_running = 0
                button.text = this.START_BUTTON_TEXT
                Toast.makeText(this,this.SERVICE_STOP_MESSAGE, Toast.LENGTH_SHORT).show()
            }
            else{
                startSignalMonitoringService()
                monitoring_running = 1
                button.text = this.STOP_BUTTON_TEXT
                Toast.makeText(this,this.SERVICE_START_MESSAGE, Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.POST_NOTIFICATIONS
        ), PERMISSIONS_REQUEST_CODE)
    }

    private fun startSignalMonitoringService() {
        val serviceIntent = Intent(this, SignalMonitoringService::class.java).apply {
            putExtra("AUTH_KEY", findViewById<EditText>(R.id.auth_key).text.toString())
        }
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    private fun stopSignalMonitoringService(){
        val stopServiceIntent = Intent(this, SignalMonitoringService::class.java)
        stopService(stopServiceIntent)
    }
}


