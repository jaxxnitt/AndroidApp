package com.jaxxnitt.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jaxxnitt.myapplication.ui.screens.AddContactScreen
import com.jaxxnitt.myapplication.ui.screens.ContactsScreen
import com.jaxxnitt.myapplication.ui.screens.HomeScreen
import com.jaxxnitt.myapplication.ui.screens.SettingsScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Contacts.route) {
            ContactsScreen(
                onNavigateBack = { navController.popBackStack() },
                onAddContact = { navController.navigate(Screen.AddContact.route) },
                onEditContact = { contactId ->
                    navController.navigate(Screen.EditContact.createRoute(contactId))
                }
            )
        }

        composable(Screen.AddContact.route) {
            AddContactScreen(
                onNavigateBack = { navController.popBackStack() },
                contactId = null
            )
        }

        composable(
            route = Screen.EditContact.route,
            arguments = listOf(
                navArgument("contactId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val contactId = backStackEntry.arguments?.getLong("contactId")
            AddContactScreen(
                onNavigateBack = { navController.popBackStack() },
                contactId = contactId
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
