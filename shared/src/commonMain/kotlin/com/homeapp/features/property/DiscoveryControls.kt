package com.homeapp.features.property

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.homeapp.data.AppContainer
import com.homeapp.data.database.DatabaseProvider
import com.homeapp.data.model.AuthState
import com.homeapp.data.model.Property
import com.homeapp.db.Saved_searches
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun DiscoveryControls(vm: PropertyViewModel) {
    val filter by vm.filter.collectAsState()
    val query by vm.searchQuery.collectAsState()
    var saved by remember { mutableStateOf<List<Saved_searches>>(emptyList()) }
    var userId by remember { mutableStateOf(0L) }
    val scope = rememberCoroutineScope()
    val q = DatabaseProvider.database.homeAppDatabaseQueries
    LaunchedEffect(Unit) {
        userId = (AppContainer.authRepository.observeSession().first() as? AuthState.SignedIn)?.user?.id ?: 0
        q.selectSavedSearches(userId).asFlow().mapToList(Dispatchers.Default).collect { saved = it }
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Find your fit", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(value = filter.maxPrice?.toString() ?: "", onValueChange = { vm.onBudgetChange(it.filter(Char::isDigit).take(13)) },
            label = { Text("Maximum asking price (UGX)") }, supportingText = { Text("Compare like periods: monthly rent and sale totals differ.") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Row { Checkbox(checked = filter.verifiedOnly, onCheckedChange = vm::onVerifiedChange)
            Text("Sample checked listings only", modifier = Modifier.padding(top = 12.dp), style = MaterialTheme.typography.bodyMedium) }
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("Recommended" to "RECOMMENDED", "Price: low first" to "PRICE_ASC", "Price: high first" to "PRICE_DESC").forEach { (label, value) ->
                FilterChip(selected = filter.sort == value, onClick = { vm.onSortChange(value) }, label = { Text(label) })
            }
        }
        TextButton(enabled = userId > 0, onClick = { scope.launch {
            q.insertSavedSearch(userId, query, filter.listingType, filter.assetType, filter.category, filter.maxPrice, if (filter.verifiedOnly) 1L else 0L, filter.sort)
        } }) { Text("Save this search on device") }
        if (saved.isNotEmpty()) {
            Text("Saved searches", style = MaterialTheme.typography.labelLarge)
            saved.take(5).forEach { s ->
                Row(Modifier.fillMaxWidth()) {
                    TextButton(modifier = Modifier.weight(1f), onClick = {
                        vm.applySearch(s.query, PropertyFilter(s.listing_type, s.asset_type, s.category, s.max_price, s.verified_only == 1L, s.sort))
                    }) { Text(s.query.ifBlank { "All locations" } + " � " + (s.listing_type ?: "rent / buy") + " � " + (s.max_price?.toString() ?: "any budget")) }
                    TextButton(onClick = { scope.launch { q.deleteSavedSearch(s.id, userId) } }) { Text("Delete") }
                }
            }
        }
    }
}

@Composable
fun PropertyComparison(rows: List<Property>, onDismiss: () -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Compare directly") },
        text = { Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            rows.forEach { p -> Column {
                Text(p.title, style = MaterialTheme.typography.titleSmall)
                Text(p.formatPrice() + " � " + p.priceUnitLabel())
                Text(p.city + " � " + p.assetType.lowercase() + " � " + p.category.lowercase())
                if (p.assetType == "HOUSE") Text(p.bedrooms)
                Text(if (p.ownerDeclared) "Owner declared; verification pending" else "Sample listing")
                Text("Brokerage: UGX 0")
            } }
        } }, confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } })
}
