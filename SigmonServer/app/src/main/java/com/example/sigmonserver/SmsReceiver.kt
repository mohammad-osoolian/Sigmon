package com.example.sigmonserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.telephony.SmsMessage
import android.util.Log
import android.widget.TextView
import android.widget.Toast

class SmsReceiver(private val handler: Handler, private val logsview: TextView) : BroadcastReceiver() {
    private var Keys = listOf<String>("7cdc766219346fe7010d3115dec48e77", //mohammad_osl
                                      "21232f297a57a5a743894a0e4a801fc3", //admin
                                      "24c9e15e52afc47c225b757e7bee1f9d", //user1
                                      "7e58d63b60197ceb55a1c487989a3720", //user2
                                      "92877af70a45fd6a2ed7fe81e1236b78", //user3
                                      "3f02ebe3d7929b091e3d8ccfde2f3bc6", //user4
                                      "0a791842f52a0acfbb3a783378c066b8", //user5
                                      "0baea2f0ae20150db78f58cddac442a9", //superuser
                                      "81dc9bdb52d04dc20036dbd8313ed055") //1234
    override fun onReceive(context: Context, intent: Intent?) {
        val bundle: Bundle? = intent?.extras
        try {
            if (bundle != null) {
                val pdus = bundle.get("pdus") as Array<*>
                for (pdu in pdus) {
                    val msg = SmsMessage.createFromPdu(pdu as ByteArray)
                    val sender = msg.originatingAddress
                    val message = msg.messageBody.toString()
                    var logmsg = "INVALID: Sms from $sender is not valid"
                    if (isValidReport(message)){
                        logmsg = "UNAUTHORIZED: Sms from $sender is not unauthorized"
                        var response = "Authentication Key is not valid!"
                        if (isAuthenticated(message)) {
                            response = "Report received"
                            logmsg = "REPORT: sms report from $sender"
                        }
                        sendResponse(context, sender, response)
                    }
                    handler.post {
                        logsview.append("\n----------------\n$logmsg")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SmsReceiver", "Exception: ${e.message}")
        }
    }

    private fun sendResponse(context: Context?, phoneNumber: String?, message: String) {
        if (context != null && phoneNumber != null) {
            val smsManager = android.telephony.SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
        }
    }

    private fun isValidReport(msg: String): Boolean{
        val auth_line = msg.split("\n")[0]
        return auth_line.contains("AUTH: ")
    }

    private fun isAuthenticated(msg: String): Boolean{
        val auth_key = msg.split("\n")[0].replace("AUTH: ", "")
        return this.Keys.contains(auth_key)
    }
}
