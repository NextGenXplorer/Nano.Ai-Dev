package com.nano.ai.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.nano.ai.di.AppContainer
import com.nano.ai.ui.screen.chat.ChatScreen
import com.nano.ai.ui.screen.history.ChatHistoryScreen
import com.nano.ai.ui.screen.home_screen.HomeScreen
import com.nano.ai.ui.screen.model_manager.ModelManagerScreen
import com.nano.ai.ui.screen.settings.SettingsScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: String = NavRoutes.Home.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(NavRoutes.Home.route) {
            HomeScreen(
                onNavigateToChat = { conversationId ->
                    navController.navigate(NavRoutes.Chat.createRoute(conversationId))
                },
                onNavigateToNewChat = {
                    navController.navigate(NavRoutes.NewChat.route)
                },
                onNavigateToModels = {
                    navController.navigate(NavRoutes.Models.route)
                },
                onNavigateToSettings = {
                    navController.navigate(NavRoutes.Settings.route)
                },
                onNavigateToHistory = {
                    navController.navigate(NavRoutes.History.route)
                }
            )
        }

        composable(
            route = NavRoutes.Chat.route,
            arguments = listOf(
                navArgument(NavRoutes.CONVERSATION_ID_ARG) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString(NavRoutes.CONVERSATION_ID_ARG)
            val chatViewModel = remember { AppContainer.createChatViewModel() }

            ChatScreen(
                viewModel = chatViewModel,
                conversationId = if (conversationId == "new") null else conversationId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToModels = { navController.navigate(NavRoutes.Models.route) }
            )
        }

        composable(NavRoutes.NewChat.route) {
            val chatViewModel = remember { AppContainer.createChatViewModel() }

            ChatScreen(
                viewModel = chatViewModel,
                conversationId = null,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToModels = { navController.navigate(NavRoutes.Models.route) }
            )
        }

        composable(NavRoutes.Models.route) {
            ModelManagerScreen(
                viewModel = AppContainer.getModelManagerViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Settings.route) {
            SettingsScreen(
                viewModel = AppContainer.getSettingsViewModel(),
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.History.route) {
            val chatViewModel = remember { AppContainer.createChatViewModel() }

            ChatHistoryScreen(
                viewModel = chatViewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToChat = { conversationId ->
                    navController.navigate(NavRoutes.Chat.createRoute(conversationId)) {
                        popUpTo(NavRoutes.History.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
