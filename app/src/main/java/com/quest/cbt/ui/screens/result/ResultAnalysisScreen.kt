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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.AttemptEntity
import com.quest.cbt.data.model.QuestionAttemptState
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.data.model.TestEntity
import com.quest.cbt.data.model.TestJson
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
import kotlinx.serialization.json.Json

@Composable
fun ResultAnalysisScreen(
    attemptId: String,
    attemptRepository: AttemptRepository,
    testRepository: TestRepository,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    var attempt by remember { mutableStateOf<AttemptEntity?>(null) }
    var test by remember { mutableStateOf<TestEntity?>(null) }
    var questions by remember { mutableStateOf<List<QuestionJson>>(emptyList()) }
    var answersMap by remember { mutableStateOf<Map<Int, QuestionAttemptState>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }

    // Filters
    var selectedSubjectFilter by remember { mutableStateOf("All") }
    var selectedStatusFilter by remember { mutableStateOf("All") }

    LaunchedEffect(attemptId) {
        val att = attemptRepository.getAttemptById(attemptId)
        if (att != null) {
            attempt = att
            val t = testRepository.getTestByIdSuspend(att.testId)
            test = t
            if (t != null) {
                try {
                    val json = Json { ignoreUnknownKeys = true }
                    val parsed = json.decodeFromString<TestJson>(t.questionsJson)
                    questions = parsed.questions
                    answersMap = json.decodeFromString(att.answersStateJson)
                } catch (_: Exception) {
                }
            }
        }
        isLoading = false
    }

    if (isLoading || attempt == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(DarkNavyBg),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PeriwinkleAccent)
        }
        return
    }

    val att = attempt!!
    val hours = att.durationSeconds / 3600
    val minutes = (att.durationSeconds % 3600) / 60
    val seconds = att.durationSeconds % 60
    val timeTakenFormatted = buildString {
        if (hours > 0) append("${hours}h ")
        append("${minutes}m ${seconds}s")
    }

    // Filter questions
    val filteredQuestions = questions.filter { q ->
        val state = answersMap[q.id]
        val matchSubject = selectedSubjectFilter == "All" || q.subject == selectedSubjectFilter
        val matchStatus = when (selectedStatusFilter) {
            "All" -> true
            "Correct" -> state?.isCorrect == true
            "Incorrect" -> state?.isCorrect == false && (state.scoreAwarded < 0)
            "Unattempted" -> state == null || state.scoreAwarded == 0
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
            androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp)
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
                                text = "${att.totalScore}",
                                color = PeriwinkleAccent,
                                fontSize = 42.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = " / ${att.maxScore}",
                                color = TextMuted,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Key Metrics Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardSurfaceElevated)
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            MetricItem(label = "Correct", value = "${att.correctCount}", color = Color(0xFF22C55E))
                            MetricItem(label = "Wrong", value = "${att.wrongCount}", color = Color(0xFFEF4444))
                            MetricItem(label = "Skipped", value = "${att.skippedCount}", color = TextSecondary)
                            MetricItem(label = "Accuracy", value = "${att.accuracyPercent.toInt()}%", color = PeriwinkleAccent)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Time Taken: $timeTakenFormatted",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Subject-wise Breakdown Card
            item {
                Text(
                    text = "Subject-wise Breakdown",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubjectScoreCard(
                        subject = "Physics",
                        score = att.physicsScore,
                        maxScore = att.physicsMax,
                        accentColor = PhysicsColor,
                        modifier = Modifier.weight(1f)
                    )
                    SubjectScoreCard(
                        subject = "Chemistry",
                        score = att.chemistryScore,
                        maxScore = att.chemistryMax,
                        accentColor = ChemistryColor,
                        modifier = Modifier.weight(1f)
                    )
                    SubjectScoreCard(
                        subject = "Maths",
                        score = att.mathsScore,
                        maxScore = att.mathsMax,
                        accentColor = MathsColor,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Question-wise Review Filter Header
            item {
                Column {
                    Text(
                        text = "Question-wise Detailed Review",
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
                    baseDir = test?.baseDir
                )
            }

            item {
                Spacer(modifier = Modifier.height(30.dp))
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
            Text(text = "$score", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(text = "/ $maxScore", color = TextMuted, fontSize = 11.sp)
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
