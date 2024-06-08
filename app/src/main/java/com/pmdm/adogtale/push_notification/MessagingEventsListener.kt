package com.pmdm.adogtale.push_notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MessagingEventsListener : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        message.notification?.let { payload ->
            Log.i("Titulo notificacion", "${payload.title}")
            Log.i("Cuerpo notificacion", "${payload.body}")
        }
    }
}
    