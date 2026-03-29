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
                MedTrackRoot(registerVm, patientsVm, treatmentVm, logsVm)
            }
        }
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
    Register("Register"),
    Patients("Patients"),
    Treatment("Treatment"),
    Logs("Logs")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MedTrackRoot(
    registerViewModel: RegisterViewModel,
    patientsViewModel: PatientsViewModel,
    treatmentPlanViewModel: TreatmentPlanViewModel,
    logsViewModel: LogsViewModel
) {
    var selectedTab by remember { mutableStateOf(AppTab.Register) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("MedTrack") }) },
        bottomBar = {
            NavigationBar {
                AppTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Text(tab.label) },
                        label = null
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            AppTab.Register -> {
                RegisterScreen(
                    viewModel = registerViewModel,
                    modifier = Modifier.fillMaxSize().padding(padding)
                )
            }
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
