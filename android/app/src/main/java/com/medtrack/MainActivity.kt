package com.medtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrack.domain.repository.MedTrackRepository
import com.medtrack.presentation.DashboardScreen
import com.medtrack.presentation.DashboardViewModel
import com.medtrack.presentation.LoginScreen
import com.medtrack.presentation.LoginViewModel
import com.medtrack.presentation.ProfileSetupScreen
import com.medtrack.presentation.ProfileSetupViewModel
import com.medtrack.presentation.RegisterScreen
import com.medtrack.presentation.RegisterViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as MedTrackApplication

        setContent {
            MaterialTheme {
                val loginVm: LoginViewModel = viewModel(
                    factory = LoginVmFactory(app.repository)
                )
                val registerVm: RegisterViewModel = viewModel(
                    factory = RegisterVmFactory(app.repository)
                )
                MedTrackRoot(app.repository, loginVm, registerVm)
            }
        }
    }
}

private class LoginVmFactory(
    private val repository: MedTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LoginViewModel(repository) as T
    }
}

private class RegisterVmFactory(
    private val repository: MedTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return RegisterViewModel(repository) as T
    }
}

private class ProfileSetupVmFactory(
    private val repository: MedTrackRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ProfileSetupViewModel(repository, userId) as T
    }
}

private class DashboardVmFactory(
    private val repository: MedTrackRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return DashboardViewModel(repository, userId) as T
    }
}

private enum class AuthTab(val label: String) {
    Login("Login"),
    Register("Register")
}

private sealed class RootScreen {
    object Auth : RootScreen()
    data class ProfileSetup(val userId: Long) : RootScreen()
    data class App(val userId: Long) : RootScreen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedTrackRoot(
    repository: MedTrackRepository,
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel
) {
    var rootScreen by remember { mutableStateOf<RootScreen>(RootScreen.Auth) }
    var selectedAuthTab by remember { mutableStateOf(AuthTab.Login) }

    // Color scheme matching login/register pages
    val gradientColor = Color(0xFF667EEA)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (rootScreen) {
                            RootScreen.Auth -> ""
                            is RootScreen.ProfileSetup -> "Profile setup"
                            is RootScreen.App -> "Dashboard"
                        },
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = gradientColor
                ),
                actions = {
                    if (rootScreen != RootScreen.Auth) {
                        IconButton(
                            onClick = {
                                rootScreen = RootScreen.Auth
                                selectedAuthTab = AuthTab.Login
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Logout,
                                contentDescription = "Logout",
                                tint = Color.White
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (rootScreen == RootScreen.Auth) {
                NavigationBar(
                    containerColor = gradientColor,
                    contentColor = Color.White
                ) {
                    when (rootScreen) {
                        RootScreen.Auth -> {
                            AuthTab.entries.forEach { tab ->
                                NavigationBarItem(
                                    selected = selectedAuthTab == tab,
                                    onClick = { selectedAuthTab = tab },
                                    icon = {
                                        Icon(
                                            if (tab == AuthTab.Login) Icons.Default.Person else Icons.Default.Description,
                                            contentDescription = tab.label,
                                            tint = if (selectedAuthTab == tab) Color.White else Color.White.copy(alpha = 0.6f)
                                        )
                                    },
                                    label = {
                                        Text(
                                            tab.label,
                                            color = if (selectedAuthTab == tab) Color.White else Color.White.copy(alpha = 0.6f),
                                            fontSize = 12.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color.White,
                                        selectedTextColor = Color.White,
                                        unselectedIconColor = Color.White.copy(alpha = 0.6f),
                                        unselectedTextColor = Color.White.copy(alpha = 0.6f),
                                        indicatorColor = Color.White.copy(alpha = 0.2f)
                                    )
                                )
                            }
                        }

                        is RootScreen.ProfileSetup -> Unit
                        is RootScreen.App -> Unit
                    }
                }
            }
        }
    ) { padding ->
        when (val screen = rootScreen) {
            RootScreen.Auth -> {
                when (selectedAuthTab) {
                    AuthTab.Login -> {
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = { userId, needsProfileSetup ->
                                rootScreen = if (needsProfileSetup) {
                                    RootScreen.ProfileSetup(userId)
                                } else {
                                    RootScreen.App(userId)
                                }
                            },
                            onRegisterClick = {
                                selectedAuthTab = AuthTab.Register
                            },
                            modifier = Modifier.fillMaxSize().padding(padding)
                        )
                    }
                    AuthTab.Register -> {
                        RegisterScreen(
                            viewModel = registerViewModel,
                            onLoginClick = {
                                selectedAuthTab = AuthTab.Login
                            },
                            modifier = Modifier.fillMaxSize().padding(padding)
                        )
                    }
                }
            }

            is RootScreen.ProfileSetup -> {
                val profileSetupViewModel: ProfileSetupViewModel = viewModel(
                    key = "profile-${screen.userId}",
                    factory = ProfileSetupVmFactory(repository, screen.userId)
                )

                ProfileSetupScreen(
                    viewModel = profileSetupViewModel,
                    onProfileCompleted = { userId ->
                        rootScreen = RootScreen.App(userId)
                    },
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }

            is RootScreen.App -> {
                val dashboardViewModel: DashboardViewModel = viewModel(
                    key = "dashboard-${screen.userId}",
                    factory = DashboardVmFactory(repository, screen.userId)
                )

                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onEditProfile = { rootScreen = RootScreen.ProfileSetup(screen.userId) },
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
        }
    }
}
