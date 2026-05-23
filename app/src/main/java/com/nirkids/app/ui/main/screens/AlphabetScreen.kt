package com.nirkids.app.ui.main.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import com.nirkids.app.domain.model.Alphabet
import com.nirkids.app.ui.main.viewmodel.AlphabetViewModel
import com.nirkids.app.ui.theme.*
import com.nirkids.tracevalidator.TraceValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlphabetScreen(
    onNavigateBack: () -> Unit,
    viewModel: AlphabetViewModel = hiltViewModel(),
    ttsHelper: com.nirkids.app.ui.utils.TtsHelper,
    vibrationHelper: com.nirkids.app.ui.utils.VibrationHelper
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLetterDetails by remember { mutableStateOf(false) }
    var selectedLetter by remember { mutableStateOf<Alphabet?>(null) }
    val scope = rememberCoroutineScope()

    // Color mapping for letters
    val letterColors = listOf(LetterRed, LetterOrange, LetterYellow, LetterGreen, LetterCyan, LetterBlue, LetterPurple)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔤 Alphabet Cards", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading && uiState.allLetters.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.allLetters.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No letters found 😢", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { /* Refresh could be triggered here */ }) {
                        Text("Refresh Data")
                    }
                }
            } else {
                val currentLetter = selectedLetter
                if (showLetterDetails && currentLetter != null) {
                    LetterDetailCard(
                        alphabet = currentLetter,
                        progress = uiState.currentProgress,
                        isVowel = currentLetter.isVowel,
                        onPlay = {
                            ttsHelper.speak(currentLetter.letter.toString())
                            viewModel.playPronunciation()
                            TraceValidator.logEvent("letter_pronounced", mapOf("letter" to currentLetter.letter.toString()))
                        },
                        onMarkLearned = { 
                            scope.launch {
                                viewModel.markAsLearned(currentLetter.letter)
                            }
                        },
                        onDismiss = { 
                            showLetterDetails = false
                            selectedLetter = null 
                        }
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            "Tap a letter to learn! 🔤",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(12.dp)
                        ) {
                            items(uiState.allLetters) { alphabet ->
                                val colorIndex = alphabet.letter.code - 65
                                val color = letterColors[colorIndex % letterColors.size]

                                LetterGridCard(
                                    alphabet = alphabet,
                                    color = color,
                                    progress = uiState.currentProgress,
                                    onClick = {
                                        selectedLetter = alphabet
                                        showLetterDetails = true
                                        vibrationHelper.vibrate(30)
                                        ttsHelper.speak(alphabet.letter.toString())
                                        TraceValidator.logEvent("letter_tapped", mapOf("letter" to alphabet.letter.toString()))
                                    },
                                    isVowel = alphabet.isVowel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LetterGridCard(
    alphabet: Alphabet,
    color: androidx.compose.ui.graphics.Color,
    progress: com.nirkids.app.domain.model.LetterProgress?,
    onClick: () -> Unit,
    isVowel: Boolean
) {
    val bgColor = if (isVowel) {
        VowelColor.copy(alpha = 0.25f)
    } else {
        ConsonantColor.copy(alpha = 0.15f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .clickable(onClick = onClick)
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                alphabet.letter.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                alphabet.letter.lowercase(),
                fontSize = 20.sp,
                color = color.copy(alpha = 0.7f)
            )
            Text(
                alphabet.imageUrl,
                fontSize = 24.sp
            )
            // Progress indicator
            if (progress != null && progress.letter == alphabet.letter) {
                if (progress.learned) {
                    Text("✅", fontSize = 16.sp)
                } else if (progress.masteryLevel > 0) {
                    Text("🔤", fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun LetterDetailCard(
    alphabet: Alphabet,
    progress: com.nirkids.app.domain.model.LetterProgress?,
    isVowel: Boolean,
    onPlay: () -> Unit,
    onMarkLearned: () -> Unit,
    onDismiss: () -> Unit
) {
    val containerColor = if (isVowel) VowelColor.copy(alpha = 0.2f) else ConsonantColor.copy(alpha = 0.15f)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Close button
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurface)
                }
            }

            // Letter display
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    alphabet.letter.toString(),
                    fontSize = 120.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isVowel) VowelColor else ConsonantColor
                )
                Text(
                    "${alphabet.letter.lowercase()}",
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            // Phonetic info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Pronunciation",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Text(
                    alphabet.phonetic,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "as in ${alphabet.exampleWord}",
                    fontSize = 28.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(alphabet.imageUrl, fontSize = 64.sp)
            }

            // Progress info
            if (progress != null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        progress.statusText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    if (progress.attempts > 0) {
                        Text(
                            "${progress.attempts} attempts",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onPlay,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, if (isVowel) VowelColor else ConsonantColor)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, tint = if (isVowel) VowelColor else ConsonantColor)
                        Text("Play", color = if (isVowel) VowelColor else ConsonantColor)
                    }
                }

                Button(
                    onClick = onMarkLearned,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (progress?.learned == true) MasteryGreen else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("${if (progress?.learned == true) "✅ Learned" else "✅ Mark Learned"}")
                }
            }
        }
    }
}
