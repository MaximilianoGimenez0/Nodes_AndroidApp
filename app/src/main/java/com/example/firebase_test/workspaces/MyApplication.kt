package com.example.firebase_test.workspaces

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.firebase_test.data.SettingsManager

class MyApplication : Application(), DefaultLifecycleObserver {
    lateinit var settingsManager: SettingsManager

    companion object {
        var isAppInForeground = false
            private set

        const val REMINDER_CHANNEL_ID = "reminder_channel"
        const val WORKSPACE_CHANNEL_ID = "workspace_channel"
    }

    override fun onCreate() {
        settingsManager = SettingsManager(applicationContext)
        super<Application>.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        createNotificationChannels()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        isAppInForeground = true
        Log.d("MyApplication", "App en PRIMER PLANO")
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        isAppInForeground = false
        Log.d("MyApplication", "App en SEGUNDO PLANO")
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val reminderChannel = NotificationChannel(
                REMINDER_CHANNEL_ID, "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarmas y recordatorios programados"
            }

            val workspaceChannel = NotificationChannel(
                WORKSPACE_CHANNEL_ID,
                "Notificaciones de Workspace",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes y actualizaciones"
            }

            notificationManager.createNotificationChannel(reminderChannel)
            notificationManager.createNotificationChannel(workspaceChannel)
        }
    }
}