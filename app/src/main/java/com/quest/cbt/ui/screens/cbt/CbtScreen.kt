package com.quest.cbt.ui.screens.cbt

import android.app.Activity
import android.content.res.Configuration
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.QuestionJson
import com.quest.cbt.ui.components.KatexMathView
import com.quest.cbt.ui.components.NumericKeypad
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.CardSurfaceElevated
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CbtScreen(
    viewModel: CbtViewModel,
    onTestSubmitted: (String) -> Unit,
    onExitTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val context = LocalContext.current

    // Keep screen on during CBT examination
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Intercept back button to confirm exit
    BackHandler {
        viewModel.toggleExitConfirmDialog(true)
    }

    // Observe submitted event
    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is CbtEvent.Submitted -> onTestSubmitted(event.attemptId)
            }
        }
    }

    if (uiState.isLoading || uiState.questions.isEmpty()) {
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

    val currentQ = uiState.questions.getOrNull(uiState.currentQuestionIndex) ?: return
    val currentAttemptState = uiState.answers[currentQ.id]
    val baseDir = uiState.test?.baseDir

    // Format remaining time (HH:MM:SS)
    val remSec = uiState.remainingSeconds
    val hours = remSec / 3600
    val minutes = (remSec % 3600) / 60
    val seconds = remSec % 60
    val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
    val isUrgent = remSec < 600 // Less than 10 mins

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // TOP BAR (NTA CBT Style)
            TopCbtBar(
                testTitle = uiState.test?.title ?: "JEE Main",
                timerText = timerText,
                isUrgent = isUrgent,
                isLandscape = isLandscape,
                onPaletteToggle = { viewModel.togglePaletteDrawer(!uiState.isPaletteDrawerOpen) }
            )

            // SUBJECT TABS BAR (Physics, Chemistry, Maths & Section info)
            SubjectTabBar(
                currentSubject = uiState.currentSubject,
                onSelectSubject = { viewModel.selectSubject(it) },
                currentSection = currentQ.section,
                currentType = currentQ.type
            )

            // MAIN AREA (Landscape: Question on left, Palette on right; Portrait: Question full width)
            if (isLandscape) {
                Row(modifier = Modifier.weight(1f)) {
                    // Question Area (Left)
                    Box(modifier = Modifier.weight(0.68f)) {
                        QuestionContent(
                            question = currentQ,
                            qState = currentAttemptState,
                            baseDir = baseDir,
                            marksCorrect = uiState.test?.marksCorrect ?: 4,
                            marksNegative = uiState.test?.marksNegative ?: 1,
                            onOptionSelected = { viewModel.selectMcqOption(it) },
                            onNumericChanged = { viewModel.setNumericAnswer(it) }
                        )
                    }

                    androidx.compose.material3.VerticalDivider(color = CardBorder, thickness = 1.dp)

                    // Question Palette (Right, Permanent in Landscape)
                    Box(
                        modifier = Modifier.weight(0.32f)
                    ) {
                        QuestionPaletteContent(
                            currentSubject = uiState.currentSubject,
                            allQuestions = uiState.questions,
                            currentQuestionIndex = uiState.currentQuestionIndex,
                            answers = uiState.answers,
                            onSelectQuestion = { viewModel.navigateToQuestion(it) },
                            onSubmitClick = { viewModel.toggleSubmitDialog(true) },
                            onQuestionPaperClick = { viewModel.toggleQuestionPaperDialog(true) },
                            onInstructionsClick = { viewModel.toggleInstructionsDialog(true) }
                        )
                    }
                }
            } else {
                // Portrait Layout
                Box(modifier = Modifier.weight(1f)) {
                    QuestionContent(
                        question = currentQ,
                        qState = currentAttemptState,
                        baseDir = baseDir,
                        marksCorrect = uiState.test?.marksCorrect ?: 4,
                        marksNegative = uiState.test?.marksNegative ?: 1,
                        onOptionSelected = { viewModel.selectMcqOption(it) },
                        onNumericChanged = { viewModel.setNumericAnswer(it) }
                    )

                    // Slide-in Drawer in Portrait
                    androidx.compose.animation.AnimatedVisibility(
                        visible = uiState.isPaletteDrawerOpen,
                        enter = slideInHorizontally(initialOffsetX = { it }),
                        exit = slideOutHorizontally(targetOffsetX = { it })
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { viewModel.togglePaletteDrawer(false) },
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.85f)
                                    .background(DarkNavyBg)
                                    .clickable(enabled = false) {}
                            ) {
                                QuestionPaletteContent(
                                    currentSubject = uiState.currentSubject,
                                    allQuestions = uiState.questions,
                                    currentQuestionIndex = uiState.currentQuestionIndex,
                                    answers = uiState.answers,
                                    onSelectQuestion = {
                                        viewModel.navigateToQuestion(it)
                                        viewModel.togglePaletteDrawer(false)
                                    },
                                    onSubmitClick = {
                                        viewModel.togglePaletteDrawer(false)
                                        viewModel.toggleSubmitDialog(true)
                                    },
                                    onQuestionPaperClick = {
                                        viewModel.togglePaletteDrawer(false)
                                        viewModel.toggleQuestionPaperDialog(true)
                                    },
                                    onInstructionsClick = {
                                        viewModel.togglePaletteDrawer(false)
                                        viewModel.toggleInstructionsDialog(true)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // BOTTOM ACTION BUTTONS
            BottomActionButtons(
                hasPrevious = uiState.currentQuestionIndex > 0,
                onPrevious = { viewModel.navigatePrevious() },
                onClear = { viewModel.clearResponse() },
                onMarkForReview = { viewModel.markForReviewAndNext() },
                onSaveAndNext = { viewModel.saveAndNext() }
            )
        }

        // Dialogs
        if (uiState.isSubmitDialogOpen) {
            SubmitSummaryDialog(
                questions = uiState.questions,
                answers = uiState.answers,
                onConfirmSubmit = {
                    viewModel.toggleSubmitDialog(false)
                    viewModel.submitTest()
                },
                onDismiss = { viewModel.toggleSubmitDialog(false) }
            )
        }

        if (uiState.isQuestionPaperDialogOpen) {
            QuestionPaperDialog(
                questions = uiState.questions,
                baseDir = baseDir,
                onDismiss = { viewModel.toggleQuestionPaperDialog(false) }
            )
        }

        if (uiState.isInstructionsDialogOpen) {
            CbtInstructionsModal(
                onDismiss = { viewModel.toggleInstructionsDialog(false) }
            )
        }

        if (uiState.isExitConfirmDialogOpen) {
            AlertDialog(
                onDismissRequest = { viewModel.toggleExitConfirmDialog(false) },
                title = { Text("Pause & Exit Test?", color = TextPrimary, fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Your progress has been saved locally. You can resume this test at any time from the home screen.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.toggleExitConfirmDialog(false)
                            onExitTest()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PeriwinkleAccent, contentColor = OnPeriwinkle)
                    ) {
                        Text("Exit Test", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.toggleExitConfirmDialog(false) }) {
                        Text("Continue Test", color = TextSecondary)
                    }
                },
                containerColor = CardSurface
            )
        }
    }
}

@Composable
private fun TopCbtBar(
    testTitle: String,
    timerText: String,
    isUrgent: Boolean,
    isLandscape: Boolean,
    onPaletteToggle: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = testTitle,
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Text(
                text = "Candidate Console",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }

        // Countdown Timer
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isUrgent) Color(0xFF7F1D1D) else CardSurfaceElevated)
                .border(1.dp, if (isUrgent) Color(0xFFEF4444) else CardBorder, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Timer,
                contentDescription = null,
                tint = if (isUrgent) Color(0xFFFCA5A5) else PeriwinkleAccent,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Time Left: ",
                color = if (isUrgent) Color(0xFFFCA5A5) else TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = timerText,
                color = if (isUrgent) Color.White else PeriwinkleAccent,
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Palette Toggle button in portrait
        if (!isLandscape) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = onPaletteToggle,
                modifier = Modifier.testTag("toggle_palette_drawer_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.GridOn,
                    contentDescription = "Open Palette",
                    tint = PeriwinkleAccent
                )
            }
        }
    }
    androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp)
}
}

@Composable
private fun SubjectTabBar(
    currentSubject: String,
    onSelectSubject: (String) -> Unit,
    currentSection: String,
    currentType: String
) {
    val subjects = listOf("Physics", "Chemistry", "Maths")
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkNavyBg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            subjects.forEach { subj ->
                val isSelected = subj == currentSubject
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) PeriwinkleAccent else CardSurfaceElevated)
                        .clickable { onSelectSubject(subj) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("subject_tab_$subj")
                ) {
                    Text(
                        text = subj,
                        color = if (isSelected) OnPeriwinkle else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Section badge (Section A: MCQ / Section B: NUM)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CardSurface)
                    .border(1.dp, CardBorder, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Sec $currentSection ($currentType)",
                    color = PeriwinkleAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp)
    }
}

@Composable
private fun QuestionContent(
    question: QuestionJson,
    qState: com.quest.cbt.data.model.QuestionAttemptState?,
    baseDir: String?,
    marksCorrect: Int,
    marksNegative: Int,
    onOptionSelected: (String) -> Unit,
    onNumericChanged: (String) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(14.dp)
    ) {
        // Question Header: Question No & Marking scheme
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question No. ${question.id}",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Correct +$marksCorrect / Negative -$marksNegative",
                color = Color(0xFF22C55E),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Question Statement rendered with KaTeX
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                KatexMathView(
                    text = question.text,
                    baseDir = baseDir,
                    images = question.images,
                    fontSizeSp = 15
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Response section
        if (question.type == "MCQ") {
            Text(
                text = "Select one answer option:",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val optionLabels = listOf("A", "B", "C", "D")
            question.options.forEachIndexed { index, option ->
                val label = optionLabels.getOrNull(index) ?: ('A' + index).toString()
                val isSelected = qState?.selectedOption == label

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            1.dp,
                            if (isSelected) PeriwinkleAccent else CardBorder,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onOptionSelected(label) }
                        .testTag("option_${label}_btn"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(0xFF1E2F4A) else CardSurface
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onOptionSelected(label) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PeriwinkleAccent,
                                unselectedColor = TextSecondary
                            )
                        )
                        Text(
                            text = "($label)",
                            color = if (isSelected) PeriwinkleAccent else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            KatexMathView(
                                text = option.text,
                                baseDir = baseDir,
                                images = option.image?.let { listOf(it) } ?: emptyList(),
                                fontSizeSp = 14
                            )
                        }
                    }
                }
            }
        } else {
            // NUM question
            Text(
                text = "Numerical Answer (Enter using keypad below):",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(CardSurface)
                    .border(1.5.dp, PeriwinkleAccent, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Text(
                    text = qState?.numericAnswer?.ifBlank { "0" } ?: "0",
                    color = if (!qState?.numericAnswer.isNullOrBlank()) TextPrimary else TextSecondary,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            NumericKeypad(
                currentValue = qState?.numericAnswer ?: "",
                onValueChange = onNumericChanged,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun BottomActionButtons(
    hasPrevious: Boolean,
    onPrevious: () -> Unit,
    onClear: () -> Unit,
    onMarkForReview: () -> Unit,
    onSaveAndNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
    ) {
        androidx.compose.material3.HorizontalDivider(color = CardBorder, thickness = 1.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Previous
        OutlinedButton(
            onClick = onPrevious,
            enabled = hasPrevious,
            modifier = Modifier
                .weight(1f)
                .height(44.dp)
                .testTag("cbt_prev_btn"),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = TextPrimary,
                disabledContentColor = TextSecondary.copy(alpha = 0.5f)
            )
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Prev", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        // Clear Response
        OutlinedButton(
            onClick = onClear,
            modifier = Modifier
                .weight(1.1f)
                .height(44.dp)
                .testTag("cbt_clear_btn"),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF87171))
        ) {
            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Clear", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        // Mark for Review & Next
        Button(
            onClick = onMarkForReview,
            modifier = Modifier
                .weight(1.4f)
                .height(44.dp)
                .testTag("cbt_mark_review_btn"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFA855F7),
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.BookmarkBorder, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(3.dp))
            Text("Review", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }

        // Save & Next
        Button(
            onClick = onSaveAndNext,
            modifier = Modifier
                .weight(1.4f)
                .height(44.dp)
                .testTag("cbt_save_next_btn"),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF22C55E),
                contentColor = Color.White
            )
        ) {
            Text("Save & Next", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(3.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
        }
    }
}
}
