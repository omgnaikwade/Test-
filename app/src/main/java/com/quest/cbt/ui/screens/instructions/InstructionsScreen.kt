package com.quest.cbt.ui.screens.instructions

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.NtaStatus
import com.quest.cbt.data.repository.TestRepository
import com.quest.cbt.ui.components.NtaLegendItem
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary

@Composable
fun InstructionsScreen(
    testId: String,
    testRepository: TestRepository,
    onProceedToSyllabus: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val testState by testRepository.getTestById(testId).collectAsState(initial = null)
    var isAgreed by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    val test = testState

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
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "General Instructions",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = test?.title ?: "JEE Main Examination",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Examination Pattern",
                        color = PeriwinkleAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Duration: ${test?.durationMin ?: 180} minutes\n" +
                                "• Total Questions: ${test?.questionCount ?: 75} questions\n" +
                                "• Sections: Physics, Chemistry, and Mathematics\n" +
                                "• Section A: Multiple Choice Questions (Single option correct)\n" +
                                "• Section B: Numerical Value Questions\n" +
                                "• Marking Scheme: +${test?.marksCorrect ?: 4} for correct, -${test?.marksNegative ?: 1} for incorrect",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Palette Legend Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Question Palette Symbols",
                        color = PeriwinkleAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    NtaLegendItem(status = NtaStatus.NOT_VISITED, count = 1, label = "You have not visited the question yet.")
                    NtaLegendItem(status = NtaStatus.NOT_ANSWERED, count = 2, label = "You have visited but not answered the question.")
                    NtaLegendItem(status = NtaStatus.ANSWERED, count = 3, label = "You have answered the question.")
                    NtaLegendItem(status = NtaStatus.MARKED_FOR_REVIEW, count = 4, label = "You have NOT answered the question, but marked it for review.")
                    NtaLegendItem(status = NtaStatus.ANSWERED_AND_MARKED, count = 5, label = "The question is answered and marked for review (WILL be evaluated).")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Navigation Instructions
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Navigating to a Question",
                        color = PeriwinkleAccent,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. Click on the question number in the Question Palette to jump directly to that question.\n" +
                                "2. Click on 'Save & Next' to save your answer and go to the next question.\n" +
                                "3. Click on 'Clear Response' to deselect your chosen answer.\n" +
                                "4. Click on 'Mark for Review & Next' to mark the question for review.",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Declaration Checkbox
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = isAgreed,
                    onCheckedChange = { isAgreed = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PeriwinkleAccent,
                        checkmarkColor = OnPeriwinkle,
                        uncheckedColor = CardBorder
                    ),
                    modifier = Modifier.testTag("instructions_checkbox")
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "I have read and understood all the instructions above. I declare that all hardware and setup are in working order and I agree to proceed with the test.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Bottom Action Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface)
                .border(1.dp, CardBorder)
                .padding(16.dp)
        ) {
            Button(
                onClick = onProceedToSyllabus,
                enabled = isAgreed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("proceed_to_syllabus_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeriwinkleAccent,
                    contentColor = OnPeriwinkle,
                    disabledContainerColor = CardBorder,
                    disabledContentColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Proceed to Syllabus", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}
