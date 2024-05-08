package com.example.myapplication.model

import com.example.myapplication.model.dataPointModel.DataPoint

class MeasurementData(
    measurementId: String?,
    userEmail: String?,
    timestamp: Long?,
    description: String?,
    dataPoints: List<DataPoint>?
) {
    var measurementId: String? = measurementId
    var userEmail: String? = userEmail
    var timestamp: Long? = timestamp
    var description: String? = description
    var dataPoints: List<DataPoint>? = dataPoints
    // Additional constructors or methods can be added here if needed
}


class DataPointData{
    var value: String?=null
    var dataPointId: Int? = null
}