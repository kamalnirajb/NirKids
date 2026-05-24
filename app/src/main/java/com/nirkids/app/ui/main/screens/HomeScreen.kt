package com.nirkids.app.ui.main.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nirkids.app.ui.main.viewmodel.HomeViewModel
import com.nirkids.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToAlphabet: () -> Unit,
    onNavigateToPronunciation: () -> Unit,
    onNavigateToParentGate: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeScreenContent(
        uiState = uiState,
        onNavigateToAlphabet = onNavigateToAlphabet,
        onNavigateToPronunciation = onNavigateToPronunciation,
        onNavigateToParentGate = onNavigateToParentGate
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: com.nirkids.app.ui.main.viewmodel.HomeUiState,
    onNavigateToAlphabet: () -> Unit,
    onNavigateToPronunciation: () -> Unit,
    onNavigateToParentGate: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📚 NirKids",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Welcome section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🌈 Welcome to NirKids!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Learn your ABCs the fun way! Press any letter to start learning. 🎯",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }

            // Stats cards
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "Learned ✅",
                        value = uiState.learnedCount,
                        color = MasteryGreen,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Practicing 🔤",
                        value = uiState.inProgressCount,
                        color = MasteryOrange,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        label = "New 🆕",
                        value = uiState.newCount,
                        color = MasteryGray,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        label = "Total 📊",
                        value = uiState.totalLetters,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Progress bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Your Progress",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val progress = if (uiState.totalLetters > 0) uiState.learnedCount.toFloat() / uiState.totalLetters else 0f
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp),
                        color = MasteryGreen,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${(progress * 100).toInt()}% complete",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }

            // Alphabet grid preview (first 6 letters)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Pick a letter to learn!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        val colors = listOf(LetterRed, LetterOrange, LetterYellow, LetterGreen, LetterCyan, LetterBlue)
                        if (uiState.allLetters.isNotEmpty()) {
                            uiState.allLetters.take(6).forEachIndexed { index, alphabet ->
                                LetterMiniCard(
                                    letter = alphabet.letter,
                                    color = colors[index % colors.size],
                                    onClick = { onNavigateToAlphabet() }
                                )
                            }
                        } else {
                            (65..70).map { it.toChar() }.forEachIndexed { index, letter ->
                                LetterMiniCard(
                                    letter = letter,
                                    color = colors[index % colors.size],
                                    onClick = { onNavigateToAlphabet() }
                                )
                            }
                        }
                        if (uiState.totalLetters > 6) {
                            Text(
                                "+${uiState.totalLetters - 6}",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }

            // Action buttons
            Text(
                "What would you like to do?",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            actionButton(
                icon = Icons.Default.Book,
                label = "🔤 Learn Alphabets",
                description = "Explore all 26 letters with fun cards",
                color = LetterBlue,
                onClick = onNavigateToAlphabet
            )

            actionButton(
                icon = Icons.Default.Mic,
                label = "🗣️ Pronunciation",
                description = "Practice speaking with TTS feedback",
                color = LetterGreen,
                onClick = onNavigateToPronunciation
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Parent Gate link (small)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .clickable { onNavigateToParentGate() }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔐", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Parent Settings", fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.weight(1f))
                Text("→", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(label: String, value: Int, color: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                value.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun LetterMiniCard(letter: Char, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .background(color.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .border(BorderStroke(2.dp, color.copy(alpha = 0.5f)), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            letter.toString(),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun actionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    color: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(description, fontSize = 14.sp, color = color.copy(alpha = 0.7f))
            }
        }
    }
}
