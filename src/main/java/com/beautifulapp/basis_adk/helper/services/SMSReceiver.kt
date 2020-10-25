package com.beautifulapp.basis_adk.helper.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsMessage
import androidx.core.os.bundleOf
import com.beautifulapp.basis_adk.sendBroadcast

class SMSReceiver : BroadcastReceiver() {
/*Todo: A ajouter dans le manifest
    <uses-permission android:name="android.permission.RECEIVE_SMS" />
    <uses-permission android:name="android.permission.READ_SMS" />

    <receiver
            android:name="com.beautifulapp.james_adk.helper.services.SMSReceiver"
            android:enabled="true">
            <intent-filter android:priority="2147483647">
                <category android:name="android.intent.category.DEFAULT" />

                <action android:name="android.provider.Telephony.SMS_RECEIVED" />
            </intent-filter>
        </receiver>
 */

    /*Exemple d'usage dans le fragment
    private val broadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == SMSReceiver.RECEIVE_SMS_ACTION) {
                val msg = intent.getBundleExtra(SMSReceiver.RECEIVE_SMS_MESSAGE_KEY)
                if (msg[SMSReceiver.RECEIVE_SMS_FROM_KEY] != MY_PHONE) return
                val matcher = Pattern.compile("(|^)\\d{4}").matcher(msg[SMSReceiver.RECEIVE_SMS_BODY_KEY] as String)
                if (matcher.find()) {
                    identifierCode.value = matcher.group(0)
                    //xmlSignIn(null)
                }
            }

        }
    }

     */

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            intent?.extras?.let { bundle ->
                try {
                    val pdus = bundle.get("pdus") as Array<Any>
                    pdus.map {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) SmsMessage.createFromPdu(
                            it as ByteArray,
                            bundle.getString("format")
                        ) else SmsMessage.createFromPdu(it as ByteArray)
                    }
                        .forEach {
                            val mIntent = Intent(RECEIVE_SMS_ACTION).putExtra(
                                RECEIVE_SMS_MESSAGE_KEY, bundleOf(
                                    RECEIVE_SMS_BODY_KEY to it.messageBody,
                                    RECEIVE_SMS_FROM_KEY to it.originatingAddress
                                )
                            )
                            sendBroadcast(mIntent, null, context)
                        }
                } catch (e: Exception) {
                }
            }

        }
    }

    companion object {
        const val RECEIVE_SMS_ACTION = "receiveSms"
        const val RECEIVE_SMS_MESSAGE_KEY = "message"
        const val RECEIVE_SMS_BODY_KEY = "body"
        const val RECEIVE_SMS_FROM_KEY = "from"
    }
}