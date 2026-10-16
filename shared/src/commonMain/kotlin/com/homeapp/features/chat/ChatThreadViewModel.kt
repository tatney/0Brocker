package com.homeapp.features.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.homeapp.data.AppContainer
import com.homeapp.data.model.ChatConversation
import com.homeapp.data.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ChatThreadUiState(
    val conversation: ChatConversation? = null,
    val messages: List<ChatMessage> = emptyList(),
    val draft: String = "",
    val error: String? = null,
    val sending: Boolean = false,
)

class ChatThreadViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val conversationId: Long = savedStateHandle["conversationId"] ?: 0L
    private val repo = AppContainer.chatRepository

    private val _ui = MutableStateFlow(ChatThreadUiState())
    val ui: StateFlow<ChatThreadUiState> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            repo.observeConversation(conversationId).collect { convo ->
                _ui.update { it.copy(conversation = convo) }
            }
        }
        viewModelScope.launch {
            repo.observeMessages(conversationId).collect { messages ->
                _ui.update { it.copy(messages = messages) }
            }
        }
    }

    fun onDraftChange(text: String) = _ui.update { it.copy(draft = text) }

    fun send() {
        val text = _ui.value.draft
        if (text.isBlank() || _ui.value.sending) return
        _ui.update { it.copy(sending = true,error=null) }
        viewModelScope.launch {runCatching {repo.sendMessage(conversationId,text)}.onSuccess {_ui.update {it.copy(draft="",sending=false)}}.onFailure {e->_ui.update {it.copy(sending=false,error=e.message)}}}
    }
}
