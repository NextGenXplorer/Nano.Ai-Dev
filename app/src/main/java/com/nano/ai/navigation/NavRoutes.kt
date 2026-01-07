package com.nano.ai.navigation

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Chat : NavRoutes("chat/{conversationId}") {
        fun createRoute(conversationId: String?) = "chat/${conversationId ?: "new"}"
    }
    data object NewChat : NavRoutes("chat/new")
    data object Models : NavRoutes("models")
    data object Settings : NavRoutes("settings")
    data object History : NavRoutes("history")

    companion object {
        const val CONVERSATION_ID_ARG = "conversationId"
    }
}
