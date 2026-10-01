package com.homeapp.core.widgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.homeapp.core.icons.IconArrowForward
import com.homeapp.core.icons.IconSearch
import com.homeapp.core.theme.kRadiusLG

/** Shared search field; a submit action is shown only when supplied. */
@Composable
fun AppSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search properties, services...",
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = kRadiusLG,
    height: Dp = 56.dp,
    onSearch: (() -> Unit)? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth().heightIn(min = height),
        shape = shape,
        color = containerColor,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant) },
            leadingIcon = { Icon(IconSearch, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp)) },
            trailingIcon = if (onSearch != null) {
                { IconButton(onClick = onSearch) { Icon(IconArrowForward, contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.primary) } }
            } else null,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.primary,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}