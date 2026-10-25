package com.example.mygameslibapk.laravelclient

import com.google.gson.annotations.SerializedName

data class MessageDto(
    val id: Int,
    @SerializedName("sender_id") val senderId: Int,
    @SerializedName("receiver_id") val receiverId: Int,
    val body: String,
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

data class LastMessageDto(
    val body: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("sender_id") val senderId: Int
)

data class ConversationContactDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String? = null,
    @SerializedName("last_message") val lastMessage: LastMessageDto? = null
)

data class SendMessageRequest(
    val body: String
)

data class ConversationsResponse(
    val success: Boolean,
    val conversations: List<ConversationContactDto>
)

data class ChatThreadResponse(
    val success: Boolean,
    val contact: UserDto,
    val messages: List<MessageDto>
)

data class FriendsListResponse(
    val success: Boolean,
    val friends: List<UserDto>,
    @SerializedName("incoming_requests") val incomingRequests: List<UserDto>,
    @SerializedName("sent_requests") val sentRequests: List<UserDto>
)