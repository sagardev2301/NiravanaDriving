package com.niravanadriving.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.niravanadriving.app.data.models.LessonStatus
import com.niravanadriving.app.platform.openDialer
import com.niravanadriving.app.ui.components.OngoingClassCard
import com.niravanadriving.app.ui.components.ScheduleCard
import com.niravanadriving.app.ui.components.StatusBadge
import com.niravanadriving.app.ui.util.UiState
import com.niravanadriving.app.ui.viewmodel.HomeViewModel
import com.niravanadriving.app.util.DateTimeUtils
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.School
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) { /* Avatar */ }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "NirvanaDrive",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is UiState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                        Button(onClick = { viewModel.refresh() }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry")
                        }
                    }
                }
            }
            is UiState.Success -> {
                val data = state.data
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val todayText = "${now.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${now.dayOfMonth}, ${now.year}"

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    data.ongoingLesson?.let { (lesson, session) ->
                        item(key = "ongoing") {
                            OngoingClassCard(
                                lesson = lesson,
                                session = session,
                                onEndClass = { viewModel.endClass(lesson.id!!, session.id) }
                            )
                        }
                    }

                    if (data.ongoingLesson == null && data.todaySchedule.none { it.status == LessonStatus.SCHEDULED }) {
                        item(key = "summary") {
                            DaySummaryCard(
                                completedCount = data.completedTodayCount,
                                totalCollections = data.todayCollections,
                                isDayEmpty = data.todaySchedule.isEmpty()
                            )
                        }
                    }

                    item(key = "header") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Today's Schedule",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = todayText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            val nextClass = data.todaySchedule.firstOrNull { 
                                it.status == LessonStatus.SCHEDULED
                            }

                            if (nextClass != null) {
                                val minsUntil = DateTimeUtils.getMinutesUntil(nextClass.scheduledTime)
                                val badgeText = if (minsUntil > 0 && minsUntil <= 60) {
                                    "Next Class in $minsUntil mins"
                                } else {
                                    "Next Class: ${DateTimeUtils.formatTime(nextClass.scheduledTime)}"
                                }
                                StatusBadge(
                                    text = badgeText,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    items(data.todaySchedule, key = { it.id!! }) { lesson ->
                        ScheduleCard(
                            lesson = lesson,
                            onNavigate = { /* Navigate */ },
                            onCall = {
                                lesson.student?.phone?.let { phone ->
                                    if (phone.isNotBlank()) openDialer(phone)
                                }
                            },
                            onStartClass = if (lesson.status == LessonStatus.SCHEDULED) {
                                { viewModel.startClass(lesson.id!!) }
                            } else null,
                            isStartEnabled = data.ongoingLesson == null
                        )
                    }
                    
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DaySummaryCard(
    completedCount: Int,
    totalCollections: Double,
    isDayEmpty: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Icon(
                    imageVector = if (isDayEmpty) Icons.Default.EventBusy else Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.padding(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isDayEmpty) "No classes scheduled for today" else "All done for today!",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!isDayEmpty) {
                    Text(
                        text = "Great job on completing your sessions.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isDayEmpty) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SummaryStat(
                        modifier = Modifier.weight(1f),
                        label = "Completed",
                        value = "$completedCount",
                        icon = Icons.Default.School
                    )
                    SummaryStat(
                        modifier = Modifier.weight(1f),
                        label = "Collected",
                        value = "₹${totalCollections.toInt()}",
                        icon = Icons.Default.Payments
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStat(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: ImageVector
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Column {
                Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}
