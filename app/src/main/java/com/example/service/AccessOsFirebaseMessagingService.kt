package com.example.service

import android.content.Context
import android.util.Log
import com.example.data.remote.FirestoreProvider
import com.example.util.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AccessOsFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "AccessOsFCMService"

    @Deprecated("Deprecated in FirebaseMessagingService")
    @Suppress("DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "Refreshed FCM registration token: $token")

        // 1. Cache token locally
        val prefs = getSharedPreferences("access_os_fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // 2. Persist to Firestore if available
        val db = FirestoreProvider.instance ?: return
        val familyId = getSharedPreferences("access_os_session_prefs", Context.MODE_PRIVATE)
            .getString("current_family_id", "FAM-8821") ?: "FAM-8821"

        val tokenKey = token.takeLast(20).replace("/", "_").replace(".", "_")
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.collection("families")
                    .document(familyId)
                    .collection("fcm_tokens")
                    .document(tokenKey)
                    .set(
                        mapOf(
                            "token" to token,
                            "deviceRole" to "Elder/Family Device",
                            "deviceModel" to "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}",
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                Log.d(tag, "Rotated FCM token registered into family $familyId")
            } catch (e: Exception) {
                Log.w(tag, "Failed to register rotated FCM token: ${e.message}")
            }
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "AccessOS Alert"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "New notification"

        if (title.contains("SOS", ignoreCase = true) || title.contains("EMERGENCY", ignoreCase = true)) {
            NotificationHelper.showSosDispatchedNotification(
                this,
                remoteMessage.data["triggeredBy"] ?: "Elder Member",
                remoteMessage.data["location"] ?: body
            )
        } else if (title.contains("Medicine", ignoreCase = true)) {
            NotificationHelper.showMedicineNotification(
                this,
                remoteMessage.data["medicineName"] ?: body,
                remoteMessage.data["time"] ?: "Now"
            )
        }
    }
}
