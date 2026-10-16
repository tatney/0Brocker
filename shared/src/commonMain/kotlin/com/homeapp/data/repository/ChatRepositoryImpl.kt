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
            rows.filter { (q.selectActiveSession().executeAsOneOrNull()?.user_id ?: -1L) in listOf(it.local_user_id,it.local_owner_id) }.map { it.toDomain() }
        }

    override fun observeConversation(id: Long): Flow<ChatConversation?> =
        q.selectConversationById(id).asFlow().mapToOneOrNull(Dispatchers.Default).map { it?.takeIf {row->q.selectActiveSession().executeAsOneOrNull()?.user_id in listOf(row.local_user_id,row.local_owner_id)}?.toDomain() }

    override fun observeMessages(conversationId: Long): Flow<List<ChatMessage>> =
        q.selectMessagesForConversation(conversationId).asFlow().mapToList(Dispatchers.Default).map { rows ->
            val conversation=q.selectConversationById(conversationId).executeAsOneOrNull()
            if(q.selectActiveSession().executeAsOneOrNull()?.user_id !in listOf(conversation?.local_user_id,conversation?.local_owner_id)) emptyList() else rows.map {it.toDomain()}
        }

    override suspend fun openOwnerConversation(propertyId: Long, userId: Long, ownerName: String): Long {
        val id = 1_000_000_000L + userId * 1_000_000L + propertyId
        if (q.selectConversationById(id).executeAsOneOrNull() == null) {
            q.insertConversation(id, ownerName.ifBlank { "Owner" } + " - listing #" + propertyId, "OWNER", "Test conversation; messages stay on this device", currentTimeEpochSeconds(), 0)
        }
        require(q.selectActiveSession().executeAsOneOrNull()?.user_id==userId) {"Sign in required"}
        val ownerId=q.selectPropertyOwner(propertyId).executeAsOneOrNull()?.owner_id?:0
        q.setConversationLocalUser(userId,ownerId,id)
        return id
    }

    override suspend fun conversationCount(): Long =
        q.selectAllConversations().executeAsList().count {q.selectActiveSession().executeAsOneOrNull()?.user_id in listOf(it.local_user_id,it.local_owner_id)}.toLong()

    override suspend fun unreadTotal(): Long =
        0L

    override suspend fun sendMessage(conversationId: Long, text: String) {
        val conversation=q.selectConversationById(conversationId).executeAsOneOrNull()
        require(q.selectActiveSession().executeAsOneOrNull()?.user_id?.let {it in listOf(conversation?.local_user_id,conversation?.local_owner_id)} == true) {"Conversation access denied"}
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        val ts = currentTimeEpochSeconds()
        val nextId = q.messageMaxId().executeAsOne() + 1
        q.insertMessage(nextId, conversationId, "user:"+q.selectActiveSession().executeAsOne().user_id, trimmed, ts)
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
        sender = if(sender=="user:"+q.selectActiveSession().executeAsOneOrNull()?.user_id) "me" else if(sender=="me") "sample" else sender,
        text = text,
        createdAtEpoch = created_at_epoch,
    )
}
