package com.example.mygameslibapk.laravelclient

import com.google.gson.annotations.SerializedName

data class ProjectDto(
    val id: Int,
    val title: String,
    val content: String,
    @SerializedName("is_public") val isPublic: Boolean = false,
    @SerializedName("is_approved") val isApproved: Boolean = false,
    @SerializedName("icon_big") val iconBig: String? = null,
    @SerializedName("icon_small") val iconSmall: String? = null,
    val owners: List<UserDto> = emptyList(),
    val editors: List<UserDto> = emptyList(),
    val participants: List<UserDto> = emptyList()
)

data class ProjectListResponse(
    val success: Boolean,
    val data: List<ProjectDto>,
    @SerializedName("current_page") val currentPage: Int,
    @SerializedName("last_page") val lastPage: Int,
    val total: Int
)