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
                val timerId = intent.getIntExtra("TIMER_ID", -1)
                val database = AppDatabase.getDatabase(context)
                val quoteDao = database.quoteDao()
                val timerDao = database.timerDao()
                val settingsRepo = SettingsRepository(context)

                val timer = if (timerId != -1) timerDao.getTimerById(timerId) else null

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
                val alarmScheduler = AlarmScheduler(context)
                if (timer != null && timer.isEnabled) {
                    alarmScheduler.scheduleAlarm(timer)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }
}
