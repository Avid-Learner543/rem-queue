package com.avidlearner.rem.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.avidlearner.rem.data.datastore.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val timerDao = com.avidlearner.rem.data.room.AppDatabase.getDatabase(context).timerDao()
                    val enabledTimers = timerDao.getEnabledTimersSync()
                    val alarmScheduler = AlarmScheduler(context)
                    
                    for (timer in enabledTimers) {
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
}
