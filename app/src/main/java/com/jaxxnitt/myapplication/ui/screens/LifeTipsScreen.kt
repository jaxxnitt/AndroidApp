package com.jaxxnitt.myapplication.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SportsHandball
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jaxxnitt.myapplication.ui.theme.Primary
import com.jaxxnitt.myapplication.ui.theme.PrimaryLight
import com.jaxxnitt.myapplication.ui.theme.Secondary
import com.jaxxnitt.myapplication.ui.theme.SecondaryLight
import com.jaxxnitt.myapplication.ui.theme.Success
import com.jaxxnitt.myapplication.ui.theme.SuccessLight
import com.jaxxnitt.myapplication.ui.theme.Tertiary
import com.jaxxnitt.myapplication.ui.theme.TertiaryLight
import com.jaxxnitt.myapplication.ui.theme.Warning
import com.jaxxnitt.myapplication.ui.theme.WarningLight
import com.jaxxnitt.myapplication.ui.theme.CelebrationPurple

private data class TipCategory(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val bgColor: Color,
    val tips: List<String>
)

private val tipCategories = listOf(
    TipCategory(
        title = "Stay Active",
        icon = Icons.Default.SportsHandball,
        color = Primary,
        bgColor = PrimaryLight,
        tips = listOf(
            "Walk at least 30 minutes a day — your future self will thank you",
            "Take the stairs instead of the elevator, your legs are not decorative",
            "Stretch every morning — you're not a statue",
            "Dance like nobody's watching (they probably aren't)"
        )
    ),
    TipCategory(
        title = "Eat Well",
        icon = Icons.Default.LocalDining,
        color = Success,
        bgColor = SuccessLight,
        tips = listOf(
            "Drink water like it's your job — 8 glasses a day minimum",
            "Eat more vegetables, they didn't do anything to deserve being ignored",
            "Cut down on processed food — if it has 47 ingredients, maybe skip it",
            "Cook at home more — your wallet and body will both be happier"
        )
    ),
    TipCategory(
        title = "Stay Connected",
        icon = Icons.Default.People,
        color = Tertiary,
        bgColor = TertiaryLight,
        tips = listOf(
            "Call someone you love today, not just when you need something",
            "Hug people more — science says it releases happy chemicals",
            "Join a club, group, or community — humans aren't meant to be solo players",
            "Tell your friends you appreciate them (yes, out loud)"
        )
    ),
    TipCategory(
        title = "Mind & Soul",
        icon = Icons.Default.SelfImprovement,
        color = CelebrationPurple,
        bgColor = Color(0xFFE8D5F5),
        tips = listOf(
            "Sleep 7-8 hours — revenge bedtime procrastination isn't worth it",
            "Try meditation — even 5 minutes of sitting still counts",
            "Write down 3 things you're grateful for every day",
            "Take breaks from your phone — the internet will survive without you"
        )
    ),
    TipCategory(
        title = "Safety First",
        icon = Icons.Default.Shield,
        color = Warning,
        bgColor = WarningLight,
        tips = listOf(
            "Wear your seatbelt — every single time, no exceptions",
            "Get regular health checkups — don't wait for something to hurt",
            "Keep emergency contacts updated (hey, that's what this app is for!)",
            "Learn basic first aid — you might save someone's life, maybe even yours"
        )
    ),
    TipCategory(
        title = "Have Fun",
        icon = Icons.Default.Face,
        color = Secondary,
        bgColor = SecondaryLight,
        tips = listOf(
            "Laugh every day — watch something funny, life's too short to be serious",
            "Pick up a hobby that has nothing to do with your job",
            "Travel somewhere new, even if it's just the next town over",
            "Pet a dog or cat — instant serotonin boost, guaranteed"
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LifeTipsScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Stay Alive Longer",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Tips to keep you happy, healthy, and checking in for a long time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(tipCategories) { category ->
                TipCategoryCard(category)
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun TipCategoryCard(category: TipCategory) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(category.bgColor.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = category.color
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            category.tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = category.color.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(18.dp)
                            .padding(top = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tip,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
