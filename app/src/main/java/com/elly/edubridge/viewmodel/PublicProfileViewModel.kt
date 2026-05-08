package com.elly.edubridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elly.edubridge.data.model.ExchangeRequest
import com.elly.edubridge.data.model.User
import com.elly.edubridge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PublicProfileState {
    object Loading : PublicProfileState()
    data class Success(val user: User) : PublicProfileState()
    data class Error(val message: String) : PublicProfileState()
}

@HiltViewModel
class PublicProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    // Add these to your PublicProfileViewModel
    private val _sendRequestState = MutableStateFlow<Boolean>(false)
    val sendRequestState = _sendRequestState.asStateFlow()

    fun sendRequest(receiver: User, skillOffered: String, skillWanted: String) {
        val currentUserId = repository.getCurrentUserId() ?: return

        viewModelScope.launch {
            _sendRequestState.value = true
            try {
                val currentUser = repository.getUser() // Get sender details
                val request = ExchangeRequest(
                    senderId = currentUserId,
                    receiverId = receiver.userId,
                    senderName = currentUser?.fullName ?: "A Student",
                    skillOffered = skillOffered,
                    skillWanted = skillWanted
                )
                repository.sendExchangeRequest(request)
                // You could add a success message state here
            } catch (e: Exception) {
                // Handle error
            } finally {
                _sendRequestState.value = false
            }
        }
    }

    private val _uiState = MutableStateFlow<PublicProfileState>(PublicProfileState.Loading)
    val uiState = _uiState.asStateFlow()

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            _uiState.value = PublicProfileState.Loading
            try {
                val user = repository.getUserById(userId)
                if (user != null) {
                    _uiState.value = PublicProfileState.Success(user)
                } else {
                    _uiState.value = PublicProfileState.Error("User not found")
                }
            } catch (e: Exception) {
                _uiState.value = PublicProfileState.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }
}