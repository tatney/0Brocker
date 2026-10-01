package com.homeapp.data.repository

import com.homeapp.core.time.currentTimeEpochSeconds

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.homeapp.data.model.ChatConversation
import com.homeapp.data.model.ChatMessage
import com.homeapp.db.Conversations
import com.homeapp.db.HomeAppDatabase
import com.homeapp.db.Messages
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class ChatRepositoryImpl(
    private val db: HomeAppDatabase,
) : ChatRepository {

    private val q get() = db.homeAppDatabaseQueries

    override fun observeConversations(): Flow<List<ChatConversation>> =
        q.selectAllConversations().asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override fun observeConversation(id: Long): Flow<ChatConversation?> =
        q.selectConversationById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.toDomain() }

    override fun observeMessages(conversationId: Long): Flow<List<ChatMessage>> =
        q.selectMessagesForConversation(conversationId).asFlow().mapToList(Dispatchers.Default).map { rows ->
            rows.map { it.toDomain() }
        }

    override suspend fun conversationCount(): Long =
        q.conversationCount().executeAsOne()

    override suspend fun unreadTotal(): Long =
        q.unreadTotal().executeAsOne()

    override suspend fun sendMessage(conversationId: Long, text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val ts = currentTimeEpochSeconds()
        val nextId = q.messageMaxId().executeAsOne() + 1
        q.insertMessage(nextId, conversationId, "me", trimmed, ts)
        q.updateConversationLast(trimmed, ts, conversationId)
    }

    private fun Conversations.toDomain() = ChatConversation(
        id = id,
        name = name,
        avatarEmoji = avatar_emoji,
        lastMessage = last_message,
        lastMessageAtEpoch = last_message_at_epoch,
        unreadCount = unread_count,
    )

    private fun Messages.toDomain() = ChatMessage(
        id = id,
        conversationId = conversation_id,
        sender = sender,
        text = text,
        createdAtEpoch = created_at_epoch,
    )
}
