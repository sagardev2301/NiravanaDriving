package com.niravanadriving.app.ui.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditLessonScreen(
    viewModel: ScheduleViewModel,
    lessonId: String?,
    onDone: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    var students by remember { mutableStateOf<List<Student>>(emptyList()) }
    var vehicles by remember { mutableStateOf<List<Vehicle>>(emptyList()) }
    var isLoadingData by remember { mutableStateOf(true) }

    // Form State
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedVehicle by remember { mutableStateOf<Vehicle?>(null) }
    var scheduledTime by remember { mutableStateOf("") } // HH:mm
    var pickupLocation by remember { mutableStateOf("") }
    var durationMinutes by remember { mutableStateOf(60) }

    // Error State
    var studentError by remember { mutableStateOf(false) }
    var vehicleError by remember { mutableStateOf(false) }
    var timeError by remember { mutableStateOf(false) }
    var pickupError by remember { mutableStateOf(false) }

    val existingLesson = remember(lessonId) { viewModel.getLessonById(lessonId) }

    LaunchedEffect(Unit) {
        try {
            val instructor = InstructorRepository.getCurrentInstructor()
            if (instructor != null) {
                students = StudentRepository.getAllStudents(instructor.id)
                vehicles = VehicleRepository.getVehicles(instructor.id)
                
                if (lessonId != null && existingLesson != null) {
                    selectedStudent = students.find { it.id == existingLesson.studentId }
                    selectedVehicle = vehicles.find { it.id == existingLesson.vehicleId }
                    scheduledTime = existingLesson.scheduledTime
                    pickupLocation = existingLesson.pickupLocation ?: ""
                    durationMinutes = existingLesson.durationMinutes
                }
            }
        } catch (e: Exception) {
            snackbarHostState.showSnackbar("Failed to load data")
        } finally {
            isLoadingData = false
        }
    }

    if (lessonId != null && existingLesson == null && !isLoadingData) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Lesson not found")
                Button(onClick = onDone) { Text("Back") }
            }
        }
        return
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (lessonId == null) "Add Lesson" else "Edit Lesson") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoadingData) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(scrollState)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Student Dropdown
                DropdownSelector(
                    label = "Select Student",
                    options = students,
                    selectedOption = selectedStudent,
                    onOptionSelected = { 
                        selectedStudent = it
                        studentError = false
                    },
                    optionLabel = { it.fullName },
                    isError = studentError,
                    supportingText = if (studentError) "Please select a student" else null
                )

                // Vehicle Dropdown
                DropdownSelector(
                    label = "Select Vehicle",
                    options = vehicles,
                    selectedOption = selectedVehicle,
                    onOptionSelected = { 
                        selectedVehicle = it
                        vehicleError = false
                    },
                    optionLabel = { it.makeModel },
                    isError = vehicleError,
                    supportingText = if (vehicleError) "Please select a vehicle" else null
                )

                // Time Input
                OutlinedTextField(
                    value = scheduledTime,
                    onValueChange = { 
                        scheduledTime = it
                        timeError = false
                    },
                    label = { Text("Time (HH:mm)") },
                    placeholder = { Text("e.g. 09:30") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Icon(Icons.Default.AccessTime, null) },
                    isError = timeError,
                    supportingText = if (timeError) { { Text("Enter a valid time (HH:mm)") } } else null,
                    singleLine = true
                )

                // Duration Dropdown
                val durations = listOf(30, 45, 60, 90)
                DropdownSelector(
                    label = "Duration (Minutes)",
                    options = durations,
                    selectedOption = durationMinutes,
                    onOptionSelected = { durationMinutes = it },
                    optionLabel = { "$it mins" }
                )

                // Pickup Location
                OutlinedTextField(
                    value = pickupLocation,
                    onValueChange = { 
                        pickupLocation = it
                        pickupError = false
                    },
                    label = { Text("Pickup Location") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    isError = pickupError,
                    supportingText = if (pickupError) { { Text("Pickup location is required") } } else null,
                    singleLine = true
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val isValid = validate(
                            selectedStudent = selectedStudent,
                            selectedVehicle = selectedVehicle,
                            time = scheduledTime,
                            pickup = pickupLocation,
                            setStudentError = { studentError = it },
                            setVehicleError = { vehicleError = it },
                            setTimeError = { timeError = it },
                            setPickupError = { pickupError = it }
                        )

                        if (isValid) {
                            val lesson = (existingLesson ?: Lesson(
                                instructorId = selectedStudent!!.instructorId,
                                studentId = selectedStudent!!.id!!,
                                scheduledDate = "", 
                                scheduledTime = scheduledTime,
                                durationMinutes = durationMinutes,
                                status = LessonStatus.SCHEDULED,
                                vehicleId = selectedVehicle!!.id,
                                pickupLocation = pickupLocation
                            )).copy(
                                studentId = selectedStudent!!.id!!,
                                scheduledTime = scheduledTime,
                                durationMinutes = durationMinutes,
                                vehicleId = selectedVehicle!!.id,
                                pickupLocation = pickupLocation,
                                student = selectedStudent,
                                vehicle = selectedVehicle
                            )

                            if (lesson.id != null) {
                                viewModel.updateLesson(lesson) { success ->
                                    if (success) onDone()
                                    else scope.launch { snackbarHostState.showSnackbar("Failed to update lesson") }
                                }
                            } else {
                                viewModel.addLesson(lesson)
                                onDone()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    Text("Save Lesson", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownSelector(
    label: String,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedOption?.let { optionLabel(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            isError = isError,
            supportingText = supportingText?.let { { Text(it) } }
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

private fun validate(
    selectedStudent: Student?,
    selectedVehicle: Vehicle?,
    time: String,
    pickup: String,
    setStudentError: (Boolean) -> Unit,
    setVehicleError: (Boolean) -> Unit,
    setTimeError: (Boolean) -> Unit,
    setPickupError: (Boolean) -> Unit
): Boolean {
    var isValid = true
    if (selectedStudent == null) {
        setStudentError(true)
        isValid = false
    }
    if (selectedVehicle == null) {
        setVehicleError(true)
        isValid = false
    }
    if (time.isBlank() || !time.contains(":")) {
        setTimeError(true)
        isValid = false
    }
    if (pickup.isBlank()) {
        setPickupError(true)
        isValid = false
    }
    return isValid
}
