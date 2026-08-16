package com.niravanadriving.app.ui.screens.learner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.niravanadriving.app.data.models.*
import com.niravanadriving.app.data.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLearnerScreen(onBack: () -> Unit) {
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Basic Info State
    var fullName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }

    // Package Details State
    var totalClasses by remember { mutableStateOf("15") }
    var startDate by remember { mutableStateOf("2023-10-24") } // Default date
    var pickupAddress by remember { mutableStateOf("") }

    // Financial Details State
    var totalFee by remember { mutableStateOf("") }
    var advanceReceived by remember { mutableStateOf("") }
    var isPaymentReceivedToday by remember { mutableStateOf(true) }

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Learner", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Section: Basic Information
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                
                // Photo Upload Placeholder
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier.size(100.dp).clickable { /* Gallery Picker */ },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Upload", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = emergencyContact,
                    onValueChange = { emergencyContact = it },
                    label = { Text("Emergency Contact") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )
            }

            // Section: Driving Package Details
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Driving Package Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = totalClasses,
                    onValueChange = { totalClasses = it },
                    label = { Text("Total Classes") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) }
                )

                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Start Date (yyyy-mm-dd)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Icon(Icons.Default.DateRange, null) },
                    readOnly = true
                )

                OutlinedTextField(
                    value = pickupAddress,
                    onValueChange = { pickupAddress = it },
                    label = { Text("Pickup Address") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    trailingIcon = { Icon(Icons.Default.MyLocation, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }

            // Section: Financial Details
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("Financial Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                OutlinedTextField(
                    value = totalFee,
                    onValueChange = { totalFee = it },
                    label = { Text("Total Package Fee") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("₹ ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = advanceReceived,
                    onValueChange = { advanceReceived = it },
                    label = { Text("Advance Received") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    prefix = { Text("₹ ") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Payment Received Today", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isPaymentReceivedToday, onCheckedChange = { isPaymentReceivedToday = it })
                }
            }

            if (errorMessage != null) {
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val currentUserId = supabase.auth.currentUserOrNull()?.id
                            if (currentUserId == null) {
                                errorMessage = "User not authenticated"
                                isLoading = false
                                return@launch
                            }

                            // Fetch Instructor ID by auth_user_id
                            val instructor = try {
                                supabase.postgrest["instructors"]
                                    .select {
                                        filter {
                                            eq("auth_user_id", currentUserId)
                                        }
                                    }
                                    .decodeSingle<Instructor>()
                            } catch (e: Exception) {
                                // If not found, show error
                                errorMessage = "Instructor profile not found"
                                isLoading = false
                                return@launch
                            }
                            
                            val student = Student(
                                instructorId = instructor.id,
                                fullName = fullName,
                                phone = phoneNumber,
                                address = pickupAddress,
                                totalSessions = totalClasses.toIntOrNull() ?: 15,
                                feePerSession = (totalFee.toDoubleOrNull() ?: 0.0) / (totalClasses.toDoubleOrNull() ?: 15.0),
                                instructorRemarks = "Emergency: $emergencyContact",
                                isActive = true
                            )

                            // Save student and get the inserted object (including generated ID)
                            val insertedStudent = try {
                                supabase.postgrest["students"]
                                    .insert(student) {
                                        select()
                                    }
                                    .decodeSingle<Student>()
                            } catch (e: Exception) {
                                errorMessage = "Failed to create learner: ${e.message}"
                                isLoading = false
                                return@launch
                            }
                            
                            // If advance received, record a payment
                            val advance = advanceReceived.toDoubleOrNull() ?: 0.0
                            if (advance > 0 && insertedStudent.id != null) {
                                try {
                                    val payment = Payment(
                                        studentId = insertedStudent.id,
                                        instructorId = instructor.id,
                                        amount = advance,
                                        paymentMethod = PaymentMethod.CASH, // Default to cash for now
                                        status = PaymentStatus.PAID,
                                        paidAt = "2023-10-24T10:00:00Z", // Placeholder
                                        notes = "Initial Advance",
                                        lessonId = null // This is the problematic field in your DB schema
                                    )
                                    supabase.postgrest["payments"].insert(payment)
                                } catch (e: Exception) {
                                    // We log this but allow the student creation to succeed
                                    println("Warning: Could not save payment record: ${e.message}")
                                }
                            }
                            
                            isLoading = false
                            onBack()
                        } catch (e: Exception) {
                            isLoading = false
                            errorMessage = "Error: ${e.message}"
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = CircleShape,
                contentPadding = PaddingValues(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C22BD)),
                enabled = !isLoading && fullName.isNotBlank() && phoneNumber.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Save & Create Learner", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
