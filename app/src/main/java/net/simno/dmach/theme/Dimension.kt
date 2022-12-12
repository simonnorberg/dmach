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
    val CircleRadius: Dp,
    val RectHeight: Dp,
    val ConfigHeight: Dp,
    val PaddingSmall: Dp,
    val PaddingMedium: Dp,
    val PaddingLarge: Dp,
    val ButtonSmall: Dp,
    val ButtonMedium: Dp,
    val ButtonLarge: Dp,
    val TextSmall: TextUnit,
    val TextMedium: TextUnit,
    val TextLarge: TextUnit,
    val LabelMedium: TextUnit
) {
    object Default : Dimensions(
        CircleRadius = 18.dp,
        RectHeight = 44.dp,
        ConfigHeight = 64.dp,
        PaddingSmall = 4.dp,
        PaddingMedium = 8.dp,
        PaddingLarge = 12.dp,
        ButtonSmall = 56.dp,
        ButtonMedium = 64.dp,
        ButtonLarge = 72.dp,
        TextSmall = 10.sp,
        TextMedium = 18.sp,
        TextLarge = 24.sp,
        LabelMedium = 14.sp
    )

    object ShortestWidth600 : Dimensions(
        CircleRadius = 24.dp,
        RectHeight = 64.dp,
        ConfigHeight = 96.dp,
        PaddingSmall = 5.dp,
        PaddingMedium = 10.dp,
        PaddingLarge = 15.dp,
        ButtonSmall = 88.dp,
        ButtonMedium = 96.dp,
        ButtonLarge = 104.dp,
        TextSmall = 12.sp,
        TextMedium = 24.sp,
        TextLarge = 30.sp,
        LabelMedium = 20.sp
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
