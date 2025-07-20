package br.edu.ifsp.dmo1.plannersensorial.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import br.edu.ifsp.dmo1.plannersensorial.R
import br.edu.ifsp.dmo1.plannersensorial.ui.activities.MainActivity
import kotlin.math.absoluteValue

class TaskNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {

        criarCanalDeNotificacao(context)

        val id = intent.getStringExtra("id") ?: return
        val title = intent.getStringExtra("title") ?: "Lembrete"
        val descricao = intent.getStringExtra("descricao") ?: ""

        val notificationIntent = Intent(context, MainActivity::class.java).apply {
            putExtra("taskId", id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }


        val requestCode = id.hashCode().absoluteValue

        val pendingIntent = PendingIntent.getActivity(
            context,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "task_channel")
            .setSmallIcon(R.drawable.baseline_notification_important_24)
            .setContentTitle(title)
            .setContentText(descricao)
            .setStyle(NotificationCompat.BigTextStyle().bigText(descricao))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.w("TaskReceiver", "Permissão POST_NOTIFICATIONS não concedida")
            return
        }

        notificationManager.notify(requestCode, builder.build())
    }

    private fun criarCanalDeNotificacao(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Notifications"
            val descriptionText = "Canal para notificações de tarefas"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("task_channel", name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

}
