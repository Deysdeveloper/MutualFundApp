package com.deysdeveloper.mutualfundapp.ui.product

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.deysdeveloper.mutualfundapp.domain.model.FundDetailsResponse
import com.deysdeveloper.mutualfundapp.domain.model.NavEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreen(
    schemeCode: String,
    onBack: () -> Unit,
    viewModel: ProductViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showWatchlistSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(schemeCode) {
        viewModel.loadFund(schemeCode)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fund Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    val saved = (uiState as? ProductUiState.Success)?.isSaved ?: false
                    IconButton(onClick = { showWatchlistSheet = true }) {
                        Icon(
                            imageVector = if (saved) Icons.Filled.Bookmark else Icons.Outlined.Bookmark,
                            contentDescription = if (saved) "Remove from watchlist" else "Add to watchlist",
                            tint = if (saved) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (val state = uiState) {
                is ProductUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is ProductUiState.Error -> {
                    ProductErrorState(
                        message = state.message,
                        onRetry = { viewModel.retry(schemeCode) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is ProductUiState.Success -> {
                    ProductContent(
                        fundDetails = state.fundDetails,
                        chartData = state.chartData
                    )
                }
            }
        }
    }

    // Watchlist bottom sheet
    if (showWatchlistSheet) {
        ModalBottomSheet(
            onDismissRequest = { showWatchlistSheet = false },
            sheetState = sheetState
        ) {
            WatchlistBottomSheet(
                schemeCode = schemeCode,
                onDismiss = {
                    showWatchlistSheet = false
                    viewModel.loadFund(schemeCode) // refresh saved state
                }
            )
        }
    }
}

// ─── Content ──────────────────────────────────────────────────────────────────

@Composable
private fun ProductContent(
    fundDetails: FundDetailsResponse,
    chartData: List<NavEntry>
) {
    val latestNav = fundDetails.data.firstOrNull()?.nav ?: "N/A"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Fund name
        Text(
            text = fundDetails.meta.schemeName,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        // Fund info rows
        InfoRow(label = "AMC", value = fundDetails.meta.fundHouse)
        InfoRow(label = "Type", value = fundDetails.meta.schemeType)
        InfoRow(label = "Category", value = fundDetails.meta.schemeCategory)
        InfoRow(label = "Latest NAV", value = "₹$latestNav")

        Spacer(modifier = Modifier.height(24.dp))

        // NAV chart
        Text(
            text = "NAV History (Last 1 Year)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))

        NavLineChart(navData = chartData)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.35f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.65f)
        )
    }
}

// ─── MPAndroidChart Line Chart ────────────────────────────────────────────────

@Composable
private fun NavLineChart(navData: List<NavEntry>) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(true)
                setDrawGridBackground(false)
                legend.isEnabled = false

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                }
                axisLeft.apply {
                    setDrawGridLines(true)
                    gridColor = Color.LTGRAY
                }
                axisRight.isEnabled = false
            }
        },
        update = { chart ->
            if (navData.isEmpty()) return@AndroidView

            val entries = navData.reversed().mapIndexed { index, item ->
                Entry(index.toFloat(), item.nav.toFloatOrNull() ?: 0f)
            }

            val dataSet = LineDataSet(entries, "NAV").apply {
                color = Color.parseColor("#2196F3")
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                fillColor = Color.parseColor("#2196F3")
                fillAlpha = 30
            }

            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

// ─── Error State ──────────────────────────────────────────────────────────────

@Composable
private fun ProductErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "Failed to load fund",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}
