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

val MetricUnitList = listOf(
    "km",
    "m",
    "mi",
    "yd",
    "ft",
    "steps",
    "reps",
    "sets",
    "kg",
    "lb",
    "oz",
    "laps",
    "floors"
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

data class Goal(
    val name: String,
    val activityType: String,
    val targetMetric: Metric?,
    val deadline: String,
    val notes: String?
)

fun sampleActivities(): List<ActivityEntry> {
    return listOf(
        // Today - May 6, 2026
        ActivityEntry(ActivityTypes.RUN, 22, Metric(3.4, "km"), "2026-05-06", 210, "City Park"),
        ActivityEntry(ActivityTypes.CORE, 305, null, "2026-05-06", 100, "Home"),

        // May 5, 2026
        ActivityEntry(ActivityTypes.CYCLING, 38, Metric(12.1, "km"), "2026-05-05", 360, "Riverside Trail"),

        // May 4, 2026
        ActivityEntry(ActivityTypes.YOGA, 30, null, "2026-05-04", 120, "Studio A"),
        ActivityEntry(ActivityTypes.WALK, 28, Metric(2.2, "km"), "2026-05-04", 140, "Neighborhood"),
        ActivityEntry(ActivityTypes.WEIGHTLIFTING, 45, null, "2026-05-04", 280, "Iron Gym"),

        // May 3, 2026
        ActivityEntry(ActivityTypes.SWIM, 35, Metric(1.0, "km"), "2026-05-03", 300, "Community Pool"),
        ActivityEntry(ActivityTypes.HIKE, 62, Metric(5.6, "km"), "2026-05-03", 520, "Pine Trail"),

        // May 2, 2026
        ActivityEntry(ActivityTypes.ROWING, 25, Metric(4.0, "km"), "2026-05-02", 260, "River Dock"),
        ActivityEntry(ActivityTypes.HIIT, 20, null, "2026-05-02", 240, "Home"),
        ActivityEntry(ActivityTypes.STRETCHING, 10, null, "2026-05-02", 40, "Home"),

        // May 1, 2026
        ActivityEntry(ActivityTypes.PILATES, 40, null, "2026-05-01", 190, "Studio B"),
        ActivityEntry(ActivityTypes.ELLIPTICAL, 30, Metric(5.0, "km"), "2026-05-01", 280, "Fitness Center"),

        // April 30, 2026
        ActivityEntry(ActivityTypes.BASKETBALL, 50, null, "2026-04-30", 420, "Community Court"),
        ActivityEntry(ActivityTypes.SOCCER, 70, null, "2026-04-30", 560, "East Field"),

        // April 29, 2026
        ActivityEntry(ActivityTypes.TENNIS, 55, null, "2026-04-29", 410, "West Courts"),
        ActivityEntry(ActivityTypes.STAIR_CLIMB, 18, Metric(45.0, "floors"), "2026-04-29", 200, "Office Tower"),
        ActivityEntry(ActivityTypes.JUMP_ROPE, 10, null, "2026-04-29", 110, "Home")
    )
}
