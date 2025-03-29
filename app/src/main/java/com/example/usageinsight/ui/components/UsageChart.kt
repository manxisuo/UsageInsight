package com.example.usageinsight.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun UsagePieChart(
    appUsageList: List<Pair<String, Long>>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            PieChart(context).apply {
                description.isEnabled = false
                setHoleRadius(40f)
                setTransparentCircleRadius(45f)
                legend.isEnabled = true
            }
        },
        update = { chart ->
            val entries = appUsageList.map { (appName, timeInMs) ->
                PieEntry(timeInMs.toFloat(), appName)
            }
            
            val dataSet = PieDataSet(entries, "应用使用时长").apply {
                colors = ColorTemplate.MATERIAL_COLORS.toList()
                valueTextSize = 14f
            }
            
            chart.data = PieData(dataSet)
            chart.invalidate()
        }
    )
} 