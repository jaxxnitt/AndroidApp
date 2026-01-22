package com.jaxxnitt.myapplication.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Contacts : Screen("contacts")
    data object AddContact : Screen("add_contact")
    data object EditContact : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: Long) = "edit_contact/$contactId"
    }
    data object Settings : Screen("settings")
}
