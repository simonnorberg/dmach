package net.simno.dmach.core

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow

@Composable
fun LightLargeText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        maxLines = 1,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun DarkLargeText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        textAlign = textAlign,
        maxLines = 1,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
fun LightMediumText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.surface,
        maxLines = 1,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun DarkMediumText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        textAlign = textAlign,
        maxLines = 1,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
fun DarkSmallText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.bodySmall
    )
}
