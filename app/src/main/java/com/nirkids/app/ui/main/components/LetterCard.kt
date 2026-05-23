package com.nirkids.app.ui.main.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nirkids.app.ui.theme.VowelColor
import com.nirkids.app.ui.theme.ConsonantColor

@Composable
fun LetterCard(
    letter: Char,
    phonetic: String,
    exampleWord: String,
    isVowel: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isVowel) {
        VowelColor.copy(alpha = 0.2f)
    } else {
        ConsonantColor.copy(alpha = 0.15f)
    }
    val borderColor = if (isVowel) VowelColor else ConsonantColor

    Card(
        modifier = Modifier
            .size(120.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .background(containerColor, RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                letter.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = borderColor
            )
            Text(
                "/$phonetic/",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                exampleWord,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}
