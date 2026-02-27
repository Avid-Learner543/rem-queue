package com.avidlearner.rem.ui.settings

import android.app.TimePickerDialog
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.avidlearner.rem.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val appSettings by viewModel.appSettings.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Notification Time Settings
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notification Time", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val timeString = String.format("%02d:%02d", appSettings.hour, appSettings.minute)
                    var showTimePicker by remember { mutableStateOf(false) }
                    
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Time: $timeString")
                    }

                    if (showTimePicker) {
                        TimePickerDialog(
                            context,
                            { _, hourOfDay, minute ->
                                viewModel.updateNotificationTime(hourOfDay, minute)
                                showTimePicker = false
                            },
                            appSettings.hour,
                            appSettings.minute,
                            true // 24 hour view
                        ).apply {
                            setOnDismissListener { showTimePicker = false }
                            show()
                        }
                    }
                }
            }

            // Notification Title Settings
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notification Title", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = appSettings.title,
                        onValueChange = { viewModel.updateNotificationTitle(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Appearance and Behavior
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Appearance & Behavior", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Persistent Notification")
                        Switch(
                            checked = appSettings.isPersistent,
                            onCheckedChange = { viewModel.updateIsPersistent(it) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode")
                        Switch(
                            checked = appSettings.isDarkMode,
                            onCheckedChange = { viewModel.updateIsDarkMode(it) }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Adaptive Theme")
                            Text(
                                text = "Use Android wallpaper colors",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = appSettings.useAdaptiveTheme,
                            onCheckedChange = { viewModel.updateUseAdaptiveTheme(it) },
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        )
                    }
                }
            }

            // Notification Color Settings
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Notification Theme Color", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val colors = listOf(
                        0xFF6200EE, // Purple
                        0xFF03DAC5, // Teal
                        0xFFB00020, // Red
                        0xFF3700B3, // Dark Purple
                        0xFF00C853, // Green
                        0xFFFFAB00  // Amber
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        colors.forEach { colorVal ->
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(colorVal))
                                    .border(
                                        width = if (appSettings.color == colorVal) 3.dp else 0.dp,
                                        color = if (appSettings.color == colorVal) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable { viewModel.updateNotificationColor(colorVal) }
                            )
                        }
                    }
                }
            }
        }
    }
}
