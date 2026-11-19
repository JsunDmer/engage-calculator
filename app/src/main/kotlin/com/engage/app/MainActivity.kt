package com.engage.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.engage.app.data.DatasetRepository
import com.engage.app.ui.calculator.CalculatorScreen
import com.engage.app.ui.calculator.CalculatorViewModel
import com.engage.app.ui.classes.ClassesScreen
import com.engage.app.ui.emblems.EmblemsScreen
import com.engage.app.ui.reference.ReferenceScreen
import com.engage.app.ui.theme.EngageTheme
import com.engage.app.ui.theme.Gold
import com.engage.core.Dataset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EngageTheme {
                AppRoot()
            }
        }
    }
}

private enum class Destination(
    val label: String,
    val title: String,
    val icon: ImageVector,
) {
    Calculator("计算器", "转职路线计算器", Icons.Default.Calculate),
    Classes("职业", "职业图鉴", Icons.Default.Shield),
    Emblems("纹章士", "纹章士图鉴", Icons.Default.AutoAwesome),
    Reference("基础", "基础说明", Icons.AutoMirrored.Filled.MenuBook),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val dataset by produceState<Dataset?>(initialValue = null) {
        value = withContext(Dispatchers.IO) { DatasetRepository.load(context) }
    }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    val loaded = dataset
    if (loaded == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Gold)
                Text(
                    "正在载入数据…",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp),
                )
            }
        }
        return
    }

    val destination = Destination.entries[selectedTab]
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            destination.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            "火焰纹章 Engage · 固定成长",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                Destination.entries.forEachIndexed { index, dest ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(dest.icon, contentDescription = dest.label) },
                        label = { Text(dest.label, style = MaterialTheme.typography.labelMedium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Gold,
                            selectedTextColor = Gold,
                            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    )
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (destination) {
                Destination.Calculator -> CalculatorTab(loaded)
                Destination.Classes -> ClassesScreen(loaded)
                Destination.Emblems -> EmblemsScreen(loaded)
                Destination.Reference -> ReferenceScreen(loaded)
            }
        }
    }
}

@Composable
private fun CalculatorTab(dataset: Dataset) {
    val vm: CalculatorViewModel = viewModel(key = "calculator") {
        CalculatorViewModel(dataset)
    }
    val state by vm.state.collectAsStateWithLifecycle()
    CalculatorScreen(
        state = state,
        onSelectCharacter = vm::selectCharacter,
        onToggleStar = vm::toggleStarGem,
        onSelectClass = vm::selectClass,
        onLevelChange = vm::setLevel,
        onAddStage = vm::addStage,
        onRemoveStage = vm::removeStage,
        onReset = vm::reset,
    )
}