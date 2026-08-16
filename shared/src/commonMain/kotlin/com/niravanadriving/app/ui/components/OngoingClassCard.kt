package com.niravanadriving.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.models.LessonSession
import kotlinx.coroutines.delay
import kotlinx.datetime.Instant
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.DurationUnit

@Composable
fun OngoingClassCard(
    lesson: Lesson,
    session: LessonSession,
    onEndClass: () -> Unit
) {
    var elapsedText by remember { mutableStateOf("00:00") }
    val startedAt = remember(session.id) { Instant.parse(session.startedAt) }
    var isOvertime by remember { mutableStateOf(false) }

    LaunchedEffect(session.id) {
        while (true) {
            val now = Clock.System.now()
            // Duration between kotlin.time.Instant (now) and kotlinx.datetime.Instant (startedAt)
            // In 0.8.0 they are the same type.
            val elapsed = now - startedAt
            val totalSeconds = elapsed.toInt(DurationUnit.SECONDS)
            val minutes = (totalSeconds / 60).toString().padStart(2, '0')
            val seconds = (totalSeconds % 60).toString().padStart(2, '0')
            elapsedText = "$minutes:$seconds"
            isOvertime = elapsed.toInt(DurationUnit.MINUTES) >= lesson.durationMinutes
            delay(1000)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Icon and Info
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, // Placeholder for car icon
                            contentDescription = null,
                            modifier = Modifier.padding(16.dp),
                            tint = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(
                                text = "ONGOING CLASS",
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                text = "Live",
                                containerColor = Color.Transparent,
                                contentColor = Color.White,
                                isLive = true
                            )
                        }
                        Text(
                            text = lesson.student?.fullName ?: "Unknown Student",
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${lesson.vehicle?.makeModel ?: "Car"} \u2022 Route: ${lesson.route ?: "N/A"}\n${lesson.notes ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = elapsedText,
                        style = MaterialTheme.typography.displayMedium,
                        color = if (isOvertime) MaterialTheme.colorScheme.error else Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "/ ${lesson.durationMinutes}:00 MINS",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Button(
                    onClick = onEndClass,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("End Class", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
