package com.engage.app.ui.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.engage.app.ui.common.GrowthScale
import com.engage.app.ui.common.GrowthTier
import com.engage.app.ui.common.growthColor
import com.engage.core.ClassInfo

private val NameColumnWidth = 96.dp
private val ValueColumnWidth = 44.dp
private val RowHeight = 40.dp
private val HeaderHeight = 30.dp

/**
 * 成长率对比表格：职业名列冻结在左，9 项成长率 + 合计横向滚动。
 * 整表放在同一个纵向滚动容器里、每行是单个 Row，左右天然对齐；
 * 此前左右两个 LazyColumn 共享 state 会因可见区宽度不同而错位，已弃用。
 * 53 行规模全量组合开销可忽略，无需懒加载。
 */
@Composable
fun GrowthTable(classes: List<ClassInfo>, stats: List<String>, onSelect: (ClassInfo) -> Unit) {
    // 数值区共用一个横向滚动状态，保证各行与表头一起平移
    val valuesScroll = rememberScrollState()
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        Row {
            TableHeaderCell("职业", Modifier.width(NameColumnWidth))
            Row(Modifier.horizontalScroll(valuesScroll)) {
                stats.forEach { stat ->
                    TableHeaderCell(stat, Modifier.width(ValueColumnWidth))
                }
                TableHeaderCell("合计", Modifier.width(ValueColumnWidth))
            }
        }
        classes.forEachIndexed { index, cls ->
            Row(
                Modifier
                    .height(RowHeight)
                    .background(rowBackground(index))
                    .clickable { onSelect(cls) },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.width(NameColumnWidth).padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Text(
                        cls.name,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Row(Modifier.horizontalScroll(valuesScroll)) {
                    stats.forEachIndexed { statIndex, _ ->
                        val value = cls.growth.getOrElse(statIndex) { 0 }
                        GrowthCell(value, GrowthScale.classStat(value))
                    }
                    GrowthCell(cls.growthTotal, GrowthScale.classTotal(cls.growthTotal), bold = true)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(HeaderHeight)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun GrowthCell(value: Int, tier: GrowthTier, bold: Boolean = false) {
    Box(
        Modifier.width(ValueColumnWidth).height(RowHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            value.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium,
            color = growthColor(tier),
        )
    }
}

/** 隔行底色便于横向追读同一职业的各项数值。 */
@Composable
private fun rowBackground(index: Int) =
    if (index % 2 == 1) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    else MaterialTheme.colorScheme.surface