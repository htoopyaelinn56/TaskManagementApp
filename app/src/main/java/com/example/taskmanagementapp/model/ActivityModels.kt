package com.example.taskmanagementapp.model

import java.util.Locale

data class ActivityEntry(
    val type: String,
    val durationMinutes: Int,
    val metric: Metric?,
    val date: String,
    val caloriesKcal: Int,
    val location: String
)

data class Metric(
    val value: Double,
    val unit: String
)

object ActivityTypes {
    const val RUN = "Run"
    const val WALK = "Walk"
    const val CYCLING = "Cycling"
    const val SWIM = "Swim"
    const val HIIT = "HIIT"
    const val YOGA = "Yoga"
    const val PILATES = "Pilates"
    const val WEIGHTLIFTING = "Weightlifting"
    const val ROWING = "Rowing"
    const val ELLIPTICAL = "Elliptical"
    const val BASKETBALL = "Basketball"
    const val SOCCER = "Soccer"
    const val TENNIS = "Tennis"
    const val HIKE = "Hike"
    const val STAIR_CLIMB = "Stair Climb"
    const val JUMP_ROPE = "Jump Rope"
    const val CORE = "Core"
    const val STRETCHING = "Stretching"
}

val ActivityTypeList = listOf(
    ActivityTypes.RUN,
    ActivityTypes.WALK,
    ActivityTypes.CYCLING,
    ActivityTypes.SWIM,
    ActivityTypes.HIIT,
    ActivityTypes.YOGA,
    ActivityTypes.PILATES,
    ActivityTypes.WEIGHTLIFTING,
    ActivityTypes.ROWING,
    ActivityTypes.ELLIPTICAL,
    ActivityTypes.BASKETBALL,
    ActivityTypes.SOCCER,
    ActivityTypes.TENNIS,
    ActivityTypes.HIKE,
    ActivityTypes.STAIR_CLIMB
)

fun formatMetric(metric: Metric?): String? {
    if (metric == null) return null
    val formattedValue = if (metric.value % 1.0 == 0.0) {
        metric.value.toInt().toString()
    } else {
        String.format(Locale.getDefault(), "%.1f", metric.value)
    }
    return "$formattedValue ${metric.unit}"
}

