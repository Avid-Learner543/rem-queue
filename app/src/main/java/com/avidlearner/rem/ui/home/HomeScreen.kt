package com.avidlearner.rem.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.avidlearner.rem.data.room.Quote
import com.avidlearner.rem.data.room.Timer
import com.avidlearner.rem.ui.MainViewModel
import com.avidlearner.rem.ui.theme.PressStart2P
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToSettings: () -> Unit
) {
    val quotes by viewModel.allQuotes.collectAsState()
    val timers by viewModel.allTimers.collectAsState()
    
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Reminders", "Timers")

    var showAddQuoteDialog by remember { mutableStateOf(false) }
    var quoteToEdit by remember { mutableStateOf<Quote?>(null) }
    
    var showTimePicker by remember { mutableStateOf(false) }
    var timerToEdit by remember { mutableStateOf<Timer?>(null) }
    
    val context = LocalContext.current
    
    // Permission launch handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Note: SCHEDULE_EXACT_ALARM doesn't always show a prompt using this API,
            // usually requires redirecting user to settings. But we'll try requesting standard permissions first.
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED) {
                // SCHEDULE_EXACT_ALARM is not runtime-granted via ActivityResultContracts usually.
                // We'll trust the AlarmManager check in Scheduler.
            }
        }
        if (permissions.isNotEmpty()) {
            permissionLauncher.launch(permissions.toTypedArray())
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rem - Queue", fontFamily = PressStart2P) },
                actions = {
                    if (selectedTab == 0) {
                        IconButton(onClick = { viewModel.shuffleQuotes() }) {
                            Icon(Icons.Default.Shuffle, contentDescription = "Shuffle")
                        }
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { 
                if (selectedTab == 0) {
                    showAddQuoteDialog = true 
                } else {
                    showTimePicker = true
                }
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (selectedTab == 0) {
                if (quotes.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No quotes yet. Add one!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quotes, key = { it.id }) { quote ->
                            QuoteItem(
                                quote = quote,
                                onEdit = { quoteToEdit = quote },
                                onDelete = { viewModel.deleteQuote(quote) }
                            )
                        }
                    }
                }
            } else {
                if (timers.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No timers yet. Add one!", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(timers, key = { it.id }) { timer ->
                            TimerItem(
                                timer = timer,
                                onToggle = { isEnabled -> viewModel.updateTimer(timer.copy(isEnabled = isEnabled)) },
                                onEdit = { timerToEdit = timer },
                                onDelete = { viewModel.deleteTimer(timer) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddQuoteDialog) {
        AddEditQuoteDialog(
            quote = null,
            onDismiss = { showAddQuoteDialog = false },
            onSave = { text ->
                viewModel.addQuote(text)
                showAddQuoteDialog = false
            }
        )
    }

    if (showTimePicker || timerToEdit != null) {
        val initialHour = timerToEdit?.hour ?: 9
        val initialMinute = timerToEdit?.minute ?: 0
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                if (timerToEdit != null) {
                    viewModel.updateTimer(timerToEdit!!.copy(hour = hourOfDay, minute = minute))
                    timerToEdit = null
                } else {
                    viewModel.addTimer(hourOfDay, minute)
                    showTimePicker = false
                }
            },
            initialHour,
            initialMinute,
            true // 24 hour view
        ).apply {
            setOnDismissListener { 
                showTimePicker = false
                timerToEdit = null
            }
            show()
        }
    }

    quoteToEdit?.let { quote ->
        AddEditQuoteDialog(
            quote = quote,
            onDismiss = { quoteToEdit = null },
            onSave = { text ->
                viewModel.updateQuote(quote.copy(text = text))
                quoteToEdit = null
            }
        )
    }
}

@Composable
fun QuoteItem(quote: Quote, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = quote.text,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddEditQuoteDialog(
    quote: Quote?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var text by remember { mutableStateOf(quote?.text ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (quote == null) "Add Quote" else "Edit Quote") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Quote Text") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    if (text.isNotBlank()) onSave(text.trim())
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun TimerItem(timer: Timer, onToggle: (Boolean) -> Unit, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onEdit),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val timeString = String.format("%02d:%02d", timer.hour, timer.minute)
            Text(
                text = timeString,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium
            )
            Switch(
                checked = timer.isEnabled,
                onCheckedChange = onToggle
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
