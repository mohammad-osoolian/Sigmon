package com.example.sigmonclient

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.CellSignalStrengthGsm
import android.telephony.CellSignalStrengthWcdma
import android.telephony.CellIdentityLte
import android.telephony.CellInfo
import android.telephony.CellInfoGsm
import android.telephony.CellInfoLte
import android.telephony.CellInfoWcdma
import android.telephony.CellInfoNr
import java.math.BigInteger
import java.security.MessageDigest
import android.telephony.CellSignalStrength
import android.telephony.CellSignalStrengthNr
import android.telephony.CellSignalStrengthCdma
import android.telephony.CellSignalStrengthLte
import android.telephony.CellSignalStrengthTdscdma
import android.telephony.TelephonyManager
import android.telephony.SmsManager
import android.util.Log
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import kotlin.reflect.typeOf

class SignalMonitoringService : Service() {

    private lateinit var telephonyManager: TelephonyManager
    private var GSM_THRESHOLD = -100
    private var WCDMA_THRESHOLD = -100
    private var CDMA_THRESHOLD = -100
    private var TDSCDMA_THRESHOLD = -100
    private var LTE_THRESHOLD = -100
    private var NR_THRESHOLD = -100
    private val NOTIF_ID = 1
    private val CHANNEL_NAME = "Signal Monitoring Service"
    private val CHANNE_ID = "SignalMonitoringServiceChannel"
    private val NOTIF_TITLE = "Signal Monitoring Service"
    private val SERVER_NUMBER = "0123456789"
    private  var AUTH_KEY = ""


    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        telephonyManager = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val notification = createNotification()
        startForeground(this.NOTIF_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        telephonyManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
    }

    override fun onDestroy() {
        telephonyManager.listen(signalStrengthListener, PhoneStateListener.LISTEN_NONE)
        stopForeground(true)
        stopSelf()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Retrieve the argument from the intent
        val auth_key = intent?.getStringExtra("AUTH_KEY") ?: "NO_AUTH"
        this.AUTH_KEY = md5(auth_key)
        return START_STICKY
    }

    private val signalStrengthListener = object : PhoneStateListener() {
        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
            super.onSignalStrengthsChanged(signalStrength)
            val cells = signalStrength.cellSignalStrengths
            if (cells.isNotEmpty()){
                val cellSignal = cells[0]
                val strength = getSignalStrengthForCell(cellSignal)
                val threshold = getThresholdForCell(cellSignal)
                val tech = getTechnologyForCell(cellSignal)
                val cellinfo = extractCellInfo(cellSignal)
                updateNotification(text = "Cell Technology: $tech ---- strengh: $strength")

                if (strength < threshold){
                    sendSmsToServer(message = addAuthKey(cellinfo))
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun extractCellInfo(cellSignalStrength: CellSignalStrength):String{
        val str = cellSignalStrength.toString()
        val parts = str.split(" ")
        val filteredParts = parts.filter { !it.contains("=2147483647") }
        var result = filteredParts.joinToString("\n").replace(":", "").replace("CellSignalStrength", "Cell Technology: ")
        result += "\nSignal Strength: " + getSignalStrengthForCell(cellSignalStrength).toString()
        result += "\nTAC: " + telephonyManager.typeAllocationCode.toString()
        return result
    }

    private fun addAuthKey(str: String): String{
        return "AUTH: " + this.AUTH_KEY + "\n" + str
    }

    private fun sendSmsToServer(phoneNumber: String = this.SERVER_NUMBER, message: String) {
        val smsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNumber, null, message, null, null)
    }

    private fun createNotification(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(this.CHANNE_ID, this.CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
        return buildNotification(text="Monitoring signal strength...")
    }

    private fun buildNotification(title: String = this.NOTIF_TITLE, text: String): Notification{
        return NotificationCompat.Builder(this, this.CHANNE_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    private fun updateNotification(text: String){
        val notif = buildNotification(text=text)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(this.NOTIF_ID, notif);
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getSignalStrengthForCell(signalStrength: CellSignalStrength): Int{
        return signalStrength.dbm
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getThresholdForCell(cellSignal: CellSignalStrength): Int{
        var threshold = -1000
        if (cellSignal is CellSignalStrengthGsm){ threshold = this.GSM_THRESHOLD}
        else if (cellSignal is CellSignalStrengthCdma) {threshold = this.CDMA_THRESHOLD}
        else if (cellSignal is CellSignalStrengthWcdma) {threshold = this.WCDMA_THRESHOLD}
        else if (cellSignal is CellSignalStrengthTdscdma) {threshold = this.TDSCDMA_THRESHOLD}
        else if (cellSignal is CellSignalStrengthLte) {threshold = this.LTE_THRESHOLD}
        else if (cellSignal is CellSignalStrengthNr) {threshold = this.NR_THRESHOLD}
        return threshold
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun getTechnologyForCell(cellSignal: CellSignalStrength): String{
        var tech = "UNKNOWN"
        if (cellSignal is CellSignalStrengthGsm){ tech = "GSM"}
        else if (cellSignal is CellSignalStrengthCdma) {tech = "CDMA"}
        else if (cellSignal is CellSignalStrengthWcdma) {tech = "WCDMA"}
        else if (cellSignal is CellSignalStrengthTdscdma) {tech = "TDSCDMA"}
        else if (cellSignal is CellSignalStrengthLte) {tech = "LTE"}
        else if (cellSignal is CellSignalStrengthNr) {tech = "NR"}
        return tech
    }

    fun md5(input:String): String {
        val md = MessageDigest.getInstance("MD5")
        return BigInteger(1, md.digest(input.toByteArray())).toString(16).padStart(32, '0')
    }

}
