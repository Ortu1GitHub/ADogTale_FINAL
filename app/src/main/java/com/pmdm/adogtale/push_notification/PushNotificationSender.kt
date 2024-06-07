package com.pmdm.adogtale.push_notification

import android.util.Log
import com.pmdm.adogtale.model.User
import com.pmdm.adogtale.utils.FirebaseUtil
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONObject
import java.io.IOException

private const val INTENT_ON_CLICK = "adt.OPEN_CHAT_ACTION"

class PushNotificationSender {

    val NOTIFICATION_TITLE: String = "El usuario %s te ha enviado un mensaje";

    val firebaseUtil = FirebaseUtil();

    val deviceTokenHandler: DeviceTokenHandler = DeviceTokenHandler();

    fun sendNotification(message: String?, senderUser: User, targetUser: User) {

        val jsonObject = JSONObject()

        val notificationObj = JSONObject()
        notificationObj.put("title", String.format(NOTIFICATION_TITLE, senderUser.username))
        notificationObj.put("body", message)
        notificationObj.put("click_action", INTENT_ON_CLICK)

        val replyToObj = JSONObject()
        val fUser = firebaseUtil.getCurrentFirebaseUser()
        replyToObj.put("userId", senderUser.userId)
        replyToObj.put("username", senderUser.username)
        replyToObj.put("email", senderUser.email)

        val dataObj = JSONObject()
        dataObj.put("reply", replyToObj)

        val androidObj = JSONObject()
        androidObj.put(
            "notification",
            (JSONObject()).put("icon", "bell_solid")
        )

        jsonObject.put("notification", notificationObj)
        jsonObject.put("data", dataObj)
        jsonObject.put("android", androidObj)

        Log.i("DeviceTokenHandler", "replyToEmail: " + senderUser.email)
        Log.i("DeviceTokenHandler", "target user email: " + targetUser.email)
        Log.i("DeviceTokenHandler", "notificationRequest: " + jsonObject.toString())

        deviceTokenHandler.retrieveDeviceToken(targetUser.email).thenAccept { deviceToken ->

            Log.i("DeviceTokenHandler", "deviceToken inside PushNotificationSender: " + deviceToken)
            jsonObject.put("to", deviceToken)

            callApi(jsonObject)
        }

    }

    fun sendNotification(pushNotificationData: PushNotificationData){
        val jsonObject = JSONObject()

        val notificationObj = JSONObject()
        notificationObj.put("title", pushNotificationData.title)
        notificationObj.put("body", pushNotificationData.body)
        notificationObj.put("click_action", INTENT_ON_CLICK)

        val replyToObj = JSONObject()
        val fUser = firebaseUtil.getCurrentFirebaseUser()
        replyToObj.put("userId", pushNotificationData.originUser.userId)
        replyToObj.put("username", pushNotificationData.originUser.username)
        replyToObj.put("email", pushNotificationData.originUser.email)

        val dataObj = JSONObject()
        dataObj.put("reply", replyToObj)

        val androidObj = JSONObject()
        androidObj.put(
            "notification",
            (JSONObject()).put("icon", "bell_solid")
        )

        jsonObject.put("notification", notificationObj)
        jsonObject.put("data", dataObj)
        jsonObject.put("android", androidObj)

        Log.i("DeviceTokenHandler", "replyToEmail: " + pushNotificationData.originUser.email)
        Log.i("DeviceTokenHandler", "target user email: " + pushNotificationData.targetUser.email)
        Log.i("DeviceTokenHandler", "notificationRequest: " + jsonObject.toString())

        deviceTokenHandler.retrieveDeviceToken(pushNotificationData.targetUser.email).thenAccept { deviceToken ->

            Log.i("DeviceTokenHandler", "deviceToken inside PushNotificationSender: " + deviceToken)
            jsonObject.put("to", deviceToken)

            callApi(jsonObject)
        }
    }

    private fun callApi(jsonObject: JSONObject) {

        val tokenLegacy =
            "AAAAp2fg8rs:APA91bE6XygZFeDdc2FCENGze4c_t5FuI8uC3ooGHPOCvn7fcrIBO4dEeBB2S4_gDYDfel09xDSc7xNuputsjL5lllcH1u5QvWSX2ba1WvxYTjdDSSGnZLzRuYdpXN-5XKgr2Lx_fzEG";
        val client = OkHttpClient()
        val url = "https://fcm.googleapis.com/fcm/send"

        Log.i("callApi jsonObject", jsonObject.toString())

        val mediaType: MediaType = "application/json; charset=utf-8".toMediaType()
        Log.i("callApi JSON", mediaType.toString())


        val body: RequestBody = RequestBody.create(mediaType, jsonObject.toString())
        Log.i("callApi body", body.toString())

        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .header("Authorization", "Bearer " + tokenLegacy)
            .build()

        Log.i("callApi request", request.toString())

        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    Log.i("callApi onFailure", "dentro")
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    Log.i("callApi onResponse", call.toString())
                    Log.i("callApi onResponse", response.toString())
                }
            })
    }

}