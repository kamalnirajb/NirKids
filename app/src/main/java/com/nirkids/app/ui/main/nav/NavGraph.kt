package com.nirkids.app.ui.main.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.nirkids.app.ui.main.screens.*

object NavDestinations {
    const val HOME = "home"
    const val ALPHABET = "alphabet"
    const val PRONUNCIATION = "pronunciation"
    const val PARENT_GATE = "parent_gate"
}

@Composable
fun NavGraph(
    navController: NavHostController,
    ttsHelper: com.nirkids.app.ui.utils.TtsHelper,
    vibrationHelper: com.nirkids.app.ui.utils.VibrationHelper
) {
    NavHost(
        navController = navController,
        startDestination = NavDestinations.HOME,
        route = "main_nav"
    ) {
        composable(NavDestinations.HOME) {
            HomeScreen(
                onNavigateToAlphabet = { navController.navigate(NavDestinations.ALPHABET) },
                onNavigateToPronunciation = { navController.navigate(NavDestinations.PRONUNCIATION) },
                onNavigateToParentGate = { navController.navigate(NavDestinations.PARENT_GATE) }
            )
        }
        composable(NavDestinations.ALPHABET) {
            AlphabetScreen(
                onNavigateBack = { navController.popBackStack() },
                ttsHelper = ttsHelper,
                vibrationHelper = vibrationHelper
            )
        }
        composable(NavDestinations.PRONUNCIATION) {
            PronunciationScreen(
                onNavigateBack = { navController.popBackStack() },
                ttsHelper = ttsHelper
            )
        }
        composable(NavDestinations.PARENT_GATE) {
            ParentGateScreen(
                onDismiss = { navController.popBackStack() },
                onGateSuccess = { /* parent settings accessed */ }
            )
        }
    }
}
