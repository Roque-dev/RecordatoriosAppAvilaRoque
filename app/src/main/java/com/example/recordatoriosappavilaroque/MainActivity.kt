package com.example.recordatoriosappavilaroque

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recordatoriosappavilaroque.data.InMemoryReminderRepository
import com.example.recordatoriosappavilaroque.data.ReminderRepository
import com.example.recordatoriosappavilaroque.ui.CreateScreen
import com.example.recordatoriosappavilaroque.ui.DetailScreen
import com.example.recordatoriosappavilaroque.ui.ListScreen
import com.example.recordatoriosappavilaroque.ui.theme.RecordatoriosAppAvilaRoqueTheme
import com.example.recordatoriosappavilaroque.viewmodel.CreateViewModel
import com.example.recordatoriosappavilaroque.viewmodel.ListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RecordatoriosAppAvilaRoqueTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    // Instanciamos el repositorio en memoria una sola vez
    val repository: ReminderRepository = InMemoryReminderRepository()

    NavHost(navController = navController, startDestination = "list") {

        // PANTALLA 1: LISTADO
        composable("list") {
            val viewModel: ListViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ListViewModel(repository) as T
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsState()

            // Solucion error de que no guarda: Recarga los datos cada vez que volvés a esta pantalla
            androidx.lifecycle.compose.LifecycleEventEffect(androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadReminders()
            }

            ListScreen(
                uiState = uiState,
                onReminderClick = { id -> navController.navigate("detail/$id") },
                onCreateClick = { navController.navigate("create") },
                onRetry = { viewModel.loadReminders() }
            )
        }

        // PANTALLA 2: CREACIÓN
        composable("create") {
            val viewModel: CreateViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CreateViewModel(repository) as T
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsState()

            // Si el guardado es exitoso, volvemos al listado automáticamente
            LaunchedEffect(uiState.saveSuccess) {
                if (uiState.saveSuccess) {
                    navController.popBackStack()
                }
            }

            CreateScreen(
                uiState = uiState,
                onTitleChange = viewModel::onTitleChange,
                onDescriptionChange = viewModel::onDescriptionChange,
                onCategorySelect = viewModel::onCategorySelect,
                onPrioritySelect = viewModel::onPrioritySelect,
                onSave = { viewModel.saveReminder() },
                onCancel = { navController.popBackStack() }
            )
        }

        // PANTALLA 3: DETALLE
        composable(
            route = "detail/{reminderId}",
            arguments = listOf(navArgument("reminderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("reminderId") ?: 0
            val reminder = (repository as InMemoryReminderRepository).getRemindersSync().find { it.id == id }

            DetailScreen(
                reminder = reminder,
                onBack = { navController.popBackStack() }
            )
        }
    }
}