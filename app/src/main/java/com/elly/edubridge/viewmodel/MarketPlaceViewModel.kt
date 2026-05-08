package com.elly.edubridge.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.elly.edubridge.data.model.User
import com.elly.edubridge.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MarketplaceState {
    object Loading : MarketplaceState()
    data class Success(val users: List<User>) : MarketplaceState()
    data class Error(val message: String) : MarketplaceState()
}

@HiltViewModel
class MarketplaceViewModel @Inject constructor(
    private val repository: UserRepository
) : ViewModel() {

    private val _marketplaceState =
        MutableStateFlow<MarketplaceState>(MarketplaceState.Loading)

    val marketplaceState = _marketplaceState.asStateFlow()

    private var allUsers: List<User> = emptyList()

    init {
        loadUsers()
    }

    fun loadUsers() {

        viewModelScope.launch {

            _marketplaceState.value = MarketplaceState.Loading

            try {

                allUsers = repository.getAllUsers()

                _marketplaceState.value =
                    MarketplaceState.Success(allUsers)

            } catch (e: Exception) {

                _marketplaceState.value =
                    MarketplaceState.Error(
                        e.localizedMessage ?: "Failed to load marketplace"
                    )
            }
        }
    }

    fun filterBySkill(skill: String) {

        if (skill == "All") {
            _marketplaceState.value =
                MarketplaceState.Success(allUsers)

            return
        }

        val filtered = allUsers.filter {

            it.skillsOffered.any { userSkill ->
                userSkill.contains(skill, ignoreCase = true)
            }
        }

        _marketplaceState.value =
            MarketplaceState.Success(filtered)
    }

    fun searchUsers(query: String) {

        if (query.isBlank()) {

            _marketplaceState.value =
                MarketplaceState.Success(allUsers)

            return
        }

        val results = allUsers.filter {

            it.fullName.contains(query, true) ||
                    it.username.contains(query, true) ||
                    it.headline.contains(query, true) ||
                    it.skillsOffered.any { skill ->
                        skill.contains(query, true)
                    }
        }

        _marketplaceState.value =
            MarketplaceState.Success(results)
    }
}