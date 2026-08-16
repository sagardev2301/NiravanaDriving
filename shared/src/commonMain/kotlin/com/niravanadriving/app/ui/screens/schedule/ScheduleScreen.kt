package com.niravanadriving.app.ui.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.repository.InstructorRepository
import com.niravanadriving.app.data.repository.LessonRepository
import com.niravanadriving.app.ui.components.ScheduleItemCard
import com.niravanadriving.app.ui.util.UiState
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    var uiState by remember { mutableStateOf<UiState<Unit>>(UiState.Loading) }
    var lessons by remember { mutableStateOf<List<Lesson>>(emptyList()) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    val tomorrow = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.plus(1, DateTimeUnit.DAY)
    }

    fun loadData() {
        uiState = UiState.Loading
        scope.launch {
            try {
                val instructor = InstructorRepository.getCurrentInstructor()
                if (instructor == null) {
                    uiState = UiState.Error("Instructor not found")
                    return@launch
                }
                val result = LessonRepository.getLessonsForDate(instructor.id, tomorrow)
                lessons = result
                uiState = UiState.Success(Unit)
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Error loading schedule")
            }
        }
    }

    LaunchedEffect(Unit) {
        loadData()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(32.dp),
                            shape = CircleShape,
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
                val tomorrowTitle = "Tomorrow, ${tomorrow.month.name.take(3)} ${tomorrow.dayOfMonth}"

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = tomorrowTitle,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }

                        item {
                            Button(
                                onClick = {
                                    scope.launch {
                                        val instructor = InstructorRepository.getCurrentInstructor()
                                        if (instructor != null) {
                                            val yesterdayLessons = LessonRepository.getYesterdayPublishedLessons(instructor.id)
                                            if (yesterdayLessons.isEmpty()) {
                                                snackbarHostState.showSnackbar("No published lessons from yesterday to fill.")
                                            } else {
                                                val newLessons = yesterdayLessons.map {
                                                    it.copy(
                                                        id = null,
                                                        scheduledDate = tomorrow.toString(),
                                                        isDraft = true
                                                    )
                                                }
                                                lessons = lessons + newLessons
                                                snackbarHostState.showSnackbar("Auto-filled from yesterday.")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ElectricBolt,
                                    contentDescription = null,
                                    tint = Color(0xFFFEB316),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Auto-Fill from Yesterday's Schedule",
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        itemsIndexed(lessons) { index, lesson ->
                            ScheduleItemCard(
                                lesson = lesson,
                                onRemove = {
                                    scope.launch {
                                        val id = lesson.id
                                        if (id != null) {
                                            if (LessonRepository.deleteLesson(id)) {
                                                lessons = lessons.filter { it.id != id }
                                            } else {
                                                snackbarHostState.showSnackbar("Failed to delete lesson.")
                                            }
                                        } else {
                                            // Local draft, just remove from list
                                            // Since id is null, we might need another way to identify, 
                                            // but for now let's assume we can filter by reference if they are the same object.
                                            lessons = lessons.filterIndexed { i, _ -> i != index }
                                        }
                                    }
                                }
                            )
                            
                            if (index < lessons.lastIndex) {
                                BufferIndicator("30 min buffer")
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }

                    // Bottom Actions
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        tonalElevation = 2.dp,
                        shadowElevation = 8.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .navigationBarsPadding()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        if (LessonRepository.saveDraftLessons(lessons)) {
                                            snackbarHostState.showSnackbar("Draft saved.")
                                            loadData() // Reload to get fresh IDs from DB
                                        } else {
                                            snackbarHostState.showSnackbar("Failed to save draft.")
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                contentPadding = PaddingValues(12.dp)
                            ) {
                                Text("Save Draft", fontWeight = FontWeight.Bold)
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    scope.launch {
                                        val instructor = InstructorRepository.getCurrentInstructor()
                                        if (instructor != null) {
                                            // First save any unsaved local changes as drafts
                                            if (LessonRepository.saveDraftLessons(lessons)) {
                                                if (LessonRepository.publishLessonsForDate(instructor.id, tomorrow)) {
                                                    // TODO: trigger notification
                                                    snackbarHostState.showSnackbar("Schedule published successfully.")
                                                    loadData() // Refresh UI
                                                } else {
                                                    snackbarHostState.showSnackbar("Failed to publish schedule.")
                                                }
                                            } else {
                                                snackbarHostState.showSnackbar("Failed to save state before publishing.")
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = CircleShape,
                                contentPadding = PaddingValues(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C22BD)),
                                enabled = lessons.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Publish & Send Notifications (${lessons.size} Classes)", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BufferIndicator(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
        Text(
            text = "\u2014 $text \u2014",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
        )
        HorizontalDivider(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
    }
}
