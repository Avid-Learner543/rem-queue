package com.avidlearner.rem.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.avidlearner.rem.data.datastore.SettingsRepository
import com.avidlearner.rem.data.room.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val database = AppDatabase.getDatabase(context)
                val quoteDao = database.quoteDao()
                val settingsRepo = SettingsRepository(context)

                // 1. Fetch quote
                val firstQuote = quoteDao.getFirstInQueue()
                
                if (firstQuote != null) {
                    // 2. Read preferences
                    val appSettings = settingsRepo.appSettingsFlow.first()

                    // 3. Show Notification
                    val notificationHelper = NotificationHelper(context)
                    notificationHelper.showQuoteNotification(
                        title = appSettings.title,
                        quoteText = firstQuote.text,
                        isPersistent = appSettings.isPersistent,
                        color = appSettings.color
                    )

                    // 4. Update queue position
                    val newPosition = System.currentTimeMillis()
                    quoteDao.updateQueuePosition(firstQuote.id, newPosition)
                }

                // 5. Reschedule alarm automatically for the next occurrence
                // Need to fetch current set hour & minute to reschedule
                val settings = settingsRepo.appSettingsFlow.first()
                val alarmScheduler = AlarmScheduler(context)
                alarmScheduler.scheduleAlarm(settings.hour, settings.minute)
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
