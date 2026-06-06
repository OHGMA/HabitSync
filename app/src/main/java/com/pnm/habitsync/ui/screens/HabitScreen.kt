package com.pnm.habitsync.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pnm.habitsync.data.model.Habit
import com.pnm.habitsync.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitsScreen(
    viewModel: HabitViewModel = viewModel(),
    onCreateHabitClick: () -> Unit // Callback to open the Create screen
) {
    // 1. Observe the data from the ViewModel
    val habits by viewModel.habits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateHabitClick,
                containerColor = Color(0xFF10B981), // Green brand color
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Header
            Text("My Habits", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("${habits.size} active habits", color = Color.Gray, fontSize = 14.sp)

            Spacer(modifier = Modifier.height(24.dp))

            // UI States: Loading, Empty, or List
            if (isLoading && habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF10B981))
                }
            } else if (habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No habits yet. Click the + button to start!", color = Color.Gray)
                }
            } else {
                // 2. The LazyColumn for high-performance scrolling
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 80.dp) // Leave space so the FAB doesn't cover the last item
                ) {
                    // "items" loops through our list of data automatically
                    items(habits) { habit ->
                        HabitItem(habit = habit, onComplete = { viewModel.completeHabit(habit) })
                    }
                }
            }
        }
    }
}

// 3. A reusable Composable for a single habit row
// Updated to accept the onComplete callback
@Composable
fun HabitItem(habit: Habit, onComplete: () -> Unit) {
    // Check if it's done today
    val isCompletedToday = habit.lastCompletedDate == com.pnm.habitsync.utils.DateUtils.getTodayString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            // Make the whole row clickable!
            .clickable(enabled = !isCompletedToday) { onComplete() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Change icon based on status
        if (isCompletedToday) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = Color(0xFF10B981), // Green
                modifier = Modifier.size(32.dp)
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.Circle,
                contentDescription = "Uncompleted",
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                // Optional: Strikethrough if completed
                textDecoration = if (isCompletedToday) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                color = if (isCompletedToday) Color.Gray else Color.Black
            )
            Text(habit.category, color = Color.Gray, fontSize = 14.sp)
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFFFF7ED))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔥 ${habit.streakCount}",
                color = Color(0xFFF97316),
                fontWeight = FontWeight.Bold
            )
        }
    }
}