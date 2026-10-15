package com.quest.cbt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.ui.theme.NtaAnswered
import com.quest.cbt.ui.theme.NtaMarked
import com.quest.cbt.ui.theme.NtaNotAnswered
import com.quest.cbt.ui.theme.NtaNotVisited
import com.quest.cbt.ui.theme.TextPrimary

@Composable
fun NtaQuestionButton(
    questionNumber: Int,
    status: NtaStatus,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    val shape = when (status) {
        NtaStatus.MARKED_FOR_REVIEW, NtaStatus.ANSWERED_AND_MARKED -> CircleShape
        else -> RoundedCornerShape(6.dp)
    }

    val bgColor = when (status) {
        NtaStatus.NOT_VISITED -> NtaNotVisited
        NtaStatus.NOT_ANSWERED -> NtaNotAnswered
        NtaStatus.ANSWERED -> NtaAnswered
        NtaStatus.MARKED_FOR_REVIEW -> NtaMarked
        NtaStatus.ANSWERED_AND_MARKED -> NtaMarked
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgColor)
            .then(
                if (isCurrent) Modifier.border(2.5.dp, Color.White, shape)
                else Modifier.border(1.dp, Color.Black.copy(alpha = 0.2f), shape)
            )
            .clickable(onClick = onClick)
            .testTag("question_palette_btn_$questionNumber"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = questionNumber.toString(),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        // Green tick badge for Answered & Marked for Review
        if (status == NtaStatus.ANSWERED_AND_MARKED) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(NtaAnswered)
                    .border(1.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}

@Composable
fun NtaLegendItem(
    status: NtaStatus,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        NtaQuestionButton(
            questionNumber = count,
            status = status,
            isCurrent = false,
            onClick = {},
            size = 28.dp
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            color = TextPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
