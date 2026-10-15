package com.quest.cbt.ui.screens.cbt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.CardSurfaceElevated
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary

data class SubjectSummaryRow(
    val subject: String,
    val total: Int,
    val answered: Int,
    val notAnswered: Int,
    val marked: Int,
    val answeredAndMarked: Int,
    val notVisited: Int
)

@Composable
fun SubmitSummaryDialog(
    questions: List<QuestionJson>,
    answers: Map<Int, QuestionAttemptState>,
    onConfirmSubmit: () -> Unit,
    onDismiss: () -> Unit
) {
    val scrollState = rememberScrollState()

    val rows = remember(questions, answers) {
        listOf("Physics", "Chemistry", "Maths").map { subject ->
            val subjQuestions = questions.filter { it.subject == subject }
            var ans = 0
            var notAns = 0
            var marked = 0
            var ansMarked = 0
            var notVisited = 0

            subjQuestions.forEach { q ->
                when (answers[q.id]?.status) {
                    NtaStatus.ANSWERED -> ans++
                    NtaStatus.NOT_ANSWERED -> notAns++
                    NtaStatus.MARKED_FOR_REVIEW -> marked++
                    NtaStatus.ANSWERED_AND_MARKED -> ansMarked++
                    NtaStatus.NOT_VISITED, null -> notVisited++
                }
            }

            SubjectSummaryRow(
                subject = subject,
                total = subjQuestions.size,
                answered = ans,
                notAnswered = notAns,
                marked = marked,
                answeredAndMarked = ansMarked,
                notVisited = notVisited
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AssignmentTurnedIn,
                    contentDescription = null,
                    tint = PeriwinkleAccent,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Exam Summary",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Review your test progress before final submission:",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                // Scrollable Summary Table
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkNavyBg)
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                        .horizontalScroll(scrollState)
                        .padding(8.dp)
                ) {
                    Column {
                        // Header
                        Row(
                            modifier = Modifier
                                .background(CardSurfaceElevated)
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TableHeaderCell("Subject", 85.dp)
                            TableHeaderCell("Total", 45.dp)
                            TableHeaderCell("Ans", 45.dp)
                            TableHeaderCell("Not Ans", 60.dp)
                            TableHeaderCell("Marked", 55.dp)
                            TableHeaderCell("Ans+Mkd", 65.dp)
                            TableHeaderCell("Not Vis", 55.dp)
                        }

                        rows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .padding(vertical = 4.dp, horizontal = 4.dp)
                                    .border(0.5.dp, CardBorder.copy(alpha = 0.5f)),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TableDataCell(row.subject, 85.dp, isBold = true)
                                TableDataCell(row.total.toString(), 45.dp)
                                TableDataCell(row.answered.toString(), 45.dp, Color(0xFF22C55E))
                                TableDataCell(row.notAnswered.toString(), 60.dp, Color(0xFFEF4444))
                                TableDataCell(row.marked.toString(), 55.dp, Color(0xFFA855F7))
                                TableDataCell(row.answeredAndMarked.toString(), 65.dp, Color(0xFFA855F7))
                                TableDataCell(row.notVisited.toString(), 55.dp, Color(0xFF6B7280))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Are you sure you want to submit? No changes can be made after submission.",
                    color = Color(0xFFFBBF24),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 17.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeriwinkleAccent,
                    contentColor = OnPeriwinkle
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_submit_btn")
            ) {
                Text("Yes, Submit", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("Resume Test", color = TextSecondary)
            }
        },
        containerColor = CardSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun TableHeaderCell(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = PeriwinkleAccent,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun TableDataCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    color: Color = TextPrimary,
    isBold: Boolean = false
) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}
