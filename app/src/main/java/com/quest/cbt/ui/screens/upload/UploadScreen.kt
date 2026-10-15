package com.quest.cbt.ui.screens.upload

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.TestEntity
import com.quest.cbt.data.model.TestJson
import com.quest.cbt.data.repository.TestRepository
import com.quest.cbt.domain.TestValidator
import com.quest.cbt.domain.ValidationResult
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.CardSurfaceElevated
import com.quest.cbt.ui.theme.DarkNavyBg
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextMuted
import com.quest.cbt.ui.theme.TextPrimary
import com.quest.cbt.ui.theme.TextSecondary
import com.quest.cbt.util.PDF_TO_JSON_PROMPT
import com.quest.cbt.util.SampleTestData
import com.quest.cbt.util.ZipUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.util.UUID

@Composable
fun UploadScreen(
    testRepository: TestRepository,
    onTestSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var isProcessing by remember { mutableStateOf(false) }
    var parsedTest by remember { mutableStateOf<TestJson?>(null) }
    var rawJsonString by remember { mutableStateOf<String?>(null) }
    var extractedBaseDir by remember { mutableStateOf<String?>(null) }

    // Form fields
    var testName by remember { mutableStateOf("") }
    var durationMinutesText by remember { mutableStateOf("180") }
    var totalMarksText by remember { mutableStateOf("300") }
    var marksCorrectText by remember { mutableStateOf("4") }
    var marksNegativeText by remember { mutableStateOf("1") }

    // Error tracking
    var validationError by remember { mutableStateOf<String?>(null) }
    var detailedErrorsList by remember { mutableStateOf<List<String>>(emptyList()) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            isProcessing = true
            validationError = null
            detailedErrorsList = emptyList()

            scope.launch {
                try {
                    val testId = UUID.randomUUID().toString()
                    val extracted = withContext(Dispatchers.IO) {
                        ZipUtils.processImportUri(context, uri, testId)
                    }

                    val json = Json { ignoreUnknownKeys = true }
                    val test = json.decodeFromString<TestJson>(extracted.jsonContent)

                    // Validate
                    when (val result = TestValidator.validate(test)) {
                        is ValidationResult.Success -> {
                            parsedTest = test
                            rawJsonString = extracted.jsonContent
                            extractedBaseDir = extracted.imagesDir?.absolutePath

                            testName = test.title
                            durationMinutesText = test.durationMin.toString()
                            marksCorrectText = "4"
                            marksNegativeText = "1"
                            totalMarksText = (test.questions.size * 4).toString()
                            validationError = null
                            detailedErrorsList = emptyList()
                        }
                        is ValidationResult.Error -> {
                            validationError = result.message
                            detailedErrorsList = result.detailedErrors
                            parsedTest = null
                        }
                    }
                } catch (e: Exception) {
                    val msg = e.localizedMessage ?: "Failed to read or parse file."
                    validationError = "Parsing error: $msg"
                    detailedErrorsList = listOf(msg)
                    parsedTest = null
                } finally {
                    isProcessing = false
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkNavyBg)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .padding(bottom = 90.dp)
    ) {
        Text(
            text = "Import Test",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "Upload a JEE Main mock paper in JSON or ZIP format",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Import Button Box
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
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(PeriwinkleAccent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = null,
                        tint = PeriwinkleAccent,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Import .json or .zip",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ZIP packages can include test.json and an images/ folder",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                )

                if (isProcessing) {
                    CircularProgressIndicator(
                        color = PeriwinkleAccent,
                        modifier = Modifier.size(32.dp)
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                filePickerLauncher.launch(arrayOf("application/json", "application/zip", "*/*"))
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("import_test_file_picker_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PeriwinkleAccent,
                                contentColor = OnPeriwinkle
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Import Test", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                isProcessing = true
                                scope.launch {
                                    val sample = SampleTestData.getSampleTest()
                                    parsedTest = sample
                                    rawJsonString = SampleTestData.getSampleTestJsonString()
                                    extractedBaseDir = null
                                    testName = sample.title
                                    durationMinutesText = sample.durationMin.toString()
                                    marksCorrectText = "4"
                                    marksNegativeText = "1"
                                    totalMarksText = (sample.questions.size * 4).toString()
                                    validationError = null
                                    detailedErrorsList = emptyList()
                                    isProcessing = false
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("load_sample_into_form_btn"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PeriwinkleAccent),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Load Sample", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        // Validation Error Box
        AnimatedVisibility(visible = validationError != null) {
            validationError?.let { err ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1619)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Validation Error",
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = err,
                            color = Color(0xFFFCA5A5),
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )

                        if (detailedErrorsList.size > 1) {
                            Spacer(modifier = Modifier.height(6.dp))
                            detailedErrorsList.take(5).forEach { detail ->
                                Text(
                                    text = "• $detail",
                                    color = Color(0xFFF87171),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val fullErrorText = buildString {
                                    appendLine("Quest CBT Validation Error:")
                                    appendLine(err)
                                    detailedErrorsList.forEach { appendLine("- $it") }
                                }
                                clipboard.setPrimaryClip(ClipData.newPlainText("Quest CBT Error", fullErrorText))
                                Toast.makeText(context, "Error copied to clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF4444)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("copy_error_btn")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Error", fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Form shown after successful parsing
        AnimatedVisibility(visible = parsedTest != null) {
            parsedTest?.let { test ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = CardSurface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF22C55E)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Test Configuration",
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Test Name
                        OutlinedTextField(
                            value = testName,
                            onValueChange = { testName = it },
                            label = { Text("Test Name") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_test_name"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PeriwinkleAccent,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Duration in minutes
                        OutlinedTextField(
                            value = durationMinutesText,
                            onValueChange = { durationMinutesText = it },
                            label = { Text("Duration (minutes)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("form_test_duration"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PeriwinkleAccent,
                                unfocusedBorderColor = CardBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Total Marks & Question Count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = totalMarksText,
                                onValueChange = { totalMarksText = it },
                                label = { Text("Total Marks") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_total_marks"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PeriwinkleAccent,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = "${test.questions.size} Questions",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Questions") },
                                modifier = Modifier.weight(1f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = CardBorder,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextSecondary,
                                    unfocusedTextColor = TextSecondary
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Marks correct and negative
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = marksCorrectText,
                                onValueChange = {
                                    marksCorrectText = it
                                    val c = it.toIntOrNull() ?: 4
                                    totalMarksText = (test.questions.size * c).toString()
                                },
                                label = { Text("Correct (+)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_marks_correct"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PeriwinkleAccent,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )

                            OutlinedTextField(
                                value = marksNegativeText,
                                onValueChange = { marksNegativeText = it },
                                label = { Text("Negative (-)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("form_marks_negative"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PeriwinkleAccent,
                                    unfocusedBorderColor = CardBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Save Button
                        Button(
                            onClick = {
                                scope.launch {
                                    val testId = UUID.randomUUID().toString()
                                    val duration = durationMinutesText.toIntOrNull() ?: 180
                                    val correct = marksCorrectText.toIntOrNull() ?: 4
                                    val negative = marksNegativeText.toIntOrNull() ?: 1
                                    val total = totalMarksText.toIntOrNull() ?: (test.questions.size * correct)

                                    val entity = TestEntity(
                                        id = testId,
                                        title = testName.ifBlank { test.title },
                                        durationMin = duration,
                                        marksCorrect = correct,
                                        marksNegative = negative,
                                        totalMarks = total,
                                        questionCount = test.questions.size,
                                        questionsJson = rawJsonString ?: SampleTestData.getSampleTestJsonString(),
                                        baseDir = extractedBaseDir
                                    )

                                    testRepository.saveTest(entity)
                                    Toast.makeText(context, "Test saved successfully!", Toast.LENGTH_SHORT).show()
                                    onTestSaved()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("save_test_btn"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PeriwinkleAccent,
                                contentColor = OnPeriwinkle
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Test to Library", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PDF to JSON Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = CardSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = PeriwinkleAccent
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Convert any PDF to JSON",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                val steps = listOf(
                    "1. Copy the parser prompt below",
                    "2. Open any AI model (Claude 3.5/3.7, ChatGPT 4o, Gemini 1.5/2.0)",
                    "3. Attach the question paper PDF & answer key, then paste the prompt",
                    "4. Download the resulting ZIP package (or test.json)",
                    "5. Import it directly into Quest CBT"
                )

                steps.forEach { step ->
                    Text(
                        text = step,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("PDF_TO_JSON_PROMPT", PDF_TO_JSON_PROMPT))
                        Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("copy_prompt_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CardSurfaceElevated,
                        contentColor = PeriwinkleAccent
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy Prompt", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}
