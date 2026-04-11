package com.deysdeveloper.mutualfundapp.ui.product

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.deysdeveloper.mutualfundapp.domain.model.FundDetailsResponse
import com.deysdeveloper.mutualfundapp.domain.model.NavEntry
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlin.math.abs

// ─── Time filter options ──────────────────────────────────────────────────────

private enum class TimeFilter(val label: String, val days: Int) {
    SIX_MONTHS("6M", 182),
    ONE_YEAR("1Y", 365),
    ALL("ALL", Int.MAX_VALUE)
}

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
                title = { Text("Analysis") },
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
    @Suppress("UNUSED_PARAMETER") chartData: List<NavEntry>
) {
    val latestNav = fundDetails.data.firstOrNull()?.nav ?: "N/A"
    val previousNav = fundDetails.data.getOrNull(1)?.nav
    val navChange = computeNavChange(latestNav, previousNav)

    var selectedFilter by remember { mutableStateOf(TimeFilter.ONE_YEAR) }

    val filteredChartData = remember(selectedFilter, fundDetails.data) {
        sampleNavData(fundDetails.data, selectedFilter)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ── Fund name + category header ──────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            androidx.compose.ui.graphics.Color(0xFF0D1B4A),
                            androidx.compose.ui.graphics.Color(0xFF1E3A8A)
                        )
                    )
                )
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Text(
                text = fundDetails.meta.schemeName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.ui.graphics.Color.White,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Category: ${fundDetails.meta.schemeCategory}",
                style = MaterialTheme.typography.bodySmall,
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.70f)
            )
            Spacer(modifier = Modifier.height(12.dp))

            // NAV + change badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "NAV ₹$latestNav",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = androidx.compose.ui.graphics.Color.White
                )
                if (navChange != null) {
                    Spacer(modifier = Modifier.width(10.dp))
                    NavChangeBadge(navChange)
                }
            }
        }

        // ── Chart ────────────────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(16.dp))
        NavLineChart(
            navData = filteredChartData,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        // ── Time filter tabs ─────────────────────────────────────────────────
        Spacer(modifier = Modifier.height(12.dp))
        TimeFilterRow(
            selected = selectedFilter,
            onSelect = { selectedFilter = it },
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // ── Fund info rows ───────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            InfoRow(label = "AMC", value = fundDetails.meta.fundHouse)
            InfoRow(label = "Type", value = fundDetails.meta.schemeType)
            InfoRow(label = "Category", value = fundDetails.meta.schemeCategory)
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
        Spacer(modifier = Modifier.height(16.dp))

        // ── Stats row ────────────────────────────────────────────────────────
        StatsRow(
            type = fundDetails.meta.schemeType.take(10),
            category = fundDetails.meta.schemeCategory.take(12),
            nav = "₹$latestNav",
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

// ─── NAV change badge ─────────────────────────────────────────────────────────

private data class NavChange(val percent: Float, val isPositive: Boolean)

private fun computeNavChange(latestNav: String, previousNav: String?): NavChange? {
    val latest = latestNav.toFloatOrNull() ?: return null
    val previous = previousNav?.toFloatOrNull() ?: return null
    if (previous == 0f) return null
    val percent = ((latest - previous) / previous) * 100f
    return NavChange(percent = abs(percent), isPositive = percent >= 0f)
}

@Composable
private fun NavChangeBadge(navChange: NavChange) {
    val bgColor = if (navChange.isPositive)
        androidx.compose.ui.graphics.Color(0xFF1B5E20)
    else
        androidx.compose.ui.graphics.Color(0xFFB71C1C)

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor.copy(alpha = 0.80f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (navChange.isPositive) Icons.AutoMirrored.Filled.TrendingUp
                          else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier.height(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "%.2f%%".format(navChange.percent),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = androidx.compose.ui.graphics.Color.White
            )
        }
    }
}

// ─── Time filter row ──────────────────────────────────────────────────────────

@Composable
private fun TimeFilterRow(
    selected: TimeFilter,
    onSelect: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TimeFilter.entries.forEach { filter ->
            val isSelected = filter == selected
            val textColor = if (isSelected)
                androidx.compose.ui.graphics.Color(0xFF1E3A8A)
            else
                MaterialTheme.colorScheme.onSurfaceVariant

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(filter) }
                    .background(
                        if (isSelected) androidx.compose.ui.graphics.Color(0xFF1E3A8A).copy(alpha = 0.10f)
                        else androidx.compose.ui.graphics.Color.Transparent
                    )
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = textColor
                )
            }

            if (filter != TimeFilter.entries.last()) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

// ─── Stats row ────────────────────────────────────────────────────────────────

@Composable
private fun StatsRow(
    type: String,
    category: String,
    nav: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        StatItem(label = "Type", value = type)
        StatsDivider()
        StatItem(label = "Category", value = category)
        StatsDivider()
        StatItem(label = "NAV", value = nav)
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun StatsDivider() {
    Box(
        modifier = Modifier
            .height(36.dp)
            .width(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

// ─── Info row ─────────────────────────────────────────────────────────────────

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

// ─── Nav data sampling ────────────────────────────────────────────────────────

private fun sampleNavData(rawData: List<NavEntry>, filter: TimeFilter): List<NavEntry> {
    val limited = if (filter.days == Int.MAX_VALUE) rawData else rawData.take(filter.days)
    // Downsample to avoid overloading the chart renderer
    val step = when (filter) {
        TimeFilter.SIX_MONTHS -> 3
        TimeFilter.ONE_YEAR -> 7
        TimeFilter.ALL -> maxOf(1, limited.size / 120)
    }
    return limited.filterIndexed { index, _ -> index % step == 0 }
}

// ─── MPAndroidChart Line Chart ────────────────────────────────────────────────

@Composable
private fun NavLineChart(
    navData: List<NavEntry>,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
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
                color = Color.parseColor("#1E3A8A")
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                cubicIntensity = 0.2f
                setDrawFilled(true)
                fillColor = Color.parseColor("#1E3A8A")
                fillAlpha = 30
            }

            chart.data = LineData(dataSet)
            chart.animateX(500)
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
