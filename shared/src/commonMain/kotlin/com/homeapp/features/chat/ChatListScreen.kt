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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.selection.selectable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalDivider
import com.homeapp.core.icons.IconBack
import com.homeapp.features.shell.BrandMark
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
    onBack: () -> Unit = {},
    viewModel: ChatListViewModel = viewModel { ChatListViewModel() },
) {
    val state by viewModel.ui.collectAsState()

    var selectedTab by rememberSaveable { mutableStateOf("Chat") }
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Row(Modifier.fillMaxWidth().height(62.dp).padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { Icon(IconBack, "Back", Modifier.size(20.dp), tint = Color.Black) }
            BrandMark(22)
            Text("0BROCKER Inbox", Modifier.padding(start = 12.dp), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        HorizontalDivider(color = Color(0xFFDDDDDD), thickness = .5.dp)
        Row(Modifier.fillMaxWidth()) {
            listOf("Chat", "Call", "Feedback").forEachIndexed { index, label ->
                Column(Modifier.weight(1f).selectable(selectedTab == label, role = Role.Tab, onClick = { selectedTab = label })) {
                    Row(Modifier.fillMaxWidth().height(58.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(label, fontSize = 16.sp, color = if(selectedTab == label) Color(0xFF969C99) else Color(0xFF4A4A4A))
                        }
                        if(index < 2) Box(Modifier.width(1.dp).height(36.dp).background(Color(0xFFDEE2E4)))
                    }
                    Box(Modifier.fillMaxWidth().height(3.dp).background(if(selectedTab == label) Color(0xFFFF355D) else Color.Transparent))
                }
            }
        }
        if (selectedTab == "Chat") {
        if (state.conversations.isEmpty()) {
            Spacer(Modifier.weight(1f))
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
