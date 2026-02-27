package com.avidlearner.rem.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.avidlearner.rem.data.datastore.SettingsRepository
import com.avidlearner.rem.data.room.AppDatabase
import com.avidlearner.rem.data.room.Quote
import com.avidlearner.rem.notification.AlarmScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val quoteDao = AppDatabase.getDatabase(application).quoteDao()
    private val settingsRepo = SettingsRepository(application)
    private val alarmScheduler = AlarmScheduler(application)

    val allQuotes: StateFlow<List<Quote>> = quoteDao.getAllQuotes().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val appSettings: StateFlow<SettingsRepository.AppSettings> = settingsRepo.appSettingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsRepository.AppSettings(9, 0, "Quote of the Day", false, false, 0xFF6200EE, false)
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

    fun updateNotificationTime(hour: Int, minute: Int) {
        viewModelScope.launch {
            settingsRepo.saveNotificationTime(hour, minute)
            alarmScheduler.scheduleAlarm(hour, minute)
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
