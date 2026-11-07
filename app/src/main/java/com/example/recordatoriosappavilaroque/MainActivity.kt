package com.example.recordatoriosappavilaroque

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recordatoriosappavilaroque.data.*
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
    val context = LocalContext.current

    // 1. Inicializar la Base de Datos (Room)
    val database = AppDatabase.getDatabase(context)
    val dao = database.reminderDao()

    // 2. Inicializar Retrofit (Internet)
    val apiService = RetrofitClient.apiService

    // 3. Inicializar los Repositorios
    val reminderRepository = OfflineReminderRepository(dao, apiService)
    val preferencesRepository = PreferencesRepository(context)

    NavHost(navController = navController, startDestination = "list") {

        // PANTALLA 1: LISTADO
        composable("list") {
            val viewModel: ListViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ListViewModel(reminderRepository, preferencesRepository) as T
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsState()

            ListScreen(
                uiState = uiState,
                onReminderClick = { id -> navController.navigate("detail/$id") },
                onCreateClick = { navController.navigate("create") },
                onRetry = { viewModel.syncData() } // El botón de reintentar ahora sincroniza con internet
            )
        }

        // PANTALLA 2: CREACIÓN
        composable("create") {
            val viewModel: CreateViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return CreateViewModel(reminderRepository) as T
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsState()

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
            // Nota: Lo ideal sería un DetailViewModel, pero para simplificar uso el repo
            var reminder by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Reminder?>(null) }

            LaunchedEffect(id) {
                reminder = reminderRepository.getReminderById(id)
            }

            DetailScreen(
                reminder = reminder,
                onBack = { navController.popBackStack() }
            )
        }
    }
}