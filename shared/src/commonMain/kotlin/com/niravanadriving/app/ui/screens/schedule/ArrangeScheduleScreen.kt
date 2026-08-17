package com.niravanadriving.app.ui.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niravanadriving.app.data.models.Lesson
import com.niravanadriving.app.data.models.LessonStatus
import com.niravanadriving.app.data.models.Student
import com.niravanadriving.app.data.models.Vehicle
import com.niravanadriving.app.data.repository.InstructorRepository
import com.niravanadriving.app.data.repository.StudentRepository
import com.niravanadriving.app.data.repository.VehicleRepository
import com.niravanadriving.app.ui.viewmodel.ScheduleViewModel
import com.niravanadriving.app.util.DateTimeUtils
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArrangeScheduleScreen(
    viewModel: ScheduleViewModel,
    studentIds: List<String>,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val selectedDate by viewModel.selectedDate.collectAsState()
    
    var draftLessonsList by remember { mutableStateOf<List<Lesson>>(emptyList()) }
    var vehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val instructor = InstructorRepository.getCurrentInstructor()
        if (instructor != null) {
            vehicles = VehicleRepository.getVehicles(instructor.id)
            val allStudents = StudentRepository.getAllStudents(instructor.id)
            // Preserve selection order
            val selectedStudents = studentIds.mapNotNull { id -> allStudents.find { it.id == id } }
            
            var currentTime = "08:00"
            val defaultDuration = 45
            val buffer = 15
            
            val initialDrafts = selectedStudents.map { student ->
                val lesson = Lesson(
                    instructorId = instructor.id,
                    studentId = student.id!!,
                    scheduledDate = selectedDate.toString(),
                    scheduledTime = currentTime,
                    durationMinutes = defaultDuration,
                    status = LessonStatus.SCHEDULED,
                    pickupLocation = student.address ?: "",
                    vehicleId = vehicles.firstOrNull()?.id,
                    isDraft = true,
                    student = student,
                    vehicle = vehicles.firstOrNull()
                )
                
                currentTime = calculateNextStartTime(currentTime, defaultDuration, buffer)
                lesson
            }
            draftLessonsList = initialDrafts
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Arrange Schedule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Step 2: Set sequence and timings", style = MaterialTheme.typography.bodySmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Help Tooltip */ }) {
                        Icon(Icons.Default.HelpOutline, contentDescription = "Help")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                tonalElevation = 8.dp,
                shadowElevation = 16.dp
            ) {
                Column(modifier = Modifier.padding(16.dp).navigationBarsPadding()) {
                    Button(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                draftLessonsList.forEach { viewModel.addLesson(it) }
                                viewModel.publish { success ->
                                    isSaving = false
                                    if (success) onSuccess()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving,
                        contentPadding = PaddingValues(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Publish & Send Notifications", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            isSaving = true
                            scope.launch {
                                draftLessonsList.forEach { viewModel.addLesson(it) }
                                viewModel.saveDraft { success ->
                                    isSaving = false
                                    if (success) onSuccess()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving,
                        contentPadding = PaddingValues(16.dp)
                    ) {
                        Text("Save Draft", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                itemsIndexed(draftLessonsList, key = { _, lesson -> lesson.studentId }) { index, lesson ->
                    DraftLessonCard(
                        lesson = lesson,
                        vehicles = vehicles,
                        onTimeChange = { newTime ->
                            val newList = draftLessonsList.toMutableList()
                            newList[index] = newList[index].copy(scheduledTime = newTime)
                            draftLessonsList = newList
                        },
                        onVehicleChange = { newVehicle ->
                            val newList = draftLessonsList.toMutableList()
                            newList[index] = newList[index].copy(
                                vehicleId = newVehicle.id,
                                vehicle = newVehicle
                            )
                            draftLessonsList = newList
                        },
                        onPickupChange = { newPickup ->
                            val newList = draftLessonsList.toMutableList()
                            newList[index] = newList[index].copy(pickupLocation = newPickup)
                            draftLessonsList = newList
                        },
                        onMoveUp = if (index > 0) { { 
                            val newList = draftLessonsList.toMutableList()
                            val current = newList.removeAt(index)
                            newList.add(index - 1, current)
                            draftLessonsList = newList
                        } } else null,
                        onMoveDown = if (index < draftLessonsList.lastIndex) { { 
                            val newList = draftLessonsList.toMutableList()
                            val current = newList.removeAt(index)
                            newList.add(index + 1, current)
                            draftLessonsList = newList
                        } } else null
                    )
                    
                    if (index < draftLessonsList.lastIndex) {
                        GapIndicator(
                            currentLesson = draftLessonsList[index],
                            nextLesson = draftLessonsList[index + 1]
                        )
                    }
                }
                item { Spacer(modifier = Modifier.height(120.dp)) }
            }
        }
    }
}

@Composable
fun DraftLessonCard(
    lesson: Lesson,
    vehicles: List<Vehicle>,
    onTimeChange: (String) -> Unit,
    onVehicleChange: (Vehicle) -> Unit,
    onPickupChange: (String) -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Drag handle placeholder + Move buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.DragHandle, contentDescription = null, tint = Color.Gray.copy(alpha = 0.5f))
                    Row {
                        IconButton(onClick = { onMoveUp?.invoke() }, enabled = onMoveUp != null, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up")
                        }
                        IconButton(onClick = { onMoveDown?.invoke() }, enabled = onMoveDown != null, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down")
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(lesson.student?.fullName?.take(1)?.uppercase() ?: "?")
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(lesson.student?.fullName ?: "Unknown", fontWeight = FontWeight.Bold)
                    Text(
                        text = "${lesson.student?.licenseType?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Manual"} Course",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Time Chip
                var showTimePicker by remember { mutableStateOf(false) }
                Surface(
                    onClick = { showTimePicker = true },
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AccessTime, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(DateTimeUtils.formatTime(lesson.scheduledTime), style = MaterialTheme.typography.labelLarge)
                        Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                    }
                }
                
                if (showTimePicker) {
                    TimePickerFallback(
                        initialTime = lesson.scheduledTime,
                        onDismiss = { showTimePicker = false },
                        onConfirm = { 
                            onTimeChange(it)
                            showTimePicker = false
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pickup Location
            OutlinedTextField(
                value = lesson.pickupLocation ?: "",
                onValueChange = onPickupChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Pickup location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Vehicle Chip
            var showVehiclePicker by remember { mutableStateOf(false) }
            Surface(
                onClick = { showVehiclePicker = true },
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(lesson.vehicle?.makeModel ?: "Select Vehicle", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            if (showVehiclePicker) {
                VehiclePickerDialog(
                    vehicles = vehicles,
                    onDismiss = { showVehiclePicker = false },
                    onSelect = {
                        onVehicleChange(it)
                        showVehiclePicker = false
                    }
                )
            }
        }
    }
}

@Composable
fun GapIndicator(currentLesson: Lesson, nextLesson: Lesson) {
    val currentEndMinutes = timeToMinutes(currentLesson.scheduledTime) + currentLesson.durationMinutes
    val nextStartMinutes = timeToMinutes(nextLesson.scheduledTime)
    val gap = nextStartMinutes - currentEndMinutes

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Vertical line
        Box(modifier = Modifier.width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.outlineVariant))

        if (gap < 15) {
            val message = if (gap < 0) "Lessons overlap" else "Overlap detected: Previous lesson ends at ${DateTimeUtils.formatTime(minutesToTime(currentEndMinutes))}"
            Surface(
                color = Color(0xFFFFF7E6), // Light Caution color
                shape = RoundedCornerShape(8.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2A900))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF2A900), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(message, style = MaterialTheme.typography.labelSmall, color = Color(0xFF874D00))
                }
            }
        } else {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "\u23F1 Suggested: ${gap}m buffer",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Box(modifier = Modifier.width(1.dp).height(16.dp).background(MaterialTheme.colorScheme.outlineVariant))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerFallback(
    initialTime: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var time by remember { mutableStateOf(initialTime) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Time (HH:mm)") },
        text = {
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                placeholder = { Text("08:00") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(time) }) { Text("Confirm") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun VehiclePickerDialog(
    vehicles: List<Vehicle>,
    onDismiss: () -> Unit,
    onSelect: (Vehicle) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Vehicle") },
        text = {
            Column {
                vehicles.forEach { vehicle ->
                    ListItem(
                        headlineContent = { Text(vehicle.makeModel) },
                        supportingContent = { Text(vehicle.registrationNumber) },
                        modifier = Modifier.clickable { onSelect(vehicle) }
                    )
                }
            }
        },
        confirmButton = {
             TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

private fun calculateNextStartTime(currentStart: String, duration: Int, buffer: Int): String {
    val totalMinutes = timeToMinutes(currentStart) + duration + buffer
    return minutesToTime(totalMinutes)
}

private fun timeToMinutes(time: String): Int {
    val parts = time.split(":")
    if (parts.size < 2) return 0
    val hours = parts[0].toIntOrNull() ?: 0
    val minutes = parts[1].toIntOrNull() ?: 0
    return hours * 60 + minutes
}

private fun minutesToTime(minutes: Int): String {
    val h = (minutes / 60) % 24
    val m = minutes % 60
    return "${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}"
}
