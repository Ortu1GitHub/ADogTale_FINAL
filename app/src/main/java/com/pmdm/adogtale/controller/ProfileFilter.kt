package com.pmdm.adogtale.controller

import android.util.Log
import com.pmdm.adogtale.model.Profile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class ProfileFilter {
    // Breed filter
    fun filterByBreed(userProfile: Profile, otherProfile: Profile): Profile {
        if (userProfile.breed == otherProfile.breed) {
            return otherProfile
        }
        return Profile()
    }

    // Gender filter
    fun filterByGender(userProfile: Profile, otherProfile: Profile): Profile {
        if (userProfile.gender != otherProfile.gender) {
            return otherProfile
        }
        return Profile()
    }

    // Age filter
    fun filterByAge(userProfile: Profile, otherProfile: Profile): Profile {
        val age = otherProfile.age.toInt()
        val minAge = userProfile.preferedLowAge.toInt()
        val maxAge = userProfile.preferedHighAge.toInt()

        if (age >= minAge && age <= maxAge) {
            val userAge = userProfile.age.toInt()
            if (otherProfile.preferedLowAge.toInt() <= userAge && otherProfile.preferedHighAge.toInt() >= userAge) {
                Log.i("Age check passed: ", "Age: $age, Min: $minAge, Max: $maxAge")
                return otherProfile
            }
        }

        Log.i("Age check failed: ", "Age: $age, Min: $minAge, Max: $maxAge")
        return Profile()
    }

    // Obtain profiles filtered by distance
    /* suspend fun filterByDistance(userProfile: Profile, otherProfile: Profile): Profile? {
         return withContext(Dispatchers.IO) {
             val client = OkHttpClient()
             val url =
                 "https://maps.googleapis.com/maps/api/distancematrix/json?origins=${userProfile.town}&destinations=${otherProfile.town}&key=AIzaSyAz4lTqtyCWzyGF2SxjID2OAl-oHegMzIE"
             val request = Request.Builder().url(url).build()

             client.newCall(request).execute().use { response ->
                 if (!response.isSuccessful) throw IOException("Unexpected code $response")

                 val bodyString = response.body()?.string()
                 try {
                     val jsonObject = JSONObject(bodyString)
                     val rows = jsonObject.getJSONArray("rows")
                     if (rows.length() > 0) {
                         val elements = rows.getJSONObject(0).getJSONArray("elements")
                         if (elements.length() > 0) {
                             val distanceObject = elements.getJSONObject(0).optJSONObject("distance")
                             val distance = distanceObject?.optString("text")
                             if (!distance.isNullOrBlank()) {
                                 Log.i(
                                     "distance",
                                     "La distancia entre ${userProfile.town} y ${otherProfile.town} es $distance"
                                 )
                                 val prefDistance =
                                     userProfile.prefDistance.replace("km", "").toDouble()
                                 val distanceInKm = distance.replace("km", "").toDouble()

                                 if (prefDistance >= distanceInKm) {
                                     Log.i("distance", "perfil bueno " + otherProfile.name)
                                     return@withContext otherProfile
                                 } else {
                                     Log.i(
                                         "distance",
                                         "La distancia es larga y grande para " + otherProfile.name
                                     )
                                     return@withContext null
                                 }
                             } else {
                                 Log.i("distance", "No se encontró la distancia en el JSON")
                                 return@withContext null
                             }
                         } else {
                             Log.i("distance", "No se encontraron elementos en el JSON")
                             return@withContext null
                         }
                     } else {
                         Log.i("distance", "No se encontraron filas en el JSON")
                         return@withContext null
                     }
                 } catch (e: JSONException) {
                     Log.i("distance", "Error al parsear el JSON", e)
                     return@withContext null
                 }
             }
         }
     }
 */
    suspend fun filterByDistance(userProfile: Profile, otherProfile: Profile): Profile? {
        return withContext(Dispatchers.IO) {
            try {
                val client = OkHttpClient()
                val url =
                    "https://maps.googleapis.com/maps/api/distancematrix/json?origins=${userProfile.town}&destinations=${otherProfile.town}&key=AIzaSyAz4lTqtyCWzyGF2SxjID2OAl-oHegMzIE"
                val request = Request.Builder().url(url).build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val bodyString = response.body?.string()
                    val jsonObject = JSONObject(bodyString)

                    val rows = jsonObject.getJSONArray("rows")
                    if (rows.length() > 0) {
                        val elements = rows.getJSONObject(0).getJSONArray("elements")
                        if (elements.length() > 0) {
                            val distanceObject = elements.getJSONObject(0).optJSONObject("distance")
                            val distance = distanceObject?.optString("text")

                            if (!distance.isNullOrBlank()) {
                                val prefDistance =
                                    userProfile.prefDistance.replace("km", "").toDouble()
                                val distanceInKm = distance.replace("km", "").toDouble()

                                if (prefDistance >= distanceInKm) {
                                    return@withContext otherProfile
                                } else {
                                    return@withContext null
                                }
                            }
                        }
                    }
                    return@withContext null
                }
            } catch (e: Exception) {
                Log.e("filterByDistance", "Error: ${e.message}", e)
                return@withContext null
            }
        }
    }
}