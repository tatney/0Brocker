package com.homeapp.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconChat
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSpaceXS
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.widgets.EmptyState

@Composable
fun ChatListScreen(
    onOpenThread: (Long) -> Unit,
    viewModel: ChatListViewModel = viewModel { ChatListViewModel() },
) {
    val state by viewModel.ui.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Chats",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.width(kSpaceSM))
            if (state.unreadTotal > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(kPrimaryRed)
                        .padding(horizontal = kSpaceSM, vertical = kSpaceXS),
                ) {
                    Text(
                        text = "${state.unreadTotal}",
                        style = MaterialTheme.typography.labelSmall,
                        color = kSurface,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        if (state.conversations.isEmpty()) {
            EmptyState(
                icon = IconChat,
                title = "No conversations yet",
                subtitle = "Start chatting with owners & service pros",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = kSpaceMD,
                    vertical = kSpaceXS,
                ),
                verticalArrangement = Arrangement.spacedBy(kSpaceSM),
            ) {
                items(state.conversations, key = { it.id }) { convo ->
                    ConversationRow(
                        name = convo.name,
                        emoji = convo.avatarEmoji,
                        lastMessage = convo.lastMessage,
                        unread = convo.unreadCount,
                        onClick = { onOpenThread(convo.id) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    name: String,
    emoji: String,
    lastMessage: String,
    unread: Long,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(kRadiusMD)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(kSpaceMD),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = emoji, fontSize = 24.sp)
        }
        Spacer(Modifier.width(kSpaceSM))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.height(kSpaceXS))
            Text(
                text = lastMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = if (unread > 0) MaterialTheme.colorScheme.onSurface else kTextSecondary,
                fontWeight = if (unread > 0) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.width(kSpaceSM))
        if (unread > 0) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(kPrimaryRed)
                    .size(22.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "$unread",
                    style = MaterialTheme.typography.labelSmall,
                    color = kSurface,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}
