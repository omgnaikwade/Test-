package com.quest.cbt.ui.screens.result

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.data.repository.AttemptRepository
import com.quest.cbt.data.repository.TestRepository
import com.quest.cbt.ui.components.KatexMathView
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.CardSurfaceElevated
import com.quest.cbt.ui.theme.ChemistryColor
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.MathsColor
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.PhysicsColor
import com.quest.cbt.ui.theme.TextMuted
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary

@Composable
fun ResultAnalysisScreen(
    attemptId: String,
    attemptRepository: AttemptRepository,
    testRepository: TestRepository,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ResultSummaryViewModel = viewModel(
        factory = ResultSummaryViewModel.provideFactory(attemptId, attemptRepository, testRepository)
    )
) {
    val uiState by viewModel.uiState.collectAsState()

    // Question filters
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    when (val state = uiState) {
        is ResultSummaryUiState.Loading -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkNavyBg),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PeriwinkleAccent)
            }
        }
        is ResultSummaryUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkNavyBg)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Unable to load test result",
                        color = Color(0xFFEF4444),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.message,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateHome,
                        colors = ButtonDefaults.buttonColors(containerColor = PeriwinkleAccent, contentColor = OnPeriwinkle)
                    ) {
                        Text("Return to Dashboard")
                    }
                }
            }
        }
        is ResultSummaryUiState.Success -> {
            val att = state.attempt
            val table = state.summaryTable
            val questions = state.questions
            val answersMap = state.answersMap
            val baseDir = state.test?.baseDir

            // Filter questions
            val filteredQuestions = questions.filter { q ->
                val qState = answersMap[q.id]
                val matchSubject = selectedSubjectFilter == "All" || q.subject == selectedSubjectFilter
                val matchStatus = when (selectedStatusFilter) {
                    "All" -> true
                    "Correct" -> qState?.isCorrect == true
                    "Incorrect" -> qState != null && !qState.isCorrect && qState.scoreAwarded < 0
                    "Unattempted" -> qState == null || qState.scoreAwarded == 0
                    else -> true
                }
                matchSubject && matchStatus
            }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(DarkNavyBg)
                    .statusBarsPadding()
            ) {
                // Top Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CardSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onNavigateHome) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Score & Performance Analysis",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = att.testTitle,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }

                        IconButton(
                            onClick = onNavigateHome,
                            modifier = Modifier.testTag("result_home_btn")
                        ) {
                            Icon(Icons.Default.Home, contentDescription = "Home", tint = PeriwinkleAccent)
                        }
                    }
                    HorizontalDivider(color = CardBorder, thickness = 1.dp)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Overall Score Card
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "TOTAL SCORE",
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${table.totalScore}",
                                        color = PeriwinkleAccent,
                                        fontSize = 42.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = " / ${table.totalMaxScore}",
                                        color = TextMuted,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(bottom = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Rating Badge
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF243656))
                                        .border(1.dp, PeriwinkleAccent.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 12.dp, vertical = 5.dp)
                                ) {
                                    Text(
                                        text = table.performanceRating,
                                        color = PeriwinkleAccent,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Key Metrics Row
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(CardSurfaceElevated)
                                        .padding(vertical = 12.dp, horizontal = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    MetricItem(label = "Correct", value = "${table.totalCorrect}", color = Color(0xFF22C55E))
                                    MetricItem(label = "Wrong", value = "${table.totalIncorrect}", color = Color(0xFFEF4444))
                                    MetricItem(label = "Skipped", value = "${table.totalUnattempted}", color = TextSecondary)
                                    MetricItem(label = "Total Accuracy", value = "${table.totalAccuracyPercent.toInt()}%", color = PeriwinkleAccent)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Time Taken: ${state.timeTakenFormatted}",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    // NTA-STYLE SUMMARY TABLE
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = CardSurface),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = PeriwinkleAccent,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "NTA Subject Summary Table",
                                        color = TextPrimary,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                NtaSummaryTableComponent(table = table)
                            }
                        }
                    }

                    // Subject Cards Row
                    item {
                        Text(
                            text = "Subject Highlights",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            table.rows.forEach { row ->
                                val color = when (row.subject) {
                                    "Physics" -> PhysicsColor
                                    "Chemistry" -> ChemistryColor
                                    else -> MathsColor
                                }
                                SubjectScoreCard(
                                    subject = row.subject,
                                    score = row.scoreAwarded,
                                    maxScore = row.maxScore,
                                    accuracy = row.accuracyPercent,
                                    accentColor = color,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Question-wise Review Filter Header
                    item {
                        Column {
                            Text(
                                text = "Question-wise Detailed Review (${filteredQuestions.size})",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Filter by Subject
                            val subjectFilters = listOf("All", "Physics", "Chemistry", "Maths")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                subjectFilters.forEach { s ->
                                    val isSel = s == selectedSubjectFilter
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) PeriwinkleAccent else CardSurfaceElevated)
                                            .clickable { selectedSubjectFilter = s }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = s,
                                            color = if (isSel) OnPeriwinkle else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Filter by Status
                            val statusFilters = listOf("All", "Correct", "Incorrect", "Unattempted")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                statusFilters.forEach { st ->
                                    val isSel = st == selectedStatusFilter
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(if (isSel) PeriwinkleAccent else CardSurfaceElevated)
                                            .clickable { selectedStatusFilter = st }
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = st,
                                            color = if (isSel) OnPeriwinkle else TextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Questions List
                    items(filteredQuestions, key = { it.id }) { q ->
                        val qState = answersMap[q.id]
                        QuestionReviewCard(
                            question = q,
                            qState = qState,
                            baseDir = baseDir
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = onNavigateHome,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PeriwinkleAccent, contentColor = OnPeriwinkle),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Return to Dashboard", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NtaSummaryTableComponent(
    table: NtaSummaryTable,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(DarkNavyBg)
            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
            .horizontalScroll(scrollState)
            .padding(8.dp)
    ) {
        Column {
            // Header Row
            Row(
                modifier = Modifier
                    .background(CardSurfaceElevated)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryHeaderCell("Subject", 90.dp)
                SummaryHeaderCell("Questions", 70.dp)
                SummaryHeaderCell("Correct", 60.dp)
                SummaryHeaderCell("Incorrect", 65.dp)
                SummaryHeaderCell("Unatt.", 55.dp)
                SummaryHeaderCell("Accuracy", 65.dp)
                SummaryHeaderCell("Score", 65.dp)
            }

            // Subject Rows
            table.rows.forEach { row ->
                val subjectColor = when (row.subject) {
                    "Physics" -> PhysicsColor
                    "Chemistry" -> ChemistryColor
                    else -> MathsColor
                }

                Row(
                    modifier = Modifier
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SummaryDataCell(row.subject, 90.dp, color = subjectColor, isBold = true)
                    SummaryDataCell(row.totalQuestions.toString(), 70.dp)
                    SummaryDataCell(row.correctCount.toString(), 60.dp, color = Color(0xFF22C55E), isBold = true)
                    SummaryDataCell(row.incorrectCount.toString(), 65.dp, color = Color(0xFFEF4444))
                    SummaryDataCell(row.unattemptedCount.toString(), 55.dp, color = TextMuted)
                    SummaryDataCell("${row.accuracyPercent.toInt()}%", 65.dp, color = PeriwinkleAccent)
                    SummaryDataCell("${row.scoreAwarded}/${row.maxScore}", 65.dp, isBold = true)
                }
                HorizontalDivider(color = CardBorder.copy(alpha = 0.4f), thickness = 0.5.dp)
            }

            // Total / Summary Footer Row
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF243656).copy(alpha = 0.6f))
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryDataCell("Total", 90.dp, color = PeriwinkleAccent, isBold = true)
                SummaryDataCell(table.totalQuestions.toString(), 70.dp, isBold = true)
                SummaryDataCell(table.totalCorrect.toString(), 60.dp, color = Color(0xFF22C55E), isBold = true)
                SummaryDataCell(table.totalIncorrect.toString(), 65.dp, color = Color(0xFFEF4444), isBold = true)
                SummaryDataCell(table.totalUnattempted.toString(), 55.dp, color = TextMuted, isBold = true)
                SummaryDataCell("${table.totalAccuracyPercent.toInt()}%", 65.dp, color = PeriwinkleAccent, isBold = true)
                SummaryDataCell("${table.totalScore}/${table.totalMaxScore}", 65.dp, color = Color.White, isBold = true)
            }
        }
    }
}

@Composable
private fun SummaryHeaderCell(text: String, width: Dp) {
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
private fun SummaryDataCell(
    text: String,
    width: Dp,
    color: Color = TextPrimary,
    isBold: Boolean = false
) {
    Text(
        text = text,
        color = color,
        fontSize = 12.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun MetricItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text(text = label, color = TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun SubjectScoreCard(
    subject: String,
    score: Int,
    maxScore: Int,
    accuracy: Float,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = subject, color = accentColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "$score", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "/ $maxScore", color = TextMuted, fontSize = 11.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "${accuracy.toInt()}% Acc", color = PeriwinkleAccent, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun QuestionReviewCard(
    question: QuestionJson,
    qState: QuestionAttemptState?,
    baseDir: String?,
    modifier: Modifier = Modifier
) {
    val isBonus = question.answer.trim().equals("BONUS", ignoreCase = true)
    val isCorrect = qState?.isCorrect == true || isBonus
    val isWrong = qState != null && !isCorrect && qState.scoreAwarded < 0
    val isUnattempted = !isCorrect && !isWrong

    val statusBadgeColor = when {
        isBonus -> Color(0xFFFBBF24)
        isCorrect -> Color(0xFF22C55E)
        isWrong -> Color(0xFFEF4444)
        else -> Color(0xFF6B7280)
    }

    val statusBadgeText = when {
        isBonus -> "BONUS (+4)"
        isCorrect -> "CORRECT (+${qState?.scoreAwarded ?: 4})"
        isWrong -> "INCORRECT (${qState?.scoreAwarded ?: -1})"
        else -> "UNATTEMPTED (0)"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Question No, Subject, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Q${question.id} • ${question.subject} (Sec ${question.section})",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBadgeColor.copy(alpha = 0.15f))
                        .border(1.dp, statusBadgeColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusBadgeText,
                        color = statusBadgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Question statement with KaTeX
            KatexMathView(
                text = question.text,
                baseDir = baseDir,
                images = question.images,
                fontSizeSp = 14
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Answer Comparison Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurfaceElevated)
                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    val userAns = if (question.type == "MCQ") {
                        qState?.selectedOption ?: "Not Answered"
                    } else {
                        qState?.numericAnswer ?: "Not Answered"
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Your Answer: $userAns",
                            color = if (isCorrect) Color(0xFF22C55E) else if (isWrong) Color(0xFFEF4444) else TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Correct: ${question.answer}",
                            color = Color(0xFF22C55E),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (qState != null && qState.timeSpentSeconds > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val qMin = qState.timeSpentSeconds / 60
                        val qSec = qState.timeSpentSeconds % 60
                        Text(
                            text = "Time Spent: ${qMin}m ${qSec}s",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Solution section if available
            question.solution?.let { sol ->
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = Color(0xFFFBBF24),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Detailed Solution:",
                        color = Color(0xFFFBBF24),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                KatexMathView(
                    text = sol,
                    baseDir = baseDir,
                    images = question.solutionImages,
                    fontSizeSp = 13
                )
            }
        }
    }
}
