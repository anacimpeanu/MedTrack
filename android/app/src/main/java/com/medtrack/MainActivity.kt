package com.medtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.medtrack.domain.repository.MedTrackRepository
import com.medtrack.presentation.LogsScreen
import com.medtrack.presentation.LogsViewModel
import com.medtrack.presentation.LoginScreen
import com.medtrack.presentation.LoginViewModel
import com.medtrack.presentation.PatientsScreen
import com.medtrack.presentation.PatientsViewModel
import com.medtrack.presentation.RegisterScreen
import com.medtrack.presentation.RegisterViewModel
import com.medtrack.presentation.TreatmentPlanScreen
import com.medtrack.presentation.TreatmentPlanViewModel

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
                val patientsVm: PatientsViewModel = viewModel(
                    factory = PatientsVmFactory(app.repository)
                )
                val treatmentVm: TreatmentPlanViewModel = viewModel(
                    factory = TreatmentPlanVmFactory(app.repository)
                )
                val logsVm: LogsViewModel = viewModel(
                    factory = LogsVmFactory(app.repository)
                )
                MedTrackRoot(loginVm, registerVm, patientsVm, treatmentVm, logsVm)
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

private class PatientsVmFactory(
    private val repository: MedTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return PatientsViewModel(repository) as T
    }
}

private class TreatmentPlanVmFactory(
    private val repository: MedTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return TreatmentPlanViewModel(repository) as T
    }
}

private class LogsVmFactory(
    private val repository: MedTrackRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LogsViewModel(repository) as T
    }
}

private enum class AppTab(val label: String) {
    Patients("Patients"),
    Treatment("Treatment"),
    Logs("Logs")
}

private enum class AuthTab(val label: String) {
    Login("Login"),
    Register("Register")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedTrackRoot(
    loginViewModel: LoginViewModel,
    registerViewModel: RegisterViewModel,
    patientsViewModel: PatientsViewModel,
    treatmentPlanViewModel: TreatmentPlanViewModel,
    logsViewModel: LogsViewModel
) {
    var loggedInUserId by remember { mutableStateOf<Long?>(null) }
    var selectedAuthTab by remember { mutableStateOf(AuthTab.Login) }
    var selectedAppTab by remember { mutableStateOf(AppTab.Patients) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MedTrack") },
                actions = {
                    if (loggedInUserId != null) {
                        TextButton(
                            onClick = {
                                loggedInUserId = null
                                selectedAuthTab = AuthTab.Login
                            }
                        ) {
                            Text("Logout")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                if (loggedInUserId == null) {
                    AuthTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedAuthTab == tab,
                            onClick = { selectedAuthTab = tab },
                            icon = { Text(tab.label) },
                            label = null
                        )
                    }
                } else {
                    AppTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = selectedAppTab == tab,
                            onClick = { selectedAppTab = tab },
                            icon = { Text(tab.label) },
                            label = null
                        )
                    }
                }
            }
        }
    ) { padding ->
        if (loggedInUserId == null) {
            when (selectedAuthTab) {
                AuthTab.Login -> {
                    LoginScreen(
                        viewModel = loginViewModel,
                        onLoginSuccess = { userId ->
                            loggedInUserId = userId
                            selectedAppTab = AppTab.Patients
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
        } else {
            when (selectedAppTab) {
                AppTab.Patients -> {
                    PatientsScreen(
                        viewModel = patientsViewModel,
                        modifier = Modifier.fillMaxSize().padding(padding)
                    )
                }
                AppTab.Treatment -> {
                    TreatmentPlanScreen(
                        viewModel = treatmentPlanViewModel,
                        modifier = Modifier.fillMaxSize().padding(padding)
                    )
                }
                AppTab.Logs -> {
                    LogsScreen(
                        viewModel = logsViewModel,
                        modifier = Modifier.fillMaxSize().padding(padding)
                    )
                }
            }
        }
    }
}
