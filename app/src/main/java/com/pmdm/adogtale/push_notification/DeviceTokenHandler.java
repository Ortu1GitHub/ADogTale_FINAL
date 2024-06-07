package com.pmdm.adogtale.push_notification;

import android.util.Log;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DeviceTokenHandler {


    public void storeDeviceToken(String userEmail, String deviceToken) {
        Map<String, Object> data = new HashMap<>();
        data.put("user_email", userEmail);
        data.put("device_token", deviceToken);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("device_tokens")
                .document(userEmail)
                .set(data, SetOptions.merge())
                .addOnSuccessListener(documentReference -> {
                    Log.i("DeviceTokenHandler", "Device token stored successfully for userEmail: " + userEmail);
                })
                .addOnFailureListener(e -> {
                    Log.e("DeviceTokenHandler", "Error storing device token for userEmail: " + userEmail);
                });
    }

    public CompletableFuture<String> retrieveDeviceToken(String userEmail) {
        CompletableFuture<String> deviceTokenFuture = new CompletableFuture<>();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("device_tokens").document(userEmail);

        docRef.get()
                .addOnCompleteListener(task -> {

                    if (!task.isSuccessful()) {
                        Log.e("DeviceTokenHandler", "Error retrieving device token for userEmail: " + userEmail);
                        deviceTokenFuture.completeExceptionally(new Exception("Error retrieving device token for userEmail: " + userEmail));
                        return;
                    }

                    DocumentSnapshot document = task.getResult();

                    if (!document.exists()) {
                        Log.e("DeviceTokenHandler", "No device token found for userEmail: " + userEmail);
                        deviceTokenFuture.completeExceptionally(new Exception("No device token found for userEmail: " + userEmail));
                        return;
                    }

                    String deviceToken = document.getString("device_token");
                    Log.i("DeviceTokenHandler", "Device token for userEmail " + userEmail + ": " + deviceToken);
                    deviceTokenFuture.complete(deviceToken);
                });

        return deviceTokenFuture;
    }

}
