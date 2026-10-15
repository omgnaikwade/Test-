package com.quest.cbt.ui.screens.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.AttemptEntity
import com.quest.cbt.data.model.TestEntity
import com.quest.cbt.data.repository.AttemptRepository
import com.quest.cbt.data.repository.TestRepository
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.ChemistryColor
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.MathsColor
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.PhysicsColor
import com.quest.cbt.ui.theme.TextMuted
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary
import com.quest.cbt.util.SampleTestData
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    testRepository: TestRepository,
    attemptRepository: AttemptRepository,
    onStartTest: (String) -> Unit,
    onViewAnalysis: (String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tests by testRepository.allTests.collectAsState(initial = emptyList())
    val completedAttempts by attemptRepository.allCompletedAttempts.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    var testToDelete by remember { mutableStateOf<TestEntity?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Quest CBT",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "JEE Main Mock Testing Platform",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            IconButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.testTag("home_settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = PeriwinkleAccent
                )
            }
        }

        if (tests.isEmpty()) {
            EmptyHomeState(
                onLoadSample = {
                    scope.launch {
                        val sample = SampleTestData.getSampleTest()
                        val testEntity = TestEntity(
                            id = "sample_jee_main_2026",
                            title = sample.title,
                            durationMin = sample.durationMin,
                            marksCorrect = 4,
                            marksNegative = 1,
                            totalMarks = sample.questions.size * 4,
                            questionCount = sample.questions.size,
                            questionsJson = SampleTestData.getSampleTestJsonString()
                        )
                        testRepository.saveTest(testEntity)
                    }
                },
                onTapUpload = onNavigateToUpload
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(tests, key = { it.id }) { test ->
                    val lastAttempt = completedAttempts.firstOrNull { it.testId == test.id }

                    TestCardItem(
                        test = test,
                        lastAttempt = lastAttempt,
                        onStart = { onStartTest(test.id) },
                        onViewAnalysis = {
                            if (lastAttempt != null) {
                                onViewAnalysis(lastAttempt.attemptId)
                            } else {
                                onStartTest(test.id)
                            }
                        },
                        onDelete = { testToDelete = test }
                    )
                }
            }
        }
    }

    // Delete Confirmation Dialog
    testToDelete?.let { test ->
        AlertDialog(
            onDismissRequest = { testToDelete = null },
            title = {
                Text(text = "Delete Test?", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete '${test.title}'? This cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            testRepository.deleteTest(test.id)
                            attemptRepository.deleteAttemptsForTest(test.id)
                            testToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { testToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardSurface
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TestCardItem(
    test: TestEntity,
    lastAttempt: AttemptEntity?,
    onStart: () -> Unit,
    onViewAnalysis: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }

    val hours = test.durationMin / 60
    val mins = test.durationMin % 60
    val durationText = buildString {
        if (hours > 0) append("$hours ${if (hours == 1) "hour" else "hours"}")
        if (mins > 0) {
            if (isNotEmpty()) append(" ")
            append("$mins mins")
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onStart,
                onLongClick = onDelete
            )
            .testTag("test_card_${test.id}"),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header row with Title & Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = test.title,
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${test.questionCount} questions • $durationText",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }

                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = TextMuted
                        )
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(CardSurface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Delete Test", color = Color(0xFFEF4444)) },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444))
                            },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subject Tags row
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SubjectBadge("Physics", PhysicsColor)
                SubjectBadge("Chemistry", ChemistryColor)
                SubjectBadge("Maths", MathsColor)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Attempt status if already attempted
            if (lastAttempt != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF243656).copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Last Score: ${lastAttempt.totalScore}/${lastAttempt.maxScore}",
                        color = PeriwinkleAccent,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Accuracy: ${lastAttempt.accuracyPercent.toInt()}%",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Buttons
            if (lastAttempt == null) {
                // Not attempted yet -> Full-width "Start Test"
                Button(
                    onClick = onStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("start_test_btn_${test.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PeriwinkleAccent,
                        contentColor = OnPeriwinkle
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Test",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            } else {
                // Attempted -> "Start Test or View Analysis"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onViewAnalysis,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("view_analysis_btn_${test.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PeriwinkleAccent),
                        border = androidx.compose.foundation.BorderStroke(1.dp, PeriwinkleAccent),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Analysis",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("retake_test_btn_${test.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PeriwinkleAccent,
                            contentColor = OnPeriwinkle
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Retake",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun EmptyHomeState(
    onLoadSample: () -> Unit,
    onTapUpload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E2838)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Science,
                    contentDescription = null,
                    tint = PeriwinkleAccent,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "No Tests Added Yet",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap the \"+\" button in the navigation bar to import your JEE Main paper JSON or ZIP file.",
                color = TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLoadSample,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeriwinkleAccent,
                    contentColor = OnPeriwinkle
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .height(48.dp)
                    .testTag("load_sample_test_btn")
            ) {
                Icon(Icons.Default.Description, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Load Sample JEE Main Paper", fontWeight = FontWeight.Bold)
            }
        }
    }
}
