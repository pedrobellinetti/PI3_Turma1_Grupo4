package com.example.superid.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.superid.R

// Fonte Inter
val Inter = FontFamily(
    Font(R.font.inter_18pt_regular, FontWeight.Normal), // Ou R.font.inter_24pt_regular, ou R.font.inter_regular
    Font(R.font.inter_18pt_medium, FontWeight.Medium),   // Ou R.font.inter_24pt_medium, ou R.font.inter_medium
    Font(R.font.inter_18pt_bold, FontWeight.Bold)     // Ou R.font.inter_24pt_bold, ou R.font.inter_bold
)

// Tipografia personalizada
val AppTypography = Typography(
    headlineLarge = TextStyle( // Títulos
        fontFamily = Inter,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp
    ),
    titleLarge = TextStyle( // Subtítulo "Login sem senha"
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    bodyMedium = TextStyle( // Texto padrão
        fontFamily = Inter,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    labelLarge = TextStyle( // Botões, rótulos de campos
        fontFamily = Inter,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    )
)