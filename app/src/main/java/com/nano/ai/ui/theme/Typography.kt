package com.nano.ai.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import com.nano.ai.R

@OptIn(ExperimentalTextApi::class)
val ManropeFontFamily = FontFamily(
    Font(
        resId = R.font.manrope,  // your filename without .ttf
        variationSettings = FontVariation.Settings(
            FontVariation.weight(FontWeight.Normal.weight)
        )
    )
)