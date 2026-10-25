package com.example.mygameslibapk.laravelclient

import com.google.gson.annotations.SerializedName

data class SkillDto(
    val id: Int,
    val title: String
)

data class HirePositionDto(
    val id: Int,
    @SerializedName("project_id") val projectId: Int,
    val title: String,
    val description: String,
    @SerializedName("tickets_amount") val ticketsAmount: Int,
    @SerializedName("is_open") val isOpen: Boolean,
    val skills: List<SkillDto> = emptyList(),
    val tickets: List<PositionTicketDto> = emptyList()
)

data class PositionTicketDto(
    val id: Int,
    @SerializedName("position_id") val positionId: Int,
    @SerializedName("user_id") val userId: Int,
    val reason: String,
    val explanation: String?,
    val success: Boolean?,
    val expired: Boolean,
    val user: UserDto? = null,
    val position: HirePositionDto? = null
)

data class RequestMoreTicketsRequest(
    @SerializedName("hire_position_id") val hirePositionId: Int,
    val quantity: Int,
    val reason: String
)

data class RequestPositionSlotsRequest(
    val title: String,
    val quantity: Int,
    val reason: String
)

data class CreatePositionRequest(
    val title: String,
    val description: String,
    @SerializedName("tickets_amount") val ticketsAmount: Int,
    val skills: List<String> = emptyList()
)

data class ApplyTicketRequest(
    val reason: String
)

data class ReviewTicketRequest(
    val action: String, // "accept" or "decline"
    val reason: String
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String?,
    val positions: List<HirePositionDto>?,
    val tickets: List<PositionTicketDto>?
)