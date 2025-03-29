package com.example.usageinsight.repository

import com.example.usageinsight.model.DailyUsageSummary
import kotlinx.coroutines.flow.Flow

interface AnalysisRepository {
    suspend fun generateDailyAnalysis(summary: DailyUsageSummary): String
}
