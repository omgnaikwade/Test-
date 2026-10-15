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
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.TestJson
import com.quest.cbt.data.repository.TestRepository
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.ChemistryColor
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.MathsColor
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.PhysicsColor
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary
import kotlinx.serialization.json.Json

@Composable
fun SyllabusScreen(
    testId: String,
    testRepository: TestRepository,
    onStartTest: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val testState by testRepository.getTestById(testId).collectAsState(initial = null)
    val scrollState = rememberScrollState()

    val test = testState
    val parsedSyllabus = remember(test?.questionsJson) {
        test?.questionsJson?.let { jsonStr ->
            try {
                val json = Json { ignoreUnknownKeys = true }
                val parsed = json.decodeFromString<TestJson>(jsonStr)
                parsed.syllabus
            } catch (_: Exception) {
                emptyMap()
            }
        } ?: emptyMap()
    }

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
                    text = "Test Syllabus",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = test?.title ?: "JEE Main",
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
            Text(
                text = "Coverage for this Exam",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Physics Syllabus Card
            val physicsTopics = parsedSyllabus["Physics"] ?: listOf(
                "Mechanics & Rotational Dynamics",
                "Thermal Physics & Kinetic Theory",
                "Electrodynamics & AC Circuits",
                "Optics (Wave & Ray)",
                "Modern Physics & Semiconductor Devices"
            )
            SubjectSyllabusCard(
                subject = "Physics",
                accentColor = PhysicsColor,
                topics = physicsTopics
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Chemistry Syllabus Card
            val chemistryTopics = parsedSyllabus["Chemistry"] ?: listOf(
                "Chemical Bonding & Periodic Table",
                "Thermodynamics & Equilibrium",
                "Organic Reaction Mechanisms",
                "Coordination Compounds & d-Block",
                "Electrochemistry & Solutions"
            )
            SubjectSyllabusCard(
                subject = "Chemistry",
                accentColor = ChemistryColor,
                topics = chemistryTopics
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mathematics Syllabus Card
            val mathsTopics = parsedSyllabus["Maths"] ?: listOf(
                "Calculus & Differential Equations",
                "Algebra, Matrices & Determinants",
                "Coordinate Geometry & Conics",
                "Vectors & 3D Geometry",
                "Trigonometry & Statistics"
            )
            SubjectSyllabusCard(
                subject = "Mathematics",
                accentColor = MathsColor,
                topics = mathsTopics
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        // Bottom Start Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CardSurface)
                .border(1.dp, CardBorder)
                .padding(16.dp)
        ) {
            Button(
                onClick = onStartTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("syllabus_start_test_btn"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PeriwinkleAccent,
                    contentColor = OnPeriwinkle
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Start Test", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun SubjectSyllabusCard(
    subject: String,
    accentColor: Color,
    topics: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = subject,
                    color = accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${topics.size} chapters",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            topics.forEach { topic ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(accentColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = topic,
                        color = TextPrimary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
