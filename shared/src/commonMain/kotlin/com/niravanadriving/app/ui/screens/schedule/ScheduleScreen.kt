package com.niravanadriving.app.ui.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.niravanadriving.app.data.repository.MockDataRepository
import com.niravanadriving.app.ui.components.ScheduleItemCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen() {
    val tomorrowSchedule = MockDataRepository.getTomorrowSchedule()

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
                            text = "Tomorrow, Oct 26",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Button(
                        onClick = { /* Auto-fill */ },
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
                            text = "Auto-Fill from Today's Schedule",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                itemsIndexed(tomorrowSchedule) { index, lesson ->
                    ScheduleItemCard(
                        lesson = lesson,
                        onRemove = { /* Remove */ }
                    )
                    
                    if (index < tomorrowSchedule.lastIndex) {
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
                        onClick = { /* Save Draft */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Text("Save Draft", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { /* Publish */ },
                        modifier = Modifier.fillMaxWidth(),
                        shape = androidx.compose.foundation.shape.CircleShape,
                        contentPadding = PaddingValues(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4C22BD)) // Deep primary from design
                    ) {
                        Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publish & Send Notifications (5 Classes)", fontWeight = FontWeight.Bold)
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
