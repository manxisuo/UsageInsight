package com.example.usageinsight.repository

import android.content.Context
import com.example.usageinsight.data.dao.FitnessDao
import com.example.usageinsight.model.FitnessData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.Flow

class FitnessRepository(
    private val context: Context,
    private val fitnessDao: FitnessDao
) {
    private val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_HEART_RATE_BPM, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .build()

    fun getFitnessDataBetween(startTime: Long, endTime: Long): Flow<List<FitnessData>> {
        return fitnessDao.getFitnessDataBetween(startTime, endTime)
    }

    suspend fun syncFitnessData() {
        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)
        
        val endTime = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)

        val readRequest = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .read(DataType.TYPE_DISTANCE_DELTA)
            .read(DataType.TYPE_HEART_RATE_BPM)
            .read(DataType.TYPE_CALORIES_EXPENDED)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        try {
            val response = Fitness.getHistoryClient(context, account)
                .readData(readRequest)
                .await()

            // 处理响应数据
            val fitnessData = processFitnessData(response)
            fitnessDao.insert(fitnessData)
        } catch (e: Exception) {
            // 处理错误
        }
    }

    private fun processFitnessData(response: DataReadResponse): FitnessData {
        var steps = 0
        var distance = 0f
        var heartRate: Float? = null
        var calories: Float? = null

        // 处理步数数据
        response.getDataSet(DataType.TYPE_STEP_COUNT_DELTA)?.let { dataSet ->
            for (dp in dataSet.dataPoints) {
                steps += dp.getValue(DataType.TYPE_STEP_COUNT_DELTA.fields[0]).asInt()
            }
        }

        // 处理距离数据
        response.getDataSet(DataType.TYPE_DISTANCE_DELTA)?.let { dataSet ->
            for (dp in dataSet.dataPoints) {
                distance += dp.getValue(DataType.TYPE_DISTANCE_DELTA.fields[0]).asFloat()
            }
        }

        // 处理心率数据
        response.getDataSet(DataType.TYPE_HEART_RATE_BPM)?.let { dataSet ->
            dataSet.dataPoints.lastOrNull()?.let { dp ->
                heartRate = dp.getValue(DataType.TYPE_HEART_RATE_BPM.fields[0]).asFloat()
            }
        }

        // 处理卡路里数据
        response.getDataSet(DataType.TYPE_CALORIES_EXPENDED)?.let { dataSet ->
            for (dp in dataSet.dataPoints) {
                calories = (calories ?: 0f) + dp.getValue(DataType.TYPE_CALORIES_EXPENDED.fields[0]).asFloat()
            }
        }

        return FitnessData(
            steps = steps,
            distance = distance,
            heartRate = heartRate,
            calories = calories,
            timestamp = System.currentTimeMillis()
        )
    }
} 