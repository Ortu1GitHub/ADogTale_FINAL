package com.pmdm.adogtale.push_notification

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.pmdm.adogtale.utils.FirebaseUtil

class MessagingEventsListener : FirebaseMessagingService() {

    private val deviceTokenHandler: DeviceTokenHandler = DeviceTokenHandler();
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance();
    private val firebaseUtil: FirebaseUtil = FirebaseUtil();

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let { payload ->
            Log.i("Titulo notificacion", "${payload.title}")
            Log.i("Cuerpo notificacion", "${payload.body}")
        }
    }


}
    