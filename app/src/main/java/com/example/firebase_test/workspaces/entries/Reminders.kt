package com.example.firebase_test.workspaces.entries

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.firebase_test.R
import com.example.firebase_test.workspaces.ChecklistItem
import com.example.firebase_test.workspaces.MyApplication
import com.example.firebase_test.workspaces.WorkspaceEntry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// --- 1. RECEPTOR DE LA ALARMA (BROADCAST RECEIVER) ---

/**
 * Se activa cuando suena una alarma programada por el AlarmManager.
 * Verifica los ajustes globales de la app y luego muestra una notificación.
 */
class ReminderReceiver : BroadcastReceiver() {

    private val TAG = "ReminderReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive disparado. Verificando ajustes...")

        // Verifica si el usuario deshabilitó las notificaciones en la config de la app
        val settingsManager = (context.applicationContext as MyApplication).settingsManager
        val appSettings = runBlocking { settingsManager.appSettingsFlow.first() }
        val appSettingIsEnabled = appSettings.allowNotifications

        if (!appSettingIsEnabled) {
            Log.w(TAG, "RECEIVER BLOQUEADO: El usuario desactivó notificaciones en DataStore.")
            return
        }

        Log.d(TAG, "Ajuste de app OK. Procesando alarma...")

        val title = intent.getStringExtra("REMINDER_TITLE") ?: "Recordatorio"
        val entryId = intent.getStringExtra("ENTRY_ID") ?: ""
        val notificationId =
            if (entryId.isNotBlank()) entryId.hashCode() else System.currentTimeMillis().toInt()

        Log.d(TAG, "Recibido: title='$title', entryId='$entryId'. Llamando a sendNotification.")
        sendNotification(context, title, notificationId)
    }

    private fun sendNotification(context: Context, title: String, notificationId: Int) {
        val channelId = MyApplication.REMINDER_CHANNEL_ID
        Log.d(TAG, "sendNotification: Construyendo notificación para canal $channelId")

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.baseline_circle_notifications_24)
            .setContentTitle(title)
            .setContentText("Tu recordatorio está sonando.")
            .setPriority(NotificationCompat.PRIORITY_HIGH).setAutoCancel(true)
            .setOnlyAlertOnce(true)

        // Silencia la notificación si la app ya está abierta
        if (MyApplication.isAppInForeground) {
            Log.d(TAG, "App en primer plano. Silenciando notificación.")
            builder.setNotificationSilent()
        }

        with(NotificationManagerCompat.from(context)) {
            Log.d(TAG, "Verificando permiso de sistema POST_NOTIFICATIONS...")

            // Comprobación crucial para Android 13+
            if (ActivityCompat.checkSelfPermission(
                    context, android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "PERMISO DENEGADO: No se puede mostrar la notificación. El sistema bloqueó POST_NOTIFICATIONS.")
                return
            }

            Log.d(TAG, "Permiso de sistema OK. Mostrando notificación ID: $notificationId")
            try {
                notify(notificationId, builder.build())
                Log.d(TAG, "Notificación ID $notificationId mostrada exitosamente.")
            } catch (e: Exception) {
                Log.e(TAG, "CRASH al llamar a notify(). ¿Falta el ícono? ${e.message}", e)
            }
        }
    }
}

// --- 2. LÓGICA DE PROGRAMACIÓN DE ALARMAS ---

/**
 * Objeto Singleton para programar, cancelar y verificar alarmas
 * usando el AlarmManager del sistema.
 */
object AlarmScheduler {

    private val TAG = "AlarmScheduler"

    /** Programa una alarma exacta basada en una WorkspaceEntry */
    fun schedule(context: Context, entry: WorkspaceEntry) {
        Log.d(TAG, "schedule: Intentando programar alarma para entryId=${entry.id}")

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val entryId = entry.id
        val title = entry.value ?: "Recordatorio"

        // Extrae los datos del recordatorio
        val itemsList = entry.items as? List<ChecklistItem>
        if (itemsList.isNullOrEmpty()) {
            Log.e(TAG, "schedule (FALLO): Entry $entryId no tiene items o el 'cast' a List<ChecklistItem> falló.")
            return
        }
        val reminderItem = itemsList.first()
        val timeInMillis = reminderItem.text.toLongOrNull() ?: 0L
        val isChecked = reminderItem.isChecked

        Log.d(TAG, "schedule: Datos extraídos -> timeInMillis=${formatTime(timeInMillis)}, isChecked=$isChecked")

        // Si el switch está apagado, cancela cualquier alarma existente
        if (!isChecked) {
            Log.d(TAG, "schedule (CANCELANDO): Alarma $entryId está desactivada (isChecked=false). Llamando a cancel().")
            cancel(context, entryId)
            return
        }

        // No reprogramar alarmas pasadas
        if (timeInMillis == 0L || timeInMillis < System.currentTimeMillis()) {
            Log.w(TAG, "schedule (OMITIENDO): Alarma $entryId está en el pasado ($timeInMillis). Omitiendo.")
            return
        }

        // Evitar duplicados
        if (isAlarmScheduled(context, entryId)) {
            Log.d(TAG, "schedule (OMITIENDO): Alarma $entryId ya está programada. No se duplicará.")
            return
        }

        Log.d(TAG, "schedule: Creando PendingIntent para $entryId")
        val pendingIntent = createPendingIntent(
            context, entryId, title, PendingIntent.FLAG_UPDATE_CURRENT
        )

        try {
            Log.d(TAG, "schedule: Verificando permisos de alarma...")
            // Manejo de permisos para Android 12 (S) y superior
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    Log.d(TAG, "schedule (SDK >= S): Permiso canScheduleExactAlarms CONCEDIDO. Usando setExactAndAllowWhileIdle.")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent!!
                    )
                } else {
                    Log.w(TAG, "schedule (SDK >= S): Permiso canScheduleExactAlarms DENEGADO. Usando set() (alarma inexacta).")
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent!!)
                }
            } else {
                // Versiones anteriores a Android 12
                Log.d(TAG, "schedule (SDK < S): Usando setExactAndAllowWhileIdle.")
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent!!
                )
            }
            Log.i(TAG, "schedule (¡ÉXITO!): Alarma $entryId ('$title') programada para ${formatTime(timeInMillis)}")

        } catch (e: SecurityException) {
            Log.e(TAG, "schedule (CRASH): SecurityException. ¿Falta SCHEDULE_EXACT_ALARM o USE_EXACT_ALARM en AndroidManifest.xml?", e)
        }
    }

    /** Cancela una alarma programada por su ID */
    fun cancel(context: Context, entryId: String) {
        Log.d(TAG, "cancel: Intentando cancelar alarma para entryId=$entryId")
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Busca el PendingIntent sin crearlo
        val pendingIntent = createPendingIntent(
            context, entryId, "", PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.i(TAG, "cancel (¡ÉXITO!): Alarma $entryId cancelada.")
        } else {
            Log.w(TAG, "cancel (OMITIENDO): Alarma $entryId no encontrada para cancelar (PendingIntent nulo).")
        }
    }

    /** Verifica si una alarma ya existe */
    fun isAlarmScheduled(context: Context, entryId: String): Boolean {
        Log.d(TAG, "isAlarmScheduled: Verificando si $entryId existe...")
        val exists = createPendingIntent(
            context, entryId, "", PendingIntent.FLAG_NO_CREATE
        ) != null
        Log.d(TAG, "isAlarmScheduled: Resultado para $entryId -> $exists")
        return exists
    }

    /** Helper para crear un PendingIntent único para cada alarma */
    private fun createPendingIntent(
        context: Context, entryId: String, title: String, flag: Int
    ): PendingIntent? {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("REMINDER_TITLE", title)
            putExtra("ENTRY_ID", entryId)
            // Usar 'data' asegura que el PendingIntent sea único por entryId
            data = android.net.Uri.parse("reminder://$entryId")
        }
        val requestCode = entryId.hashCode()
        return PendingIntent.getBroadcast(
            context, requestCode, intent, flag or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** Helper interno para logging */
    internal fun formatTime(timeInMillis: Long): String {
        return SimpleDateFormat(
            "dd/MM/yyyy HH:mm:ss", Locale.getDefault()
        ).format(Date(timeInMillis))
    }
}

// --- 3. DIÁLOGO DE CREACIÓN DE RECORDATORIO ---
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReminderDialog(
    onConfirm: (title: String, items: List<ChecklistItem>) -> Unit, onDismiss: () -> Unit
) {
    val TAG = "ReminderDialog"

    var title by rememberSaveable { mutableStateOf("") }
    val timePickerState = rememberTimePickerState(is24Hour = true)
    var showTimePickerDialog by remember { mutableStateOf(false) }

    val formattedTime = remember(timePickerState.hour, timePickerState.minute) {
        String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reminder_dialog_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.reminder_dialog_label)) },
                    singleLine = true
                )
                Button(
                    onClick = { showTimePickerDialog = true }, modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reminder_dialog_select_time_button, formattedTime))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val calendar = Calendar.getInstance().apply {
                        set(Calendar.HOUR_OF_DAY, timePickerState.hour)
                        set(Calendar.MINUTE, timePickerState.minute)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                        if (before(Calendar.getInstance())) {
                            add(Calendar.DATE, 1)
                        }
                    }
                    val reminderTimeInMillis = calendar.timeInMillis

                    val reminderItem = ChecklistItem(
                        text = reminderTimeInMillis.toString(),
                        isChecked = true
                    )

                    Log.d(
                        TAG,
                        "onConfirm: Título='$title', TimeInMillis=$reminderTimeInMillis (${
                            AlarmScheduler.formatTime(
                                reminderTimeInMillis
                            )
                        })"
                    )
                    onConfirm(title, listOf(reminderItem))
                }, enabled = title.isNotBlank()
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        })

    if (showTimePickerDialog) {
        TimePickerDialog(
            onDismiss = { showTimePickerDialog = false },
            onConfirm = { showTimePickerDialog = false },
            timePickerState = timePickerState
        )
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit, onConfirm: () -> Unit, timePickerState: TimePickerState
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.time_picker_dialog_title)) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(R.string.common_accept))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        })
}