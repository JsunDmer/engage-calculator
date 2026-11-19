package com.engage.app.ui.classes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engage.app.ui.common.KeyValue
import com.engage.app.ui.common.SectionCard
import com.engage.app.ui.common.TierBadge
import com.engage.app.ui.common.multiline
import com.engage.app.ui.theme.Gold
import com.engage.core.ClassInfo
import com.engage.core.Dataset

private val TierFilters = listOf("全部", "基础", "高级", "特殊")
private val SectionFilters = listOf("全部", "专属职业", "通用职业")

@Composable
fun ClassesScreen(dataset: Dataset) {
    var query by remember { mutableStateOf("") }
    var tierFilter by remember { mutableStateOf("全部") }
    var sectionFilter by remember { mutableStateOf("全部") }
    var selected by remember { mutableStateOf<ClassInfo?>(null) }

    val filtered = dataset.classes.filter { cls ->
        (tierFilter == "全部" || cls.tier == tierFilter) &&
            (sectionFilter == "全部" || cls.section == sectionFilter) &&
            (query.isBlank() || cls.name.contains(query) || cls.weapons.contains(query) || cls.skill.contains(query))
    }

    selected?.let { cls ->
        ClassDetailScreen(cls = cls, stats = dataset.stats, onBack = { selected = null })
        return
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("搜索职业 / 武器 / 特技", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { query = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "清空")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(8.dp))
            Row(
                Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                TierFilters.forEach { tier ->
                    FilterChip(
                        selected = tierFilter == tier,
                        onClick = { tierFilter = tier },
                        label = { Text(tier, style = MaterialTheme.typography.labelMedium) },
                    )
                }
                Spacer(Modifier.width(4.dp))
                SectionFilters.drop(1).forEach { section ->
                    FilterChip(
                        selected = sectionFilter == section,
                        onClick = { sectionFilter = if (sectionFilter == section) "全部" else section },
                        label = {
                            Text(
                                section.removeSuffix("职业"),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        },
                    )
                }
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            item {
                Text(
                    "共 ${filtered.size} 个职业",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(filtered, key = { it.name }) { cls ->
                ClassRow(cls = cls, onClick = { selected = cls })
            }
        }
    }
}

@Composable
private fun ClassRow(cls: ClassInfo, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TierBadge(cls.tier)
                Spacer(Modifier.width(7.dp))
                Text(cls.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                if (cls.section == "专属职业") {
                    Spacer(Modifier.width(5.dp))
                    Text(
                        "专属",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold,
                    )
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                buildString {
                    append(cls.weapons.lineSequence().firstOrNull().orEmpty())
                    if (cls.battleType.isNotEmpty()) append("　${cls.battleType}")
                },
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "成长 ${cls.growthTotal}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                "上限 ${cls.capTotal}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ClassDetailScreen(cls: ClassInfo, stats: List<String>, onBack: () -> Unit) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.Close, contentDescription = "返回列表")
                }
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(cls.name, style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.width(7.dp))
                        TierBadge(cls.tier)
                    }
                    Text(
                        "${cls.section}　移动 ${cls.move}　${cls.battleType.ifBlank { "—" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SectionCard(title = "职业特性") {
                weaponLines(cls.weapons).forEachIndexed { index, line ->
                    KeyValue(if (index == 0) "武器" else "或", line)
                }
                if (cls.skill.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    KeyValue("特技", cls.skill)
                    if (cls.skillDetail.isNotBlank()) {
                        Text(
                            cls.skillDetail,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = 74.dp),
                        )
                    }
                }
                if (cls.note.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    KeyValue("备注", cls.note)
                }
            }
        }

        item { StatTableCard(title = "基础能力值", stats = stats, values = cls.base, total = cls.base.sum()) }
        item { StatTableCard(title = "成长率（%）", stats = stats, values = cls.growth, total = cls.growthTotal) }
        item {
            StatTableCard(
                title = "职业上限",
                stats = stats,
                values = cls.caps,
                total = cls.capTotal,
                subNote = "实际上限 = 职业上限 + 角色上限修正（力~魔防，HP 与体不受影响）",
            )
        }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

private fun weaponLines(text: String): List<String> = multiline(text)

@Composable
private fun StatTableCard(
    title: String,
    stats: List<String>,
    values: List<Int>,
    total: Int,
    subNote: String? = null,
) {
    SectionCard(title = title, subtitle = subNote) {
        // 9 项属性在手机上会超宽，按两行排列以保持可读性
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            stats.chunked(5).forEachIndexed { chunkIndex, rowStats ->
                val offset = chunkIndex * 5
                Row(Modifier.fillMaxWidth()) {
                    rowStats.forEachIndexed { i, stat ->
                        val value = values.getOrElse(offset + i) { 0 }
                        Column(
                            Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                stat,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                value.toString(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        if (i != rowStats.lastIndex) Spacer(Modifier.width(4.dp))
                    }
                    repeat(5 - rowStats.size) {
                        Spacer(Modifier.weight(1f))
                        Spacer(Modifier.width(4.dp))
                    }
                }
                if (chunkIndex == 0 && values.size > 5) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Text("合计 ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    total.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Gold,
                )
            }
        }
    }
}