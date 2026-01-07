package com.nano.ai.ui.screen.home_screen

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nano.ai.R
import com.nano.ai.di.AppContainer
import com.nano.ai.inference.InferenceState
import com.nano.ai.ui.components.ActionButton
import com.nano.ai.ui.components.AnimatedTitle
import com.nano.ai.ui.components.ConversationItem
import com.nano.ai.ui.theme.rDp
import com.nano.ai.viewmodel.ChatViewModel
import com.nano.ai.viewmodel.ModelManagerViewModel
import com.nano.ai.viewmodel.ThemeViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToChat: (String?) -> Unit = {},
    onNavigateToNewChat: () -> Unit = {},
    onNavigateToModels: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToHistory: () -> Unit = {}
) {
    val chatViewModel = remember { AppContainer.createChatViewModel() }
    val modelManagerViewModel = AppContainer.getModelManagerViewModel()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopBar(
                onNavigateToHistory = onNavigateToHistory,
                onNavigateToSettings = onNavigateToSettings
            )
        },
        bottomBar = {
            BottomBar(onNavigateToNewChat = onNavigateToNewChat)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToNewChat,
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(Icons.Default.Add, "New Chat")
            }
        }
    ) { paddingValues ->
        BodyContent(
            paddingValues = paddingValues,
            chatViewModel = chatViewModel,
            modelManagerViewModel = modelManagerViewModel,
            onNavigateToChat = onNavigateToChat,
            onNavigateToModels = onNavigateToModels
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    CenterAlignedTopAppBar(
        title = {
            AnimatedTitle()
        },
        navigationIcon = {
            IconButton(onClick = onNavigateToHistory) {
                Icon(Icons.Default.History, "History")
            }
        },
        actions = {
            ActionButton(
                onClickListener = onNavigateToSettings,
                R.drawable.settings,
                modifier = Modifier.padding(end = rDp(6.dp))
            )
        }
    )
}

@Composable
fun BodyContent(
    paddingValues: PaddingValues,
    chatViewModel: ChatViewModel,
    modelManagerViewModel: ModelManagerViewModel,
    onNavigateToChat: (String?) -> Unit,
    onNavigateToModels: () -> Unit,
    themeViewModel: ThemeViewModel = AppContainer.getThemeViewModel()
) {
    val isDarkTheme by themeViewModel.isDarkTheme.collectAsStateWithLifecycle()
    val conversations by chatViewModel.conversations.collectAsStateWithLifecycle()
    val inferenceState by modelManagerViewModel.inferenceState.collectAsStateWithLifecycle()

    val colorScheme = if (isDarkTheme) {
        MaterialTheme.colorScheme.background
    } else {
        MaterialTheme.colorScheme.background
    }
    val finalTheme by animateColorAsState(colorScheme, label = "theme")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(finalTheme)
            .padding(paddingValues)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Model status card
            ModelStatusCard(
                inferenceState = inferenceState,
                onNavigateToModels = onNavigateToModels
            )

            // Recent conversations
            if (conversations.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Welcome to Nano.Ai",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Start a new chat to begin",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                Text(
                    text = "Recent Chats",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(conversations.take(5), key = { it.id }) { conversation ->
                        ConversationItem(
                            title = conversation.title,
                            lastMessage = null,
                            timestamp = formatTimestamp(conversation.updatedAt),
                            onClick = { onNavigateToChat(conversation.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModelStatusCard(
    inferenceState: InferenceState,
    onNavigateToModels: () -> Unit
) {
    androidx.compose.material3.Surface(
        onClick = onNavigateToModels,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Memory,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = when (inferenceState) {
                    is InferenceState.Ready -> MaterialTheme.colorScheme.primary
                    is InferenceState.Loading -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                when (inferenceState) {
                    is InferenceState.Ready -> {
                        Text(
                            text = "Model Ready",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = inferenceState.modelName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is InferenceState.Loading -> {
                        Text(
                            text = "Loading...",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                        androidx.compose.material3.LinearProgressIndicator(
                            progress = { inferenceState.progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "No Model Loaded",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Tap to load a model",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BottomBar(
    onNavigateToNewChat: () -> Unit,
    themeViewModel: ThemeViewModel = AppContainer.getThemeViewModel()
) {
    var value by remember { mutableStateOf("") }

    Box(
        Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.primary
                    .copy(0.04f)
                    .compositeOver(MaterialTheme.colorScheme.background)
            )
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = rDp(8.dp))
                .padding(top = rDp(8.dp), bottom = rDp(10.dp))
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(max = rDp(200.dp))
            ) {
                TextField(
                    value = value,
                    onValueChange = { value = it },
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(text = "Say Anything...")
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(rDp(6.dp))) {
                ActionButton(
                    onClickListener = {},
                    R.drawable.tool,
                    modifier = Modifier.padding(start = 12.dp)
                )

                ActionButton(
                    onClickListener = {
                        themeViewModel.setDarkTheme(!themeViewModel.isDarkTheme.value)
                    },
                    R.drawable.smart_temp_message,
                    modifier = Modifier.padding(start = 12.dp)
                )

                Spacer(Modifier.weight(1f))

                ActionButton(
                    onClickListener = onNavigateToNewChat,
                    R.drawable.send_chat,
                    shape = MaterialShapes.Ghostish.toShape(),
                    modifier = Modifier.padding(end = 12.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(0.3f),
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp

    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(Date(timestamp))
    }
}
