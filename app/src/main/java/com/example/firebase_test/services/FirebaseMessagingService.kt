package com.example.firebase_test.services

import android.app.PendingIntent
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.firebase_test.MainActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import com.example.firebase_test.R
import com.example.firebase_test.data.SettingsManager
// --- ¡ESTE ES EL IMPORT CLAVE! ---
import com.example.firebase_test.workspaces.MyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class FirebaseMessagingService : FirebaseMessagingService() {

    private val settingsManager: SettingsManager by lazy {
        (application as MyApplication).settingsManager
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {

        super.onMessageReceived(remoteMessage)

        remoteMessage.data.let { data ->
            val title = data["title"]
            val body = data["body"]
            val workspaceId = data["workspaceId"] // Leemos el workspaceId

            Log.d("FCM", "¡MENSAJE RECIBIDO! Desde: ${remoteMessage.from}")
            if (title != null && body != null) {
                // 1. Lanzamos una corrutina para leer el DataStore (que es asíncrono)
                CoroutineScope(Dispatchers.IO).launch {

                    // 2. Leemos el valor ACTUAL de las preferencias
                    // .first() toma el valor más reciente del Flow y termina
                    val currentSettings = settingsManager.appSettingsFlow.first()

                    // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
                    // Ahora comprobamos AMBAS condiciones:
                    // 1. Que la app esté en primer plano
                    // 2. Que el switch de notificaciones esté activado
                    if (!MyApplication.isAppInForeground && currentSettings.allowNotifications) {
                        Log.d(
                            "FCM-Logic",
                            "App en primer plano Y flag en TRUE. Intentando enviar notificación..."
                        )
                        sendNotification(title, body, workspaceId)

                    } else {
                        // Si la app está en segundo plano, o el flag es false, se ignora.
                        Log.d(
                            "FCM-Logic",
                            "Notificación ignorada. App en segundo plano o flag en FALSE."
                        )
                    }
                    // --- FIN DEL CAMBIO ---
                }
            }
        }
    }

    /**
     * Se llama cuando el sistema genera un nuevo token de FCM.
     */
    override fun onNewToken(token: String) {
        Log.d("FCM", "Token refrescado: $token")

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            sendTokenToFirestore(token, currentUser.uid)
        } else {
            Log.w("FCM", "Token refrescado pero no hay usuario logueado.")
        }
    }

    /**
     * Esta es tu función para construir y mostrar la notificación.
     * La dejamos EXACTAMENTE IGUAL porque ya estaba correcta.
     */
    private fun sendNotification(title: String, messageBody: String, workspaceId: String?) {

        // LOG 1: ¿Se llamó a la función?
        Log.d("FCM-Notificacion", "Iniciando sendNotification. Título: $title")

        val CHANNEL_ID = "workspace_channel"

        // 1. Crear un Intent para abrir la app al tocar la notificación
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("EXTRA_WORKSPACE_ID", workspaceId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        // 2. Crear un PendingIntent (una "intención pendiente")
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this, 0, // request code
            intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // LOG 2: ¿Estamos a punto de construir el builder?
        Log.d("FCM-Notificacion", "Construyendo el Notification builder...")

        // 3. Construir la notificación
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_circle_notifications_24).setContentTitle(title)
            .setContentText(messageBody).setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent).setAutoCancel(true)

        // LOG 3: ¿Se construyó el builder?
        Log.d("FCM-Notificacion", "Builder creado. Obteniendo NotificationManagerCompat.")

        // 4. Mostrar la notificación
        with(NotificationManagerCompat.from(this)) {

            // LOG 4: ¿Vamos a chequear el permiso?
            Log.d("FCM-Notificacion", "Verificando permiso POST_NOTIFICATIONS...")

            if (ActivityCompat.checkSelfPermission(
                    this@FirebaseMessagingService, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("FCM-Notificacion", "¡PERMISO DENEGADO! Abortando notificación.")
                return
            }

            // LOG 5: ¿Permiso OK?
            Log.d("FCM-Notificacion", "Permiso CONCEDIDO.")

            val notificationId = System.currentTimeMillis().toInt()

            try {
                // LOG 6: ¿Estamos a punto de mostrarla?
                Log.d("FCM-Notificacion", "Mostrando notificación con ID: $notificationId...")
                notify(notificationId, builder.build())

                // LOG 7: ¡ÉXITO!
                Log.d("FCM-Notificacion", "¡Notificación mostrada exitosamente!")

            } catch (e: Exception) {
                // LOG 8: ¡CRASH! (Casi siempre por el ícono R.drawable)
                Log.e("FCM-Notificacion", "CRASH AL MOSTRAR NOTIFICACIÓN: ${e.message}", e)
            }
        }
    }

    /**
     * Sube el token a la subcolección /users/{userId}/tokens
     */
    private fun sendTokenToFirestore(token: String, userId: String) {
        if (token.isBlank() || userId.isBlank()) return

        val db = Firebase.firestore

        val tokenData = hashMapOf(
            "token" to token, "lastUsed" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("tokens").document(token).set(tokenData)
            .addOnSuccessListener {
                Log.d("FCM", "Token guardado en Firestore exitosamente.")
            }.addOnFailureListener { e ->
                Log.e("FCM", "Error al guardar token", e)
            }
    }
}
