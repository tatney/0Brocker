package com.homeapp.features.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ChatConversation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatListUiState(
    val conversations: List<ChatConversation> = emptyList(),
    val unreadTotal: Long = 0,
)

class ChatListViewModel : ViewModel() {

    private val repo = AppContainer.chatRepository

    private val _ui = MutableStateFlow(ChatListUiState())
    val ui: StateFlow<ChatListUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeConversations().collect { convos ->
                _ui.value = ChatListUiState(
                    conversations = convos,
                    unreadTotal = convos.sumOf { it.unreadCount },
                )
            }
        }
    }
}
