package top.yjp.my.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import top.yjp.my.app.model.User
import top.yjp.my.app.repository.IAuthRepository

class AuthViewModel(
    private val authRepository: IAuthRepository
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow("")
    val token: StateFlow<String> = _token.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = authRepository.login(username, password)
            result.fold(
                onSuccess = { (token, user) ->
                    _token.value = token
                    _currentUser.value = user
                    _isLoggedIn.value = true
                },
                onFailure = { e ->
                    _error.value = e.message ?: "登录失败"
                }
            )
            _isLoading.value = false
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentToken = _token.value
            if (currentToken.isNotEmpty()) {
                authRepository.logout(currentToken)
            }
            _token.value = ""
            _currentUser.value = null
            _isLoggedIn.value = false
        }
    }

    fun clearError() {
        _error.value = null
    }

    /** 提供给 TaskRepository 使用的 token */
    fun getToken(): String = _token.value
}
