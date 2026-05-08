package com.elly.edubridge.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.elly.edubridge.ui.screens.*
import com.elly.edubridge.viewmodel.AuthViewModel
import com.elly.edubridge.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavHost(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()

    NavHost(navController = navController, startDestination = "auth_check") {
        composable("auth_check") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            val userState by profileViewModel.currentUser.collectAsState()

            LaunchedEffect(userState, auth.currentUser) {
                val currentFirebaseUser = auth.currentUser
                if (currentFirebaseUser == null) {
                    navController.navigate("auth") { popUpTo("auth_check") { inclusive = true } }
                } else {
                    val user = userState
                    if (user != null) {
                        if (user.username.isEmpty()) {
                            navController.navigate("complete_profile") { popUpTo("auth_check") { inclusive = true } }
                        } else if (user.skillsOffered.isEmpty()) {
                            navController.navigate("skill_selection") { popUpTo("auth_check") { inclusive = true } }
                        } else {
                            navController.navigate("home") { popUpTo("auth_check") { inclusive = true } }
                        }
                    }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        composable("auth") { AuthScreen(navController) }
        composable("login") {
            val authViewModel: AuthViewModel = hiltViewModel()
            LoginScreen(navController, authViewModel)
        }
        composable("register") {
            val authViewModel: AuthViewModel = hiltViewModel()
            RegisterScreen(navController, authViewModel)
        }
        composable("complete_profile") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            CompleteProfileScreen(navController, profileViewModel)
        }
        composable("skill_selection") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            SkillSelectionScreen(navController, profileViewModel)
        }
        composable ("marketplace"){
            MarketplaceScreen(navController)
        }

        composable("home") {
            val profileViewModel: ProfileViewModel = hiltViewModel()
            HomeScreen(navController, profileViewModel)
        }
        composable(
            route = "public_profile/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: ""
            PublicProfileScreen(navController, userId)
        }
    }
}
