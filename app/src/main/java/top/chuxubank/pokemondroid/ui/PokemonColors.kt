package top.chuxubank.pokemondroid.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

fun colorForPokemonColorName(name: String?): Color {
    return when (name?.lowercase()) {
        "black" -> Color(0xFF2C2C2C)
        "blue" -> Color(0xFF6BA8FF)
        "brown" -> Color(0xFFB97A57)
        "gray" -> Color(0xFFB0B0B0)
        "green" -> Color(0xFF7AD19A)
        "pink" -> Color(0xFFF2A7C4)
        "purple" -> Color(0xFFA58BD4)
        "red" -> Color(0xFFFF7A7A)
        "white" -> Color(0xFFF5F5F5)
        "yellow" -> Color(0xFFFFE58A)
        else -> Color(0xFFE0E0E0)
    }
}

fun readableTextColor(background: Color): Color {
    return if (background.luminance() < 0.45f) {
        Color.White
    } else {
        Color(0xFF1F1F1F)
    }
}
