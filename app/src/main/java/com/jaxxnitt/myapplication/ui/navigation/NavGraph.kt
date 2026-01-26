package com.jaxxnitt.myapplication.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.jaxxnitt.myapplication.AreYouDeadApplication
import com.jaxxnitt.myapplication.ui.screens.AddContactScreen
import com.jaxxnitt.myapplication.ui.screens.ContactsScreen
import com.jaxxnitt.myapplication.ui.screens.FirstTimeSetupScreen
import com.jaxxnitt.myapplication.ui.screens.HomeScreen
import com.jaxxnitt.myapplication.ui.screens.LoginScreen
import com.jaxxnitt.myapplication.ui.screens.OtpVerificationScreen
import com.jaxxnitt.myapplication.ui.screens.PhoneLoginScreen
import com.jaxxnitt.myapplication.ui.screens.ProfileScreen
import com.jaxxnitt.myapplication.ui.screens.SettingsScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.net.URLDecoder

@Composable
fun NavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as AreYouDeadApplication

    // Check app state for navigation
    val settings = runBlocking {
        app.settingsDataStore.settingsFlow.first()
    }
    val isFirstTime = settings.isFirstTime
    val isLoggedIn = app.authRepository.isLoggedIn

    // Determine start destination based on auth state
    // Login first, then name setup if first time
    val startDestination = when {
        !isLoggedIn -> Screen.Login.route
        isFirstTime -> Screen.FirstTimeSetup.route
        else -> Screen.Home.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth screens
        composable(Screen.Login.route) {
            val currentSettings = runBlocking {
                app.settingsDataStore.settingsFlow.first()
            }
            LoginScreen(
                onLoginSuccess = {
                    if (currentSettings.isFirstTime) {
                        navController.navigate(Screen.FirstTimeSetup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                },
                onPhoneLogin = {
                    navController.navigate(Screen.PhoneLogin.route)
                }
            )
        }

        composable(Screen.PhoneLogin.route) {
            PhoneLoginScreen(
                onNavigateBack = { navController.popBackStack() },
                onCodeSent = { verificationId, phoneNumber ->
                    navController.navigate(
                        Screen.OtpVerification.createRoute(verificationId, phoneNumber)
                    )
                }
            )
        }

        composable(
            route = Screen.OtpVerification.route,
            arguments = listOf(
                navArgument("verificationId") { type = NavType.StringType },
                navArgument("phoneNumber") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val verificationId = backStackEntry.arguments?.getString("verificationId") ?: ""
            val phoneNumber = URLDecoder.decode(
                backStackEntry.arguments?.getString("phoneNumber") ?: "",
                "UTF-8"
            )
            val currentSettings = runBlocking {
                app.settingsDataStore.settingsFlow.first()
            }
            OtpVerificationScreen(
                verificationId = verificationId,
                phoneNumber = phoneNumber,
                onNavigateBack = { navController.popBackStack() },
                onVerificationSuccess = {
                    if (currentSettings.isFirstTime) {
                        navController.navigate(Screen.FirstTimeSetup.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }

        // First time setup - goes to Home after completion
        composable(Screen.FirstTimeSetup.route) {
            FirstTimeSetupScreen(
                onSetupComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.FirstTimeSetup.route) { inclusive = true }
                    }
                }
            )
        }

        // Main app screens
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToContacts = { navController.navigate(Screen.Contacts.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
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
                onNavigateBack = { navController.popBackStack() },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
