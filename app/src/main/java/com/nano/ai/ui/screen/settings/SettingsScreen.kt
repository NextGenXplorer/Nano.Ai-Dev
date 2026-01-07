package com.nano.ai.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nano.ai.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val dynamicColor by viewModel.dynamicColor.collectAsStateWithLifecycle()
    val temperature by viewModel.temperature.collectAsStateWithLifecycle()
    val topP by viewModel.topP.collectAsStateWithLifecycle()
    val topK by viewModel.topK.collectAsStateWithLifecycle()
    val maxTokens by viewModel.maxTokens.collectAsStateWithLifecycle()
    val repeatPenalty by viewModel.repeatPenalty.collectAsStateWithLifecycle()
    val contextLength by viewModel.contextLength.collectAsStateWithLifecycle()
    val systemPrompt by viewModel.systemPrompt.collectAsStateWithLifecycle()
    val showTokenCount by viewModel.showTokenCount.collectAsStateWithLifecycle()
    val showGenerationTime by viewModel.showGenerationTime.collectAsStateWithLifecycle()
    val autoScroll by viewModel.autoScroll.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Theme Section
            SettingsSection(title = "Appearance") {
                SettingsSwitchItem(
                    title = "Dark Mode",
                    subtitle = "Use dark theme",
                    icon = { Icon(Icons.Default.DarkMode, null) },
                    checked = darkMode,
                    onCheckedChange = viewModel::setDarkMode
                )
                SettingsSwitchItem(
                    title = "Dynamic Colors",
                    subtitle = "Use Material You colors",
                    icon = { Icon(Icons.Default.Palette, null) },
                    checked = dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor
                )
            }

            // Inference Settings
            SettingsSection(title = "Inference Parameters") {
                SettingsSliderItem(
                    title = "Temperature",
                    value = temperature,
                    valueRange = 0f..2f,
                    steps = 19,
                    valueDisplay = "%.2f".format(temperature),
                    onValueChange = viewModel::setTemperature
                )

                SettingsSliderItem(
                    title = "Top P",
                    value = topP,
                    valueRange = 0f..1f,
                    steps = 9,
                    valueDisplay = "%.2f".format(topP),
                    onValueChange = viewModel::setTopP
                )

                SettingsSliderItem(
                    title = "Top K",
                    value = topK.toFloat(),
                    valueRange = 1f..100f,
                    steps = 98,
                    valueDisplay = topK.toString(),
                    onValueChange = { viewModel.setTopK(it.toInt()) }
                )

                SettingsSliderItem(
                    title = "Max Tokens",
                    value = maxTokens.toFloat(),
                    valueRange = 128f..8192f,
                    steps = 62,
                    valueDisplay = maxTokens.toString(),
                    onValueChange = { viewModel.setMaxTokens(it.toInt()) }
                )

                SettingsSliderItem(
                    title = "Repeat Penalty",
                    value = repeatPenalty,
                    valueRange = 1f..2f,
                    steps = 9,
                    valueDisplay = "%.2f".format(repeatPenalty),
                    onValueChange = viewModel::setRepeatPenalty
                )

                SettingsSliderItem(
                    title = "Context Length",
                    value = contextLength.toFloat(),
                    valueRange = 512f..32768f,
                    steps = 62,
                    valueDisplay = contextLength.toString(),
                    onValueChange = { viewModel.setContextLength(it.toInt()) }
                )

                Button(
                    onClick = viewModel::resetInferenceSettings,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.RestartAlt, null)
                    Text("Reset to Defaults", modifier = Modifier.padding(start = 8.dp))
                }
            }

            // System Prompt
            SettingsSection(title = "System Prompt") {
                var editedPrompt by remember(systemPrompt) { mutableStateOf(systemPrompt) }

                OutlinedTextField(
                    value = editedPrompt,
                    onValueChange = { editedPrompt = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { Text("System Prompt") },
                    maxLines = 5
                )

                Button(
                    onClick = { viewModel.setSystemPrompt(editedPrompt) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = editedPrompt != systemPrompt
                ) {
                    Text("Save System Prompt")
                }
            }

            // UI Settings
            SettingsSection(title = "Display") {
                SettingsSwitchItem(
                    title = "Show Token Count",
                    subtitle = "Display token count for responses",
                    checked = showTokenCount,
                    onCheckedChange = viewModel::setShowTokenCount
                )
                SettingsSwitchItem(
                    title = "Show Generation Time",
                    subtitle = "Display time taken for responses",
                    checked = showGenerationTime,
                    onCheckedChange = viewModel::setShowGenerationTime
                )
                SettingsSwitchItem(
                    title = "Auto Scroll",
                    subtitle = "Automatically scroll to new messages",
                    checked = autoScroll,
                    onCheckedChange = viewModel::setAutoScroll
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon?.invoke()

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = if (icon != null) 12.dp else 0.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SettingsSliderItem(
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    steps: Int = 0,
    valueDisplay: String,
    onValueChange: (Float) -> Unit
) {
    var sliderValue by remember(value) { mutableFloatStateOf(value) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = valueDisplay,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Slider(
            value = sliderValue,
            onValueChange = { sliderValue = it },
            onValueChangeFinished = { onValueChange(sliderValue) },
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
