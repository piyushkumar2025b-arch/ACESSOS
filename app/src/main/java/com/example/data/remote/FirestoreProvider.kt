package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.R
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

/**
 * Shared singleton provider for FirebaseFirestore using the configured database ID.
 */
object FirestoreProvider {
    private const val TAG = "FirestoreProvider"

    @Volatile
    private var firestoreInstance: FirebaseFirestore? = null

    fun initialize(context: Context): FirebaseFirestore? {
        if (firestoreInstance != null) return firestoreInstance
        return synchronized(this) {
            if (firestoreInstance != null) return firestoreInstance
            try {
                val dbId = context.applicationContext.getString(R.string.firestore_database_id)
                val app = FirebaseApp.getInstance()
                val db = FirebaseFirestore.getInstance(app, dbId)
                @Suppress("DEPRECATION")
                try {
                    val settings = FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)
                        .build()
                    db.firestoreSettings = settings
                    Log.d(TAG, "Firestore configured for database '$dbId' with persistence enabled.")
                } catch (se: Exception) {
                    Log.d(TAG, "Firestore persistence already configured: ${se.message}")
                }
                firestoreInstance = db
                db
            } catch (e: Exception) {
                Log.w(TAG, "Failed to initialize FirebaseFirestore instance: ${e.message}")
                null
            }
        }
    }

    val instance: FirebaseFirestore?
        get() {
            if (firestoreInstance != null) return firestoreInstance
            return synchronized(this) {
                if (firestoreInstance != null) return firestoreInstance
                try {
                    val app = FirebaseApp.getInstance()
                    val context = app.applicationContext
                    val dbId = context.getString(R.string.firestore_database_id)
                    val db = FirebaseFirestore.getInstance(app, dbId)
                    firestoreInstance = db
                    db
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get FirebaseFirestore instance: ${e.message}")
                    null
                }
            }
        }
}
