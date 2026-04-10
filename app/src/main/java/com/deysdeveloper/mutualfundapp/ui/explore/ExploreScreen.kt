package com.deysdeveloper.mutualfundapp.ui.explore

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.deysdeveloper.mutualfundapp.domain.model.Fund

// ─── Constants ────────────────────────────────────────────────────────────────

private const val MAX_CARDS_PER_CATEGORY = 4

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    onNavigateToProduct: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Explore Funds", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton(onClick = onNavigateToSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        )

        when (val state = uiState) {
            is ExploreUiState.Loading -> ExploreLoadingState()
            is ExploreUiState.Error -> ExploreErrorState(
                message = state.message,
                onRetry = viewModel::fetchCategories
            )
            is ExploreUiState.Success -> ExploreContent(
                categories = state.categories,
                onFundClick = onNavigateToProduct
            )
        }
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun ExploreContent(
    categories: Map<String, List<Fund>>,
    onFundClick: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        categories.forEach { (categoryName, funds) ->
            item(key = categoryName) {
                CategorySection(
                    title = categoryName,
                    funds = funds.take(MAX_CARDS_PER_CATEGORY),
                    onFundClick = onFundClick
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    funds: List<Fund>,
    onFundClick: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        // Section header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { /* future: navigate to category list */ }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (funds.isEmpty()) {
            Text(
                text = "No funds available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(funds, key = { it.schemeCode }) { fund ->
                    FundCard(fund = fund, onClick = { onFundClick(fund.schemeCode.toString()) })
                }
            }
        }
    }
}

@Composable
private fun FundCard(
    fund: Fund,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(180.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = fund.schemeName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Code: ${fund.schemeCode}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ─── Loading / Error states ───────────────────────────────────────────────────

@Composable
private fun ExploreLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ExploreErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Oops! Something went wrong.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}
