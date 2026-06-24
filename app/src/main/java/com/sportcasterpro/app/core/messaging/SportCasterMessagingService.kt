package com.sportcasterpro.app.core.messaging

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Receives push notifications (match reminders, remote-scoring invites, stream health alerts).
 * Phase 2 wires this into a notification channel + the remote score controller pairing flow.
 */
class SportCasterMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Message received from ${message.from}: ${message.data}")
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM token issued")
    }

    private companion object {
        const val TAG = "SportCasterFCM"
    }
}
