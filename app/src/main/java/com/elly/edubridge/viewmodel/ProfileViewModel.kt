package com.elly.edubridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elly.edubridge.data.model.User
import com.elly.edubridge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

sealed class ProfileState {
    object Idle : ProfileState()
    object Loading : ProfileState()
    object Success : ProfileState()
    data class Error(val message: String) : ProfileState()
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Idle)
    val profileState = _profileState.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser = _currentUser.asStateFlow()

    private val _isUsernameAvailable = MutableStateFlow<Boolean?>(null)
    val isUsernameAvailable = _isUsernameAvailable.asStateFlow()

    private val _availableSkills = MutableStateFlow<List<com.elly.edubridge.data.model.Skill>>(emptyList())
    val availableSkills = _availableSkills.asStateFlow()

    init {
        loadUser()
        loadSkills()
    }

    private fun loadSkills() {
        viewModelScope.launch {
            try {
                _availableSkills.value = repository.getSkills()
            } catch (e: Exception) {
                // Handle error or use local fallback
            }
        }
    }

    fun loadUser() {
        viewModelScope.launch {
            _currentUser.value = repository.getUser()
        }
    }

    fun checkUsername(username: String) {
        if (username.isEmpty()) {
            _isUsernameAvailable.value = null
            return
        }
        viewModelScope.launch {
            _isUsernameAvailable.value = repository.checkUsernameUnique(username)
        }
    }

    fun updateProfile(user: User, imageFile: File? = null) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            try {
                var updatedUser = user
                if (imageFile != null) {
                    val imageUrl = repository.uploadProfileImage(imageFile)
                    updatedUser = user.copy(profileImage = imageUrl)
                }
                repository.updateUser(updatedUser)
                _currentUser.value = updatedUser
                _profileState.value = ProfileState.Success
            } catch (e: Exception) {
                _profileState.value = ProfileState.Error(e.localizedMessage ?: "Update failed")
            }
        }
    }

    fun resetState() {
        _profileState.value = ProfileState.Idle
    }
}
