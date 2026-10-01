package com.homeapp.data.repository

import com.homeapp.data.model.ChatConversation
import com.homeapp.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun observeConversations(): Flow<List<ChatConversation>>
    fun observeConversation(id: Long): Flow<ChatConversation?>
    fun observeMessages(conversationId: Long): Flow<List<ChatMessage>>
    suspend fun conversationCount(): Long
    suspend fun unreadTotal(): Long
    suspend fun sendMessage(conversationId: Long, text: String)
}
