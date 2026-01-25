package com.jaxxnitt.myapplication.ui.navigation

sealed class Screen(val route: String) {
    data object FirstTimeSetup : Screen("first_time_setup")
    data object Home : Screen("home")
    data object Profile : Screen("profile")
    data object Contacts : Screen("contacts")
    data object AddContact : Screen("add_contact")
    data object EditContact : Screen("edit_contact/{contactId}") {
        fun createRoute(contactId: Long) = "edit_contact/$contactId"
    }
    data object Settings : Screen("settings")

    // Auth screens
    data object Login : Screen("login")
    data object PhoneLogin : Screen("phone_login")
    data object OtpVerification : Screen("otp_verification/{verificationId}/{phoneNumber}") {
        fun createRoute(verificationId: String, phoneNumber: String): String {
            val encodedPhone = java.net.URLEncoder.encode(phoneNumber, "UTF-8")
            return "otp_verification/$verificationId/$encodedPhone"
        }
    }
}
