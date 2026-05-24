package com.nirkids.app.ui.main.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nirkids.app.ui.main.viewmodel.*
import com.nirkids.app.ui.theme.*
import com.nirkids.tracevalidator.TraceValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationScreen(
    onNavigateBack: () -> Unit,
    viewModel: PronunciationViewModel = hiltViewModel(),
    ttsHelper: com.nirkids.app.ui.utils.TtsHelper
) {
    val uiState by viewModel.uiState.collectAsState()

    PronunciationScreenContent(
        uiState = uiState,
        onNavigateBack = onNavigateBack,
        onTogglePracticeMode = { viewModel.togglePracticeMode() },
        onLoadNextLetter = { viewModel.loadNextLetter() },
        onCheckPronunciation = { viewModel.checkPronunciation(it) },
        onClearFeedback = { viewModel.clearFeedback() },
        onSpeakLetter = { letter, example ->
            ttsHelper.speak("$letter. $letter is for $example.")
            TraceValidator.logEvent("pronunciation_speak", mapOf("letter" to letter.toString()))
        },
        onSpeakText = { text ->
            ttsHelper.speak(text)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PronunciationScreenContent(
    uiState: com.nirkids.app.ui.main.viewmodel.PronunciationUiState,
    onNavigateBack: () -> Unit,
    onTogglePracticeMode: () -> Unit,
    onLoadNextLetter: () -> Unit,
    onCheckPronunciation: (String) -> Unit,
    onClearFeedback: () -> Unit,
    onSpeakLetter: (Char, String) -> Unit,
    onSpeakText: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🗣️ Pronunciation", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onTogglePracticeMode) {
                        Icon(
                            if (uiState.isPracticeMode) Icons.Default.Done else Icons.Default.Edit,
                            contentDescription = "Toggle Practice"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetterGreen,
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
            if (uiState.isLoading && uiState.currentLetter == null) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.currentLetter == null) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No letters to practice! 😢", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onLoadNextLetter) {
                        Text("Try Again")
                    }
                }
            } else {
                val letter = uiState.currentLetter!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Letter card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(8.dp, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (letter.isVowel)
                                VowelColor.copy(alpha = 0.2f)
                            else
                                ConsonantColor.copy(alpha = 0.15f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                letter.imageUrl,
                                fontSize = 72.sp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                letter.letter.toString(),
                                fontSize = 96.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (letter.isVowel) VowelColor else ConsonantColor
                            )
                            Text(
                                letter.phonetic,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "as in ${letter.exampleWord}",
                                fontSize = 24.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    onSpeakLetter(letter.letter, letter.exampleWord)
                                },
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if (letter.isVowel) VowelColor else ConsonantColor)
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Listen", fontSize = 18.sp)
                            }
                        }
                    }

                    // Phonetic breakdown
                    if (uiState.phoneticBreakdown.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Phonetic Breakdown", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                uiState.phoneticBreakdown.forEachIndexed { index, segment ->
                                    Text(
                                        "Part ${index + 1}: /$segment/",
                                        fontSize = 16.sp,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }

                    // Practice mode input
                    if (uiState.isPracticeMode) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "Type what sound you hear!",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            BasicTextField(
                                value = uiState.userInput,
                                onValueChange = { onCheckPronunciation(it) },
                                textStyle = TextStyle(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                decorationBox = { innerTextField ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (uiState.userInput.isEmpty()) {
                                            Text(
                                                "Type here...",
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                                fontSize = 24.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }

                    // Feedback
                    uiState.feedbackMessage?.let { message ->
                        val feedbackColor = when (uiState.feedbackType) {
                            FeedbackType.SUCCESS -> MasteryGreen
                            FeedbackType.GREAT -> LetterYellow
                            FeedbackType.TRY_AGAIN -> LetterOrange
                            FeedbackType.NONE -> MaterialTheme.colorScheme.onSurface
                        }
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = feedbackColor.copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                message,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = feedbackColor,
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // Next letter button
                    Button(
                        onClick = {
                            onLoadNextLetter()
                            onClearFeedback()
                            onSpeakText("Next letter!")
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.NextPlan, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Next Letter", fontSize = 18.sp)
                    }
                }
            }
        }
    }
}
