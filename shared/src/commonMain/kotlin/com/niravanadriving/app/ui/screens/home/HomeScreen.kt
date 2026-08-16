package com.niravanadriving.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niravanadriving.app.data.models.Instructor
import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.models.LessonSession
import com.niravanadriving.app.data.models.LessonStatus
import com.niravanadriving.app.data.repository.InstructorRepository
import com.niravanadriving.app.data.repository.LessonRepository
import com.niravanadriving.app.ui.components.OngoingClassCard
import com.niravanadriving.app.ui.components.ScheduleCard
import com.niravanadriving.app.ui.components.StatusBadge
import com.niravanadriving.app.ui.util.UiState
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

data class HomeData(
    val instructor: Instructor,
    val ongoingLesson: Pair<Lesson, LessonSession>?,
    val todaySchedule: List<Lesson>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var uiState by remember { mutableStateOf<UiState<HomeData>>(UiState.Loading) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        uiState = UiState.Loading
        scope.launch {
            try {
                val instructor = InstructorRepository.getCurrentInstructor()
                if (instructor == null) {
                    uiState = UiState.Error("Failed to resolve instructor profile")
                    return@launch
                }
                val ongoing = LessonRepository.getOngoingLesson(instructor.id)
                val schedule = LessonRepository.getTodayLessons(instructor.id)
                uiState = UiState.Success(HomeData(instructor, ongoing, schedule))
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            // Avatar placeholder
                        }
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
                    IconButton(onClick = { /* Search */ }) {
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
                        Button(onClick = { loadData() }, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Retry")
                        }
                    }
                }
            }
            is UiState.Success -> {
                val data = state.data
                val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val todayText = "${now.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${now.day}, ${now.year}"

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    data.ongoingLesson?.let { (lesson, session) ->
                        item {
                            OngoingClassCard(
                                lesson = lesson,
                                session = session,
                                onEndClass = { /* End Class */ }
                            )
                        }
                    }

                    item {
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
                            
                            // Compute next class time
                            val nextClass = data.todaySchedule.firstOrNull { 
                                it.status == LessonStatus.SCHEDULED
                            }

                            if (nextClass != null) {
                                val minsUntil = com.niravanadriving.app.util.DateTimeUtils.getMinutesUntil(nextClass.scheduledTime)
                                val badgeText = if (minsUntil > 0 && minsUntil <= 60) {
                                    "Next Class in $minsUntil mins"
                                } else {
                                    "Next Class: ${com.niravanadriving.app.util.DateTimeUtils.formatTime(nextClass.scheduledTime)}"
                                }
                                StatusBadge(
                                    text = badgeText,
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    items(data.todaySchedule) { lesson ->
                        ScheduleCard(
                            lesson = lesson,
                            onNavigate = { /* Navigate */ },
                            onCall = { /* Call */ }
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
