package com.deysdeveloper.mutualfundapp.ui.explore

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Search

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.deysdeveloper.mutualfundapp.domain.model.Fund

private const val MAX_CARDS_PER_CATEGORY = 4

// Gradients cycle if there are more categories than entries
private val categoryGradients = listOf(
    listOf(Color(0xFF1565C0), Color(0xFF42A5F5)),
    listOf(Color(0xFF2E7D32), Color(0xFF66BB6A)),
    listOf(Color(0xFF6A1B9A), Color(0xFFAB47BC)),
    listOf(Color(0xFFBF360C), Color(0xFFFFA726)),
)

@Composable
fun ExploreScreen(
    onNavigateToProduct: (String) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToCategory: (label: String, query: String) -> Unit = { _, _ -> },
    viewModel: ExploreViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ExploreHeader(onSearchClick = onNavigateToSearch)

        when (val state = uiState) {
            is ExploreUiState.Loading -> ExploreLoadingState()
            is ExploreUiState.Error   -> ExploreErrorState(
                message = state.message,
                onRetry = viewModel::loadCategories
            )
            is ExploreUiState.Success -> ExploreContent(
                categories = state.categories,
                onFundClick = onNavigateToProduct,
                onSeeAllClick = onNavigateToCategory
            )
        }
    }
}

@Composable
private fun ExploreHeader(onSearchClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D1B4A), Color(0xFF1E3A8A))
                )
            )
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp)
    ) {
        Column {
            Text(
                text = "Discover Funds",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Explore top mutual funds for your goals",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.70f)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onSearchClick),
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.14f),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = "Search",
                        tint = Color.White.copy(alpha = 0.75f),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Search mutual funds...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.60f)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreContent(
    categories: Map<String, List<Fund>>,
    onFundClick: (String) -> Unit,
    onSeeAllClick: (label: String, query: String) -> Unit
) {
    LazyColumn(contentPadding = PaddingValues(bottom = 32.dp)) {
        categories.entries.forEachIndexed { index, (categoryName, funds) ->
            item(key = categoryName) {
                val query = EXPLORE_CATEGORIES[categoryName] ?: categoryName
                CategorySection(
                    title = categoryName,
                    funds = funds.take(MAX_CARDS_PER_CATEGORY),
                    onFundClick = onFundClick,
                    onSeeAllClick = { onSeeAllClick(categoryName, query) },
                    colorIndex = index % categoryGradients.size
                )
            }
        }
    }
}

@Composable
private fun CategorySection(
    title: String,
    funds: List<Fund>,
    onFundClick: (String) -> Unit,
    onSeeAllClick: () -> Unit,
    colorIndex: Int
) {
    val gradient = categoryGradients[colorIndex]

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 4.dp, height = 22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Brush.verticalGradient(gradient))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "See All",
                style = MaterialTheme.typography.labelMedium,
                color = gradient[0],
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onSeeAllClick)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (funds.isEmpty()) {
            Text(
                text = "No funds available",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(funds, key = { it.schemeCode }) { fund ->
                    FundCard(
                        fund = fund,
                        gradient = gradient,
                        onClick = { onFundClick(fund.schemeCode.toString()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FundCard(
    fund: Fund,
    gradient: List<Color>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(Brush.horizontalGradient(gradient))
            )
            Column(modifier = Modifier.padding(14.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(gradient[0].copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                        contentDescription = null,
                        tint = gradient[0],
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = fund.schemeName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    shape = RoundedCornerShape(5.dp),
                    color = gradient[0].copy(alpha = 0.10f)
                ) {
                    Text(
                        text = "#${fund.schemeCode}",
                        style = MaterialTheme.typography.labelSmall,
                        color = gradient[0],
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ExploreLoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading funds...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun ExploreErrorState(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Oops! Something went wrong.",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onRetry,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A8A)
                )
            ) {
                Text("Try Again")
            }
        }
    }
}
