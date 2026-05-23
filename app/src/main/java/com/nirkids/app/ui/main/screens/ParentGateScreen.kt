package com.nirkids.app.ui.main.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.nirkids.app.ui.main.viewmodel.ParentGateViewModel
import com.nirkids.app.ui.theme.*
import com.nirkids.tracevalidator.TraceValidator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentGateScreen(
    onDismiss: () -> Unit,
    onGateSuccess: () -> Unit,
    viewModel: ParentGateViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPinEntry by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔐 Parent Gate", fontSize = 22.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (!uiState.isSuccess) onDismiss()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LetterPurple,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            if (uiState.isSuccess) {
                // Success screen
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MasteryGreen.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🎉", fontSize = 72.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Verified!", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = MasteryGreen)
                        Text("You may proceed to parent settings", fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        var showChangePin by remember { mutableStateOf(false) }
                        var newPinInput by remember { mutableStateOf("") }
                        
                        if (showChangePin) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                OutlinedTextField(
                                    value = newPinInput,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                                    label = { Text("Enter New 4-Digit PIN") },
                                    modifier = Modifier.width(200.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        viewModel.setParentPin(newPinInput)
                                        showChangePin = false
                                        newPinInput = ""
                                    },
                                    enabled = newPinInput.length == 4
                                ) {
                                    Text("Save PIN")
                                }
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onGateSuccess,
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MasteryGreen)
                                ) {
                                    Text("Enter Settings", fontSize = 18.sp)
                                }
                                OutlinedButton(
                                    onClick = { showChangePin = true },
                                    shape = RoundedCornerShape(16.dp)
                                ) {
                                    Text("Change PIN")
                                }
                            }
                        }
                    }
                }
            } else if (uiState.isLocked) {
                // Locked state
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = LetterOrange.copy(alpha = 0.15f))
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔒", fontSize = 72.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Gate Locked!", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = LetterOrange)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Too many wrong attempts.", fontSize = 16.sp)
                        Text("Try again in 60 seconds.", fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = { viewModel.clearLock() },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Unlock")
                        }
                    }
                }
            } else if (!showPinEntry) {
                // Math puzzle
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Solve this to continue", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            uiState.gateState.displayQuestion,
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        BasicTextField(
                            value = uiState.userInput,
                            onValueChange = { viewModel.onUserInputChanged(it) },
                            textStyle = TextStyle(
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            decorationBox = { innerTextField ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (uiState.userInput.isEmpty()) {
                                        Text("Enter your answer", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                    }
                                    innerTextField()
                                }
                            }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.submitAnswer(uiState.userInput)
                                if (uiState.isSuccess) {
                                    TraceValidator.logEvent("parent_gate_verified", emptyMap())
                                }
                            },
                            enabled = uiState.userInput.isNotBlank(),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = LetterPurple)
                        ) {
                            Text("Verify", fontSize = 18.sp)
                        }
                        val error = uiState.errorMessage
                        if (error != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                error,
                                fontSize = 14.sp,
                                color = LetterOrange,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Or enter PIN",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        TextButton(onClick = { showPinEntry = true }) {
                            Text("Use PIN instead")
                        }
                    }
                }

                // Question hint
                Text(
                    "Tip: Think about the numbers!",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            } else {
                // PIN entry
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Enter Parent PIN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            repeat(4) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        if (index < pinInput.length) "●" else "",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        // Number pad
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(9) { i ->
                                TextButton(
                                    onClick = {
                                        if (pinInput.length < 4) {
                                            pinInput += (i + 1).toString()
                                        }
                                    },
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text((i + 1).toString(), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            item {
                                // Empty center
                            }
                            item {
                                TextButton(
                                    onClick = {
                                        if (pinInput.length < 4) pinInput += "0" },
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("0", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            item {
                                TextButton(
                                    onClick = { pinInput = pinInput.dropLast(1) },
                                    modifier = Modifier.size(56.dp),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Backspace, contentDescription = "Backspace", modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                viewModel.verifyPin(pinInput)
                            },
                            enabled = pinInput.length == 4,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Submit PIN")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = {
                            showPinEntry = false
                            pinInput = ""
                        }) {
                            Text("Back to math puzzle")
                        }
                    }
                }
            }
        }
    }
}
