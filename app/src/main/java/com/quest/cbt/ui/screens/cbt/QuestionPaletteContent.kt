package com.quest.cbt.ui.screens.cbt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.ui.components.NtaLegendItem
import com.quest.cbt.ui.components.NtaQuestionButton
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary

@Composable
fun QuestionPaletteContent(
    currentSubject: String,
    allQuestions: List<QuestionJson>,
    currentQuestionIndex: Int,
    answers: Map<Int, QuestionAttemptState>,
    onSelectQuestion: (Int) -> Unit,
    onSubmitClick: () -> Unit,
    onQuestionPaperClick: () -> Unit,
    onInstructionsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    // Filter questions of current subject
    val subjectQuestions = remember(allQuestions, currentSubject) {
        allQuestions.filter { it.subject == currentSubject }
    }

    // Counts for current subject
    val (notVisited, notAnswered, answered, marked, ansMarked) = remember(subjectQuestions, answers) {
        var nv = 0
        var na = 0
        var a = 0
        var m = 0
        var am = 0

        subjectQuestions.forEach { q ->
            when (answers[q.id]?.status) {
                NtaStatus.ANSWERED -> a++
                NtaStatus.NOT_ANSWERED -> na++
                NtaStatus.MARKED_FOR_REVIEW -> m++
                NtaStatus.ANSWERED_AND_MARKED -> am++
                NtaStatus.NOT_VISITED, null -> nv++
            }
        }
        Tuple5(nv, na, a, m, am)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(DarkNavyBg)
            .padding(12.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "Question Palette",
            color = TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Palette Status Legend Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        NtaLegendItem(status = NtaStatus.ANSWERED, count = answered, label = "Answered")
                        NtaLegendItem(status = NtaStatus.NOT_ANSWERED, count = notAnswered, label = "Not Answered")
                        NtaLegendItem(status = NtaStatus.NOT_VISITED, count = notVisited, label = "Not Visited")
                    }
                    Column(modifier = Modifier.weight(1.1f)) {
                        NtaLegendItem(status = NtaStatus.MARKED_FOR_REVIEW, count = marked, label = "Marked for Review")
                        NtaLegendItem(status = NtaStatus.ANSWERED_AND_MARKED, count = ansMarked, label = "Ans & Marked")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Subject & Questions Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$currentSubject Questions",
                color = PeriwinkleAccent,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "${subjectQuestions.size} Questions",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Questions Grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(CardSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 40.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(subjectQuestions, key = { it.id }) { q ->
                    val overallIdx = allQuestions.indexOfFirst { it.id == q.id }
                    val qState = answers[q.id]?.status ?: NtaStatus.NOT_VISITED
                    val isCurrent = overallIdx == currentQuestionIndex

                    NtaQuestionButton(
                        questionNumber = q.id,
                        status = qState,
                        isCurrent = isCurrent,
                        onClick = { onSelectQuestion(overallIdx) },
                        size = 38.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Secondary Palette Buttons: Question Paper & Instructions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onQuestionPaperClick,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("palette_question_paper_btn"),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Paper", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }

            OutlinedButton(
                onClick = onInstructionsClick,
                modifier = Modifier
                    .weight(1f)
                    .height(38.dp)
                    .testTag("palette_instructions_btn"),
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary)
            ) {
                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(15.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Instructions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Submit Button
        Button(
            onClick = onSubmitClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("palette_submit_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Submit Test", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class Tuple5<A, B, C, D, E>(
    val a: A, val b: B, val c: C, val d: D, val e: E
)
