package com.niravanadriving.app.ui.screens.learner

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.niravanadriving.app.data.models.Student
import com.niravanadriving.app.data.repository.InstructorRepository
import com.niravanadriving.app.data.repository.StudentRepository
import com.niravanadriving.app.ui.components.LearnerCard
import com.niravanadriving.app.ui.util.UiState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnerScreen(onAddLearner: () -> Unit) {
    var uiState by remember { mutableStateOf<UiState<List<Student>>>(UiState.Loading) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Active") }
    
    val scope = rememberCoroutineScope()

    fun loadData() {
        uiState = UiState.Loading
        scope.launch {
            try {
                val instructor = InstructorRepository.getCurrentInstructor()
                if (instructor == null) {
                    uiState = UiState.Error("Instructor not found")
                    return@launch
                }
                val students = StudentRepository.getAllStudents(instructor.id)
                uiState = UiState.Success(students)
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Error loading learners")
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
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) { /* Avatar */ }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Learners",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            if (uiState is UiState.Success) {
                                Text(
                                    text = "Total: ${(uiState as UiState.Success).data.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddLearner,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Learner") },
                containerColor = Color(0xFF4C22BD),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
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
                val allStudents = state.data
                
                // Dynamic Filter Calculations
                val activeCount = allStudents.count { it.isActive }
                val pendingCount = allStudents.count { it.balance > 0 }
                val completedCount = allStudents.count { it.sessionsCompleted >= (it.totalSessions ?: 15) }

                val filters = listOf(
                    "Active ($activeCount)",
                    "Payment Pending ($pendingCount)",
                    "Completed ($completedCount)"
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search by name, phone, or ID...", style = MaterialTheme.typography.bodyMedium) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        shape = CircleShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        singleLine = true
                    )
                    
                    // Filters
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filters.forEach { filter ->
                            val filterName = filter.substringBefore(" (")
                            val isSelected = selectedFilter == filterName
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filterName },
                                label = { Text(filter) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White,
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = null,
                                shape = CircleShape
                            )
                        }
                    }
                    
                    // Learner List
                    val filteredStudents = allStudents.filter { student ->
                        val matchesSearch = student.fullName.contains(searchQuery, ignoreCase = true)
                        val matchesFilter = when (selectedFilter) {
                            "Active" -> student.isActive
                            "Payment Pending" -> student.balance > 0
                            "Completed" -> student.sessionsCompleted >= (student.totalSessions ?: 15)
                            else -> true
                        }
                        matchesSearch && matchesFilter
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredStudents) { student ->
                            LearnerCard(
                                student = student,
                                onCall = { /* Call */ },
                                onMessage = { /* Message */ },
                                onLocation = { /* Location */ }
                            )
                        }
                    }
                }
            }
        }
    }
}
