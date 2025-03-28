package com.example.usageinsight.util

import java.time.LocalDate
import java.time.ZoneId

fun getTodayStartMillis(): Long {
    return LocalDate.now()
        .atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}
