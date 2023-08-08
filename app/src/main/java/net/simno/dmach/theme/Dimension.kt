package net.simno.dmach.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

sealed class Dimensions(
    val circleRadius: Dp,
    val rectHeight: Dp,
    val configHeightSmall: Dp,
    val configHeight: Dp,
    val paddingSmall: Dp,
    val paddingMedium: Dp,
    val paddingLarge: Dp,
    val buttonSmall: Dp,
    val buttonMedium: Dp,
    val buttonLarge: Dp,
    val textSmall: TextUnit,
    val textMedium: TextUnit,
    val textLarge: TextUnit,
    val labelSmall: TextUnit,
    val labelMedium: TextUnit
) {
    data object Default : Dimensions(
        circleRadius = 18.dp,
        rectHeight = 44.dp,
        configHeightSmall = 48.dp,
        configHeight = 64.dp,
        paddingSmall = 4.dp,
        paddingMedium = 8.dp,
        paddingLarge = 12.dp,
        buttonSmall = 56.dp,
        buttonMedium = 64.dp,
        buttonLarge = 72.dp,
        textSmall = 16.sp,
        textMedium = 18.sp,
        textLarge = 24.sp,
        labelSmall = 10.sp,
        labelMedium = 14.sp
    )

    data object ShortestWidth600 : Dimensions(
        circleRadius = 24.dp,
        rectHeight = 64.dp,
        configHeightSmall = 72.dp,
        configHeight = 96.dp,
        paddingSmall = 5.dp,
        paddingMedium = 10.dp,
        paddingLarge = 15.dp,
        buttonSmall = 88.dp,
        buttonMedium = 96.dp,
        buttonLarge = 104.dp,
        textSmall = 22.sp,
        textMedium = 24.sp,
        textLarge = 30.sp,
        labelSmall = 12.sp,
        labelMedium = 20.sp
    )
}

@Composable
fun ProvideDimensions(
    dimensions: Dimensions,
    content: @Composable () -> Unit
) {
    val dimensionSet = remember { dimensions }
    CompositionLocalProvider(LocalDimensions provides dimensionSet, content = content)
}

internal val LocalDimensions: ProvidableCompositionLocal<Dimensions> = staticCompositionLocalOf {
    Dimensions.Default
}
