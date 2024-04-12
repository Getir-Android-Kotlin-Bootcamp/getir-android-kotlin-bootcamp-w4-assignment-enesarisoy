package com.ns.myapplication.model

data class Profile(
    val country: String?,
    val email: String,
    val employer: String?,
    val fullName: String,
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val occupation: String?,
    val password: String,
    val phoneNumber: String?,
    val userId: String
)