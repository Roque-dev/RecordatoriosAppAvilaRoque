package com.example.recordatoriosappavilaroque

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.recordatoriosappavilaroque.data.*
import com.example.recordatoriosappavilaroque.ui.*
import com.example.recordatoriosappavilaroque.ui.theme.RecordatoriosAppAvilaRoqueTheme
import com.example.recordatoriosappavilaroque.viewmodel.*
import com.google.firebase.firestore.FirebaseFirestore

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

    // 1. PRIMERO inicializar el AuthRepository
    val authRepository = remember { AuthRepository() }

    // 2. Luego la Base de datos (Room) y Preferencias
    val database = AppDatabase.getDatabase(context)
    val dao = database.reminderDao()
    val preferencesRepository = PreferencesRepository(context)

    // 3. Crea el Repositorio principal que depende de los anteriores
    val reminderRepository = remember {
        OfflineReminderRepository(
            dao = dao,
            auth = authRepository.auth,
            firestore = FirebaseFirestore.getInstance()
        )
    }

    // Determinar si el usuario ya estaba logueado al abrir la app
    val startDestination = if (authRepository.currentUser != null) "list" else "auth"

    NavHost(navController = navController, startDestination = startDestination) {

        // PANTALLA DE LOGIN / REGISTRO
        composable("auth") {
            val viewModel: AuthViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return AuthViewModel(authRepository) as T
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsState()

            LaunchedEffect(uiState.isSuccess) {
                if (uiState.isSuccess) {
                    navController.navigate("list") {
                        popUpTo("auth") { inclusive = true }
                    }
                }
            }

            AuthScreen(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onAuthenticate = { viewModel.authenticate() },
                onToggleMode = viewModel::toggleMode
            )
        }

        // PANTALLA 1: LISTADO
        composable("list") {
            val viewModel: ListViewModel = viewModel(
                factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return ListViewModel(reminderRepository, preferencesRepository, authRepository) as T
                    }
                }
            )
            val uiState by viewModel.uiState.collectAsState()

            ListScreen(
                uiState = uiState,
                onReminderClick = { id -> navController.navigate("detail/$id") },
                onCreateClick = { navController.navigate("create") },
                onRetry = { viewModel.syncData() },
                onLogout = {
                    viewModel.logout {
                        navController.navigate("auth") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
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
            var reminder by remember { androidx.compose.runtime.mutableStateOf<Reminder?>(null) }

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