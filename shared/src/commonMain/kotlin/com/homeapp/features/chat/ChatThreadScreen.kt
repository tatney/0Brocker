package com.homeapp.features.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.homeapp.core.icons.IconArrowForward
import com.homeapp.core.icons.IconBack
import com.homeapp.core.theme.kPrimaryRed
import com.homeapp.core.theme.kRadiusLGSize
import com.homeapp.core.theme.kRadiusMD
import com.homeapp.core.theme.kRadiusSMSize
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.theme.kSurface
import com.homeapp.core.theme.kTextSecondary
import com.homeapp.core.theme.kTileGrey
import com.homeapp.data.model.ChatMessage

@Composable
fun ChatThreadScreen(
    onBack: () -> Unit,
    viewModel: ChatThreadViewModel = viewModel(),
) {
    val state by viewModel.ui.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.lastIndex)
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(kTileGrey, CircleShape),
                    contentAlignment = Alignment.Center,
                ) { Text(state.conversation?.avatarEmoji ?: "💬", fontSize = 20.sp) }
                Spacer(Modifier.width(kSpaceSM))
                Column(Modifier.weight(1f)) {
                    Text(
                        text = state.conversation?.name ?: "Chat",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = kSpaceMD, vertical = kSpaceSM),
                verticalArrangement = Arrangement.spacedBy(kSpaceSM),
            ) {
                items(state.messages, key = { it.id }) { message ->
                    MessageBubble(message)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = state.draft,
                    onValueChange = viewModel::onDraftChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message", color = kTextSecondary) },
                    maxLines = 3,
                    shape = kRadiusMD,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = kPrimaryRed,
                        cursorColor = kPrimaryRed,
                    ),
                )
                Spacer(Modifier.width(kSpaceSM))
                IconButton(
                    onClick = viewModel::send,
                    enabled = state.draft.isNotBlank(),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(kPrimaryRed),
                ) {
                    Icon(IconArrowForward, contentDescription = "Send", tint = kSurface)
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val mine = message.isMine
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(
                    topStart = if (mine) kRadiusLGSize else kRadiusSMSize,
                    topEnd = if (mine) kRadiusSMSize else kRadiusLGSize,
                    bottomEnd = kRadiusLGSize,
                    bottomStart = kRadiusLGSize,
                ))
                .background(if (mine) kPrimaryRed else kTileGrey)
                .padding(horizontal = kSpaceMD, vertical = kSpaceSM),
        ) {
            Text(
                text = message.text,
                style = MaterialTheme.typography.bodyMedium,
                color = if (mine) kSurface else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}