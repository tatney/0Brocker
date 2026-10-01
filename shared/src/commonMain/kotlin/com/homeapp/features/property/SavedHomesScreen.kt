package com.homeapp.features.property

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homeapp.core.icons.IconBack
import com.homeapp.core.theme.kSpaceMD
import com.homeapp.core.theme.kSpaceSM
import com.homeapp.core.widgets.EmptyState
import com.homeapp.data.model.Property

/** Lists all saved/favorited properties with an empty-state fallback. */
@Composable
fun SavedHomesScreen(
    onBack: () -> Unit,
    onPropertyClick: (Long) -> Unit,
    viewModel: SavedHomesViewModel = viewModel { SavedHomesViewModel() },
) {
    val saved by viewModel.saved.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = kSpaceMD, vertical = kSpaceSM),
        verticalArrangement = Arrangement.spacedBy(kSpaceSM),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(IconBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = "Saved homes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
        if (saved.isEmpty()) {
            item {
                EmptyState(
                    title = "No saved homes yet",
                    subtitle = "Tap the bookmark on any listing to save it here.",
                )
            }
        } else {
            items(saved, key = { it.id }) { property ->
                PropertyCard(property, onClick = { onPropertyClick(property.id) })
            }
        }
    }
}
