package com.example.recordatoriosappavilaroque.data

data class UserPreferences(
    val isDarkTheme: Boolean = false,
    val showCompleted: Boolean = true,
    val sortOrder: SortOrder = SortOrder.BY_DATE
)

// Enum para el criterio de ordenamiento
enum class SortOrder { BY_DATE, BY_PRIORITY }