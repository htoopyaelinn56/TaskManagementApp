package com.example.taskmanagementapp.model

// Sealed class representing items that can appear in mixed lists (activities and goals)
sealed class ListItem {
    data class ActivityItem(val activity: ActivityEntry) : ListItem()
    data class GoalItem(val goal: Goal) : ListItem()
}

