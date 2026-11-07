package com.example.recordatoriosappavilaroque.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// Extensión para crear la ÚNICA instancia de DataStore en toda la app
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

class PreferencesRepository(private val context: Context) {

    // Defino las claves tipadas
    private object Keys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val SHOW_COMPLETED = booleanPreferencesKey("show_completed")
        val SORT_ORDER = stringPreferencesKey("sort_order")
    }

    // Expongo un Flow con manejo de errores y valores por defecto
    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                // Si hay error de lectura, devolve preferencias vacías (valores por defecto)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val theme = preferences[Keys.DARK_THEME] ?: false
            val showCompleted = preferences[Keys.SHOW_COMPLETED] ?: true
            val sortOrderString = preferences[Keys.SORT_ORDER] ?: SortOrder.BY_DATE.name

            // Convertir el String de vuelta al Enum de forma segura
            val sortOrder = try {
                SortOrder.valueOf(sortOrderString)
            } catch (e: Exception) {
                SortOrder.BY_DATE
            }

            UserPreferences(
                isDarkTheme = theme,
                showCompleted = showCompleted,
                sortOrder = sortOrder
            )
        }

    // Funciones suspendidas para modificar las preferencias (sin bloquear el hilo principal)
    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[Keys.DARK_THEME] = enabled }
    }

    suspend fun setShowCompleted(enabled: Boolean) {
        context.dataStore.edit { it[Keys.SHOW_COMPLETED] = enabled }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { it[Keys.SORT_ORDER] = order.name }
    }
}