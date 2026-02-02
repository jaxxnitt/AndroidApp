package com.jaxxnitt.myapplication.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jaxxnitt.myapplication.ui.components.TimePickerDialog
import com.jaxxnitt.myapplication.ui.viewmodel.AuthViewModel
import com.jaxxnitt.myapplication.ui.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onSignOut: () -> Unit,
    viewModel: SettingsViewModel = viewModel(),
    authViewModel: AuthViewModel = viewModel()
) {
    val settings by viewModel.settings.collectAsState()
    var showTimePicker by remember { mutableStateOf(false) }
    var showGracePeriodDropdown by remember { mutableStateOf(false) }
    var showFrequencyDropdown by remember { mutableStateOf(false) }
    var showMessagingMethodDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Enable/Disable Switch
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Check-in Monitoring",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = if (settings.isEnabled) "Active" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.isEnabled,
                        onCheckedChange = { viewModel.updateEnabled(it) }
                    )
                }
            }

            // User Name
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "  Your Name",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = settings.userName,
                        onValueChange = { viewModel.updateUserName(it) },
                        label = { Text("Name shown in alerts") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // Check-in Time
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Check-in Time",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Daily reminder at this time",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Text(
                        text = formatTime(settings.checkInHour, settings.checkInMinute),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Check-in Frequency
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFrequencyDropdown = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Check-in Frequency",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "How often you need to check in",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column {
                        Text(
                            text = getFrequencyText(settings.checkInFrequencyDays),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DropdownMenu(
                            expanded = showFrequencyDropdown,
                            onDismissRequest = { showFrequencyDropdown = false }
                        ) {
                            listOf(1, 2, 3).forEach { days ->
                                DropdownMenuItem(
                                    text = { Text(getFrequencyText(days)) },
                                    onClick = {
                                        viewModel.updateCheckInFrequency(days)
                                        showFrequencyDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Grace Period
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showGracePeriodDropdown = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Timer,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Grace Period",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Time after check-in time before alert",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column {
                        Text(
                            text = "${settings.gracePeriodHours} hours",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DropdownMenu(
                            expanded = showGracePeriodDropdown,
                            onDismissRequest = { showGracePeriodDropdown = false }
                        ) {
                            listOf(1, 2, 3, 4, 6, 8, 12).forEach { hours ->
                                DropdownMenuItem(
                                    text = { Text("$hours hours") },
                                    onClick = {
                                        viewModel.updateGracePeriod(hours)
                                        showGracePeriodDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Messaging Method
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showMessagingMethodDropdown = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Message,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(
                                text = "Alert Method",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "How to notify emergency contacts",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Column {
                        Text(
                            text = getMessagingMethodText(settings.messagingMethod),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        DropdownMenu(
                            expanded = showMessagingMethodDropdown,
                            onDismissRequest = { showMessagingMethodDropdown = false }
                        ) {
                            listOf("sms", "whatsapp", "both").forEach { method ->
                                DropdownMenuItem(
                                    text = { Text(getMessagingMethodText(method)) },
                                    onClick = {
                                        viewModel.updateMessagingMethod(method)
                                        showMessagingMethodDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "How it works",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = buildString {
                            append("1. You'll receive a reminder at ${formatTime(settings.checkInHour, settings.checkInMinute)}\n")
                            append("2. Check in ")
                            append(when (settings.checkInFrequencyDays) {
                                1 -> "daily"
                                2 -> "every 2 days"
                                3 -> "every 3 days"
                                else -> "every ${settings.checkInFrequencyDays} days"
                            })
                            append(" by pressing \"I'm Alive\"\n")
                            val methodText = when (settings.messagingMethod) {
                                "sms" -> "SMS"
                                "whatsapp" -> "WhatsApp"
                                else -> "SMS & WhatsApp"
                            }
                            append("3. If you don't check in within ${settings.gracePeriodHours} hours, your emergency contacts will be notified via $methodText and Email")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sign Out Button
            Button(
                onClick = {
                    authViewModel.signOut()
                    onSignOut()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Icon(
                    Icons.Default.Logout,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Sign Out")
            }
        }

        // Time Picker Dialog
        if (showTimePicker) {
            TimePickerDialog(
                initialHour = settings.checkInHour,
                initialMinute = settings.checkInMinute,
                onConfirm = { hour, minute ->
                    viewModel.updateCheckInTime(hour, minute)
                    showTimePicker = false
                },
                onDismiss = { showTimePicker = false }
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val format = SimpleDateFormat("h:mm a", Locale.getDefault())
    return format.format(calendar.time)
}

private fun getFrequencyText(days: Int): String {
    return when (days) {
        1 -> "Every day"
        2 -> "Every 2 days"
        3 -> "Every 3 days"
        else -> "Every $days days"
    }
}

private fun getMessagingMethodText(method: String): String {
    return when (method) {
        "sms" -> "SMS Only"
        "whatsapp" -> "WhatsApp Only"
        "both" -> "SMS & WhatsApp"
        else -> "SMS & WhatsApp"
    }
}
