package com.avidlearner.rem.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.avidlearner.rem.data.datastore.SettingsRepository
import com.avidlearner.rem.data.room.AppDatabase
import com.avidlearner.rem.data.room.Quote
import com.avidlearner.rem.data.room.Timer
import com.avidlearner.rem.notification.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val quoteDao = AppDatabase.getDatabase(application).quoteDao()
    private val timerDao = AppDatabase.getDatabase(application).timerDao()
    private val settingsRepo = SettingsRepository(application)
    private val alarmScheduler = AlarmScheduler(application)

    val allQuotes: StateFlow<List<Quote>> = quoteDao.getAllQuotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val allTimers: StateFlow<List<Timer>> = timerDao.getAllTimers().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appSettings: StateFlow<SettingsRepository.AppSettings> = settingsRepo.appSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsRepository.AppSettings("Quote of the Day", false, false, 0xFF6200EE, false)
    )

    fun addQuote(text: String) {
        viewModelScope.launch {
            val position = System.currentTimeMillis()
            quoteDao.insertQuote(Quote(text = text, queuePosition = position))
        }
    }

    fun updateQuote(quote: Quote) {
        viewModelScope.launch {
            quoteDao.updateQuote(quote)
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            quoteDao.deleteQuote(quote)
        }
    }

    fun shuffleQuotes() {
        viewModelScope.launch {
            val quotes = quoteDao.getAllQuotesSync()
            val shuffled = quotes.shuffled()
            shuffled.forEachIndexed { index, quote ->
                quoteDao.updateQueuePosition(quote.id, System.currentTimeMillis() + index)
            }
        }
    }

    fun addTimer(hour: Int, minute: Int) {
        viewModelScope.launch {
            val timer = Timer(hour = hour, minute = minute)
            val id = timerDao.insertTimer(timer)
            alarmScheduler.scheduleAlarm(timer.copy(id = id.toInt()))
        }
    }

    fun updateTimer(timer: Timer) {
        viewModelScope.launch {
            timerDao.updateTimer(timer)
            if (timer.isEnabled) {
                alarmScheduler.scheduleAlarm(timer)
            } else {
                alarmScheduler.cancelAlarm(timer)
            }
        }
    }

    fun deleteTimer(timer: Timer) {
        viewModelScope.launch {
            timerDao.deleteTimer(timer)
            alarmScheduler.cancelAlarm(timer)
        }
    }

    fun updateNotificationTitle(title: String) {
        viewModelScope.launch {
            settingsRepo.saveNotificationTitle(title)
        }
    }

    fun updateIsPersistent(isPersistent: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveIsPersistent(isPersistent)
        }
    }

    fun updateIsDarkMode(isDarkMode: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveIsDarkMode(isDarkMode)
        }
    }

    fun updateNotificationColor(color: Long) {
        viewModelScope.launch {
            settingsRepo.saveNotificationColor(color)
        }
    }

    fun updateUseAdaptiveTheme(useAdaptive: Boolean) {
        viewModelScope.launch {
            settingsRepo.saveUseAdaptiveTheme(useAdaptive)
        }
    }
}
