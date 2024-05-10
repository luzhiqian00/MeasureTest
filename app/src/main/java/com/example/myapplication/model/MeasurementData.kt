package com.example.myapplication.model

import com.example.myapplication.model.dataPointModel.DataPoint
import com.google.gson.annotations.SerializedName

data class MeasurementData(
    var measurementId: String?,
    var userEmail: String?,
    var timestamp: Long?,
    var description: String?,
    var dataPoints: List<DataPoint>?
)


class DataPointData{
    var value: String?=null
    var dataPointId: Int? = null
}

data class MeasurementsResponse(
    @SerializedName("measurements") val measurements: List<MeasurementData>
)