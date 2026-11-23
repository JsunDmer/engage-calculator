package com.engage.app.ui.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engage.app.ui.common.GrowthScale
import com.engage.app.ui.common.GrowthTier
import com.engage.app.ui.common.growthColor
import com.engage.core.ClassInfo

private val NameColumnWidth = 96.dp
private val ValueColumnWidth = 44.dp
private val RowHeight = 40.dp
private val HeaderHeight = 32.dp

/**
 * 成长率对比表格：表头固定在顶部，点击列头按该列排序（再点切换升降序）。
 * 职业名列冻结在左，9 项成长率 + 合计横向滚动；表头与数据行共用横向滚动状态。
 * 整表放在同一个纵向滚动容器里、每行是单个 Row，左右天然对齐。
 */
@Composable
fun GrowthTable(classes: List<ClassInfo>, stats: List<String>, onSelect: (ClassInfo) -> Unit) {
    val valuesScroll = rememberScrollState()
    // -1 = 原始顺序；0..stats.lastIndex = 按对应成长率列；stats.size = 按合计
    var sortColumn by remember { mutableStateOf(-1) }
    var sortAsc by remember { mutableStateOf(false) }

    val sorted = remember(classes, sortColumn, sortAsc) {
        if (sortColumn < 0) return@remember classes
        val cmp = compareBy<ClassInfo> { cls ->
            if (sortColumn == stats.size) cls.growthTotal else cls.growth.getOrElse(sortColumn) { 0 }
        }
        if (sortAsc) classes.sortedWith(cmp) else classes.sortedWith(cmp.reversed())
    }

    Column(Modifier.fillMaxSize()) {
        // 固定表头：不随纵向滚动，点击切换排序
        Row(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
            SortableHeader("职业", Modifier.width(NameColumnWidth), active = sortColumn == -1, asc = false) {
                sortColumn = -1
            }
            Row(Modifier.horizontalScroll(valuesScroll)) {
                stats.forEachIndexed { i, stat ->
                    SortableHeader(stat, Modifier.width(ValueColumnWidth), active = sortColumn == i, asc = sortAsc) {
                        if (sortColumn == i) sortAsc = !sortAsc else { sortColumn = i; sortAsc = false }
                    }
                }
                SortableHeader("合计", Modifier.width(ValueColumnWidth), active = sortColumn == stats.size, asc = sortAsc) {
                    if (sortColumn == stats.size) sortAsc = !sortAsc else { sortColumn = stats.size; sortAsc = false }
                }
            }
        }
        // 数据区纵向滚动
        Column(Modifier.verticalScroll(rememberScrollState())) {
            sorted.forEachIndexed { index, cls ->
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
}

/** 可点击排序的表头单元格；激活列显示 ▲/▼ 指示方向。 */
@Composable
private fun SortableHeader(label: String, modifier: Modifier, active: Boolean, asc: Boolean, onClick: () -> Unit) {
    Box(
        modifier
            .height(HeaderHeight)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            if (active && label != "职业") "$label${if (asc) "▲" else "▼"}" else label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = if (active) 10.sp else 11.sp),
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
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