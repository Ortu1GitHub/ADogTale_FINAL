package com.pmdm.adogtale.controller

import android.util.Log
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore
import com.pmdm.adogtale.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class FeedFilter {

    private val API_KEY_MAPS: String = "AIzaSyA_w5cPKGXr5mneEe4qvlIgEJxIBVViZ7s"

    fun isSameLookingFor(userProfile: Profile, otherProfile: Profile): Boolean {
        return userProfile.lookingFor == otherProfile.lookingFor
    }

    fun isSameBreed(userProfile: Profile, otherProfile: Profile): Boolean {
        return userProfile.breed == otherProfile.breed
    }

    fun isDifferentGender(userProfile: Profile, otherProfile: Profile): Boolean {
        return userProfile.gender != otherProfile.gender
    }

    fun isBetweenAgeInterval(userProfile: Profile, otherProfile: Profile): Boolean {
        return (otherProfile.age.toInt() >= userProfile.preferedLowAge.toInt()) && (otherProfile.age.toInt() <= userProfile.preferedHighAge.toInt())
    }

    suspend fun isAlreadyLikedBy(userProfile: Profile, otherProfile: Profile): Boolean {
        return FirebaseFirestore.getInstance()
            .collection("profiles_matching")
            .whereEqualTo("user_original", userProfile.userEmail)
            .whereEqualTo("user_target", otherProfile.userEmail)
            .count()
            .get(AggregateSource.SERVER)
            .await()
            .count > 0

    }

    suspend fun isInRangeOfDistance(userProfile: Profile, otherProfile: Profile): Boolean {
        return withContext(Dispatchers.IO) {
            val client = OkHttpClient()
            val url =
                "https://maps.googleapis.com/maps/api/distancematrix/json?origins=${userProfile.town}&destinations=${otherProfile.town}&key=${API_KEY_MAPS}"
            val request = Request.Builder().url(url).build()

            client.newCall(request)
                .execute()
                .use { response ->
                    if (!response.isSuccessful) {
                        Log.e("FeedFilter", "Error getting API distance response");
                        Log.e("FeedFilter", response.toString())
                        return@withContext false
                    }

                    val bodyString = response.body?.string()
                    val jsonObject = JSONObject(bodyString)

                    val rows = jsonObject.getJSONArray("rows")

                    if (rows.length() <= 0) {
                        Log.e("FeedFilter", "Error reading response");
                        Log.e("FeedFilter", response.toString())
                        return@withContext false
                    }

                    val elements = rows.getJSONObject(0).getJSONArray("elements")

                    if (elements.length() <= 0) {
                        Log.e("FeedFilter", "Error reading response");
                        Log.e("FeedFilter", response.toString())
                        return@withContext false
                    }

                    val statusObject = elements.getJSONObject(0).optString("status")

                    if (!statusObject.uppercase().equals("OK")) {
                        Log.i("FeedFilter", "Distancia muy larga o sin ruta directa entre ambos puntos")
                        return@withContext false
                    }

                    val distanceObject = elements.getJSONObject(0).optJSONObject("distance")
                    val distance = distanceObject?.optString("text")

                    if (distance.isNullOrBlank()) {
                        Log.e("FeedFilter", "Error reading response");
                        Log.e("FeedFilter", response.toString())
                        return@withContext false
                    }

                    Log.i("FeedFilter", "respuesta API maps: " + jsonObject)

                    val prefDistance = userProfile.prefDistance.replace("km", "").toDouble()
                    val distanceInKm = cleanDistance(distance)

                    Log.i("FeedFilter", "prefDistance: " + prefDistance)
                    Log.i("FeedFilter", "distanceInKm: " + distanceInKm)

                    return@withContext prefDistance >= distanceInKm

                }
        }
    }

    private fun cleanDistance(original: String): Double {

        if (original.lowercase().contains("km")) {
            return original.lowercase().replace("km", "").toDouble()
        }

        if (original.lowercase().contains("m")) {
            val distanceInMeters = original.lowercase().replace("m", "").toDouble()
            return distanceInMeters / 1000
        }

        return original.toDouble()
    }

}