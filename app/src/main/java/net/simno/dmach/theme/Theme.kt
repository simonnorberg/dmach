package net.simno.dmach.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val dimensions = if (configuration.smallestScreenWidthDp < 600) {
        Dimensions.Default
    } else {
        Dimensions.ShortestWidth600
    }

    ProvideDimensions(dimensions = dimensions) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                background = Surface,
                surface = Surface,
                onSurface = OnSurface,
                onSurfaceVariant = OnSurfaceVariant,
                primary = Primary,
                onPrimary = OnPrimary,
                secondary = Secondary,
                onSecondary = OnSecondary,
                tertiary = Tertiary
            ),
            shapes = Shapes,
            typography = Typography,
            content = content
        )
    }
}

object AppTheme {
    val dimens: Dimensions
        @ReadOnlyComposable
        @Composable
        get() = LocalDimensions.current
}
