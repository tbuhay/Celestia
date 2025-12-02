package com.example.celestia.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A settings row containing an icon, title, subtitle, and a switch toggle.
 *
 * Used for boolean user preferences such as:
 * - Dark mode
 * - 24-hour format
 * - Auto-refresh on launch
 * - Location usage
 * - Notification preferences
 *
 * @param title Main label for the setting.
 * @param subtitle Additional explanatory text shown below the title.
 * @param icon Leading icon representing the setting.
 * @param checked Whether the toggle is currently enabled.
 * @param onCheckedChange Callback invoked when the toggle state changes.
 */
@Composable
fun SettingsToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(28.dp)
                    .padding(end = 12.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    maxLines = Int.MAX_VALUE
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.semantics {
                contentDescription = title
            }
        )
    }
}

/**
 * A settings row used for actions rather than toggles.
 *
 * Examples:
 * - Clear cache
 * - Reset preferences
 * - Manage notification settings
 *
 * Contains:
 * - Icon
 * - Title
 * - Subtitle
 * - A trailing action button (e.g., "Clear", "Open", "Reset")
 *
 * @param title Main label describing the action.
 * @param subtitle Additional context or explanation.
 * @param icon Leading icon representing the action.
 * @param actionText Text for the trailing action button.
 * @param onClick Callback invoked when the action button is pressed.
 */
@Composable
fun SettingsActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    actionText: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        TextButton(onClick = onClick) {
            Text(actionText, color = MaterialTheme.colorScheme.onErrorContainer)
        }
    }
}
