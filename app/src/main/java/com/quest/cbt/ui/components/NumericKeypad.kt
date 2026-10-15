package com.quest.cbt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurfaceElevated
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextPrimary

@Composable
fun NumericKeypad(
    currentValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val handleKeyPress: (String) -> Unit = { key ->
        when (key) {
            "CLEAR" -> onValueChange("")
            "BACK" -> {
                if (currentValue.isNotEmpty()) {
                    onValueChange(currentValue.dropLast(1))
                }
            }
            "." -> {
                if (!currentValue.contains(".")) {
                    onValueChange(if (currentValue.isEmpty()) "0." else "$currentValue.")
                }
            }
            "-" -> {
                if (currentValue.isEmpty()) {
                    onValueChange("-")
                } else if (currentValue == "-") {
                    onValueChange("")
                }
            }
            else -> {
                if (currentValue.length < 12) {
                    onValueChange(currentValue + key)
                }
            }
        }
    }

    if (compact) {
        // Compact 2-row layout optimized for mobile landscape
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val row1 = listOf("1", "2", "3", "4", "5", "6")
            val row2 = listOf("7", "8", "9", "0", ".", "-")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row1.forEach { key ->
                    KeyButton(
                        text = key,
                        height = 36.dp,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keypad_btn_$key"),
                        onClick = { handleKeyPress(key) }
                    )
                }
                // Clear button in row 1
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardSurfaceElevated)
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        .clickable { handleKeyPress("CLEAR") }
                        .testTag("keypad_btn_clear"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clear",
                        color = Color(0xFFF87171),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                row2.forEach { key ->
                    KeyButton(
                        text = key,
                        height = 36.dp,
                        fontSize = 15.sp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("keypad_btn_$key"),
                        onClick = { handleKeyPress(key) }
                    )
                }
                // Backspace button in row 2
                Box(
                    modifier = Modifier
                        .weight(1.3f)
                        .height(36.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(CardSurfaceElevated)
                        .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                        .clickable { handleKeyPress("BACK") }
                        .testTag("keypad_btn_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = PeriwinkleAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    } else {
        // Standard 4-row keypad
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val rows = listOf(
                listOf("7", "8", "9"),
                listOf("4", "5", "6"),
                listOf("1", "2", "3"),
                listOf("-", "0", ".")
            )

            rows.forEach { rowKeys ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowKeys.forEach { key ->
                        KeyButton(
                            text = key,
                            height = 46.dp,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("keypad_btn_$key"),
                            onClick = { handleKeyPress(key) }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurfaceElevated)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { handleKeyPress("CLEAR") }
                        .testTag("keypad_btn_clear"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Clear",
                        color = Color(0xFFF87171),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CardSurfaceElevated)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .clickable { handleKeyPress("BACK") }
                        .testTag("keypad_btn_backspace"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                        contentDescription = "Backspace",
                        tint = PeriwinkleAccent
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyButton(
    text: String,
    height: Dp,
    fontSize: androidx.compose.ui.unit.TextUnit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(CardSurfaceElevated)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = TextPrimary,
            fontSize = fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}
