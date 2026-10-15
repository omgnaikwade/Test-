package com.quest.cbt.ui.components

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.quest.cbt.data.model.AttemptEntity
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextMuted
import com.quest.cbt.ui.theme.TextSecondary

@Composable
fun ScoreLineChart(
    attempts: List<AttemptEntity>,
    modifier: Modifier = Modifier
) {
    // Reverse so chronologically left to right
    val chronologicalAttempts = remember(attempts) {
        attempts.sortedBy { it.endTime }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        if (chronologicalAttempts.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No test attempts yet.\nComplete a mock test to see your score progression!",
                    color = TextMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
        } else {
            Column {
                Text(
                    text = "Score Progression (out of 300)",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    val width = size.width
                    val height = size.height

                    val paddingLeft = 70f
                    val paddingRight = 40f
                    val paddingTop = 20f
                    val paddingBottom = 40f

                    val chartWidth = width - paddingLeft - paddingRight
                    val chartHeight = height - paddingTop - paddingBottom

                    val maxScore = 300f
                    val minScore = 0f

                    // Grid lines (0, 100, 200, 300)
                    val textPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#94A3B8")
                        textSize = 28f
                        isAntiAlias = true
                        textAlign = Paint.Align.RIGHT
                    }

                    val gridPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#2A354A")
                        strokeWidth = 1.5f
                    }

                    val levels = listOf(0, 100, 200, 300)
                    levels.forEach { level ->
                        val y = paddingTop + chartHeight - (level.toFloat() / maxScore) * chartHeight
                        drawLine(
                            color = Color(0xFF2A354A),
                            start = Offset(paddingLeft, y),
                            end = Offset(width - paddingRight, y),
                            strokeWidth = 1f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "$level",
                            paddingLeft - 15f,
                            y + 10f,
                            textPaint
                        )
                    }

                    // Benchmark target line (e.g. 200 marks for 99th percentile)
                    val targetY = paddingTop + chartHeight - (200f / maxScore) * chartHeight
                    drawLine(
                        color = Color(0x60FBBF24),
                        start = Offset(paddingLeft, targetY),
                        end = Offset(width - paddingRight, targetY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f))
                    )

                    val count = chronologicalAttempts.size
                    val points = mutableListOf<Offset>()

                    chronologicalAttempts.forEachIndexed { index, attempt ->
                        val x = if (count == 1) {
                            paddingLeft + chartWidth / 2f
                        } else {
                            paddingLeft + (index.toFloat() / (count - 1)) * chartWidth
                        }
                        val scoreClamped = attempt.totalScore.coerceIn(0, 300)
                        val y = paddingTop + chartHeight - (scoreClamped.toFloat() / maxScore) * chartHeight
                        points.add(Offset(x, y))

                        // X-axis label (#1, #2, etc.)
                        val labelPaint = Paint().apply {
                            color = android.graphics.Color.parseColor("#94A3B8")
                            textSize = 24f
                            isAntiAlias = true
                            textAlign = Paint.Align.CENTER
                        }
                        drawContext.canvas.nativeCanvas.drawText(
                            "#${index + 1}",
                            x,
                            height - 10f,
                            labelPaint
                        )
                    }

                    if (points.size > 1) {
                        val strokePath = Path()
                        val fillPath = Path()

                        strokePath.moveTo(points.first().x, points.first().y)
                        fillPath.moveTo(points.first().x, paddingTop + chartHeight)
                        fillPath.lineTo(points.first().x, points.first().y)

                        for (i in 0 until points.size - 1) {
                            val p0 = points[i]
                            val p1 = points[i + 1]
                            val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                            val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                            strokePath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                            fillPath.cubicTo(
                                controlPoint1.x, controlPoint1.y,
                                controlPoint2.x, controlPoint2.y,
                                p1.x, p1.y
                            )
                        }

                        fillPath.lineTo(points.last().x, paddingTop + chartHeight)
                        fillPath.close()

                        // Draw gradient fill under curve
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PeriwinkleAccent.copy(alpha = 0.35f),
                                    Color.Transparent
                                ),
                                startY = paddingTop,
                                endY = paddingTop + chartHeight
                            )
                        )

                        // Draw line curve
                        drawPath(
                            path = strokePath,
                            color = PeriwinkleAccent,
                            style = Stroke(width = 3.5.dp.toPx())
                        )
                    }

                    // Draw dots and value labels for each point
                    val scoreLabelPaint = Paint().apply {
                        color = android.graphics.Color.parseColor("#F1F5F9")
                        textSize = 26f
                        isAntiAlias = true
                        textAlign = Paint.Align.CENTER
                        isFakeBoldText = true
                    }

                    points.forEachIndexed { idx, point ->
                        val attempt = chronologicalAttempts[idx]

                        // Outer glowing circle
                        drawCircle(
                            color = PeriwinkleAccent.copy(alpha = 0.3f),
                            radius = 9.dp.toPx(),
                            center = point
                        )
                        // Inner solid circle
                        drawCircle(
                            color = PeriwinkleAccent,
                            radius = 5.dp.toPx(),
                            center = point
                        )
                        drawCircle(
                            color = CardSurface,
                            radius = 2.5.dp.toPx(),
                            center = point
                        )

                        // Draw score above dot
                        drawContext.canvas.nativeCanvas.drawText(
                            "${attempt.totalScore}",
                            point.x,
                            point.y - 18f,
                            scoreLabelPaint
                        )
                    }
                }
            }
        }
    }
}
