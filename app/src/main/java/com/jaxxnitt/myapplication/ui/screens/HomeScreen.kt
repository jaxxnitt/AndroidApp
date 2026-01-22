package com.jaxxnitt.myapplication.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jaxxnitt.myapplication.ui.viewmodel.CheckInStatus
import com.jaxxnitt.myapplication.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToContacts: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val checkInSuccess by viewModel.checkInSuccess.collectAsState()

    LaunchedEffect(checkInSuccess) {
        if (checkInSuccess) {
            viewModel.resetCheckInSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Are You Dead?") },
                actions = {
                    IconButton(onClick = onNavigateToContacts) {
                        Icon(Icons.Default.People, contentDescription = "Contacts")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Status section
            StatusCard(
                checkInStatus = uiState.checkInStatus,
                lastCheckIn = uiState.lastCheckIn?.let { viewModel.formatLastCheckIn(it.timestamp) },
                nextCheckInDue = uiState.nextCheckInDue,
                frequencyDays = uiState.settings.checkInFrequencyDays
            )

            // Check-in button
            CheckInButton(
                isCheckedIn = uiState.checkInStatus == CheckInStatus.CHECKED_IN,
                onCheckIn = { viewModel.checkIn() }
            )

            // Info text
            if (!uiState.settings.isEnabled) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = "Check-in monitoring is disabled",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            } else {
                FrequencyInfo(frequencyDays = uiState.settings.checkInFrequencyDays)
            }
        }
    }
}

@Composable
private fun StatusCard(
    checkInStatus: CheckInStatus,
    lastCheckIn: String?,
    nextCheckInDue: String,
    frequencyDays: Int
) {
    val (statusText, statusColor, statusIcon) = when (checkInStatus) {
        CheckInStatus.CHECKED_IN -> Triple(
            "You're all checked in!",
            Color(0xFF4CAF50),
            Icons.Default.CheckCircle
        )
        CheckInStatus.PENDING -> Triple(
            "Time to check in",
            MaterialTheme.colorScheme.primary,
            null
        )
        CheckInStatus.OVERDUE -> Triple(
            "Check-in overdue!",
            MaterialTheme.colorScheme.error,
            Icons.Default.Warning
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = statusColor.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (statusIcon != null) {
                Icon(
                    imageVector = statusIcon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Text(
                text = statusText,
                style = MaterialTheme.typography.headlineSmall,
                color = statusColor,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (lastCheckIn != null) {
                Text(
                    text = "Last check-in:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = lastCheckIn,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (checkInStatus != CheckInStatus.CHECKED_IN) {
                Text(
                    text = "Next check-in by:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = nextCheckInDue,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CheckInButton(
    isCheckedIn: Boolean,
    onCheckIn: () -> Unit
) {
    val buttonColor by animateColorAsState(
        targetValue = if (isCheckedIn) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
        label = "buttonColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isCheckedIn) 0.95f else 1f,
        label = "scale"
    )

    Button(
        onClick = onCheckIn,
        modifier = Modifier
            .size(200.dp)
            .scale(scale),
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isCheckedIn) "ALIVE!" else "I'M",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            if (!isCheckedIn) {
                Text(
                    text = "ALIVE",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun FrequencyInfo(frequencyDays: Int) {
    val frequencyText = when (frequencyDays) {
        1 -> "Check-in required daily"
        2 -> "Check-in required every 2 days"
        3 -> "Check-in required every 3 days"
        else -> "Check-in required every $frequencyDays days"
    }

    Text(
        text = frequencyText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )
}
