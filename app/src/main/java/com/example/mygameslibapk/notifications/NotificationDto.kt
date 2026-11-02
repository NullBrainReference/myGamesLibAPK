package com.example.mygameslibapk.notifications

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    val type: String,
    val title: String,
    val message: String?,
    @SerializedName("is_read") val isRead: Boolean,
    @SerializedName("created_at") val createdAt: String?
)

data class PaginatedNotifications(
    val data: List<NotificationDto>
)

data class NotificationsApiResponse(
    val success: Boolean,
    @SerializedName("unread_count") val unreadCount: Int,
    val notifications: PaginatedNotifications
)

data class MarkReadResponse(
    val success: Boolean,
    val message: String,
    @SerializedName("target_url") val targetUrl: String? = null
)