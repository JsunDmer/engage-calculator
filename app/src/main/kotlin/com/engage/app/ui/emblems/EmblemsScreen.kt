package com.engage.app.ui.emblems

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.engage.app.ui.common.InfoBanner
import com.engage.app.ui.common.KeyValue
import com.engage.app.ui.common.SectionCard
import com.engage.app.ui.common.multiline
import com.engage.app.ui.theme.Gold
import com.engage.app.ui.theme.Navy
import com.engage.core.Dataset
import com.engage.core.Emblem
import com.engage.core.EmblemSkill
import com.engage.core.SkillChain

private val SectionFilters = listOf("全部", "本篇", "DLC")
private val KindOrder = listOf(
    "同步特技", "结合武器", "结合指令技", "结合特技", "继承特技", "解锁资质", "解锁功能",
)

/** 三鼎纹章士的技能链与三位学级长的技能共用，这里做一次归属映射。 */
private fun aliasesOf(emblem: Emblem): Set<String> = buildSet {
    add(emblem.name)
    when (emblem.name) {
        "艾黛尔贾特", "帝弥托利", "库罗德" -> add("三鼎")
        "鲁弗莱" -> add("库洛武")
    }
}

/** 技能表里的纹章士字段可能是「库洛武 / 鲁弗莱」这种合写形式。 */
private fun skillBelongsTo(skill: EmblemSkill, emblem: Emblem): Boolean =
    skill.emblem.split('/', '／').any { it.trim() == emblem.name } ||
        skill.emblem == emblem.name

@Composable
fun EmblemsScreen(dataset: Dataset) {
    var query by remember { mutableStateOf("") }
    var sectionFilter by remember { mutableStateOf("全部") }
    var selected by remember { mutableStateOf<Emblem?>(null) }

    selected?.let { emblem ->
        EmblemDetailScreen(emblem = emblem, dataset = dataset, onBack = { selected = null })
        return
    }

    val filtered = dataset.emblems.filter { emblem ->
        (sectionFilter == "全部" || emblem.section == sectionFilter) &&
            (query.isBlank() || emblem.name.contains(query) || emblem.chapter.contains(query))
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("搜索纹章士 / 章节", style = MaterialTheme.typography.bodySmall) },
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
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SectionFilters.forEach { section ->
                    FilterChip(
                        selected = sectionFilter == section,
                        onClick = { sectionFilter = section },
                        label = { Text(section, style = MaterialTheme.typography.labelMedium) },
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
                    "共 ${filtered.size} 位纹章士　技能 ${dataset.skills.size} 条　进化链 ${dataset.chains.size} 条",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(filtered, key = { it.name }) { emblem ->
                EmblemRow(emblem = emblem, onClick = { selected = emblem })
            }
        }
    }
}

@Composable
private fun EmblemRow(emblem: Emblem, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (emblem.section == "DLC") Gold else Navy),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                emblem.name.take(1),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.surface,
            )
        }
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emblem.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(6.dp))
                Text(
                    emblem.chapter,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                emblem.maxBondBonus.replace('\n', ' '),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            emblem.section,
            style = MaterialTheme.typography.labelSmall,
            color = if (emblem.section == "DLC") Gold else MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun EmblemDetailScreen(emblem: Emblem, dataset: Dataset, onBack: () -> Unit) {
    val skills = remember(emblem, dataset) {
        dataset.skills.filter { skillBelongsTo(it, emblem) }
            .sortedBy { KindOrder.indexOf(it.kind).let { i -> if (i < 0) 99 else i } }
            .groupBy { it.kind }
            .toList()
            .sortedBy { (kind, _) -> KindOrder.indexOf(kind).let { i -> if (i < 0) 99 else i } }
    }
    val aliases = remember(emblem) { aliasesOf(emblem) }
    val chains = remember(emblem, dataset) {
        dataset.chains.filter { it.emblem in aliases }
            .sortedBy { if (it.kind == "同步") 0 else 1 }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回列表")
                }
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (emblem.section == "DLC") Gold else Navy),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        emblem.name.take(1),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.surface,
                    )
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(emblem.name, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${emblem.section}　${emblem.chapter}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item {
            SectionCard(title = "基本信息") {
                KeyValue("获得", emblem.joinCondition)
                Spacer(Modifier.height(6.dp))
                Text(
                    "满级牵绊加成",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    multiline(emblem.maxBondBonus).forEach { bonus ->
                        Text(
                            bonus,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.secondaryContainer)
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                KeyValue("初始同步", emblem.firstSyncSkill)
                Spacer(Modifier.height(4.dp))
                KeyValue("初始继承", emblem.firstInheritSkill)
                Spacer(Modifier.height(4.dp))
                KeyValue("初始结合", emblem.firstEngageSkill)
                Spacer(Modifier.height(4.dp))
                KeyValue("初始连携", emblem.firstComboSkill)
                Spacer(Modifier.height(4.dp))
                KeyValue("结合武器", emblem.firstEngageWeapon)
            }
        }

        item {
            SectionCard(title = "解锁流程", subtitle = "牵绊等级提升后开启的能力") {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    multiline(emblem.unlocks).forEach { line ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(Gold),
                            )
                            Spacer(Modifier.width(7.dp))
                            Text(line, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }

        if (chains.isNotEmpty()) {
            item {
                Text(
                    "技能进化链",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            items(chains, key = { "${it.kind}-${it.family}" }) { chain ->
                ChainCard(chain)
            }
        }

        if (skills.isNotEmpty()) {
            item {
                Text(
                    "全部技能",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
            items(skills) { (kind, list) ->
                SectionCard(title = kind, subtitle = "${list.size} 个") {
                    list.forEachIndexed { index, skill ->
                        SkillRow(skill)
                        if (index != list.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        }

        item {
            InfoBanner(text = "同步特技在被装备纹章士的角色身上生效；继承特技需在纹章士之间花费 SP 继承并装备后才生效，二者同名时不叠加。")
        }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun SkillRow(skill: EmblemSkill) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(skill.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.width(6.dp))
            Text(
                skill.bondLevel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            if (skill.sp.isNotBlank() && skill.sp != "—") {
                Text(
                    "${skill.sp} SP",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gold,
                )
            }
        }
        if (skill.detail.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                skill.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (skill.note.isNotBlank()) {
            Spacer(Modifier.height(2.dp))
            Text(
                skill.note,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
    }
}

@Composable
private fun ChainCard(chain: SkillChain) {
    SectionCard(title = chain.family, subtitle = "${chain.kind}特技进化　合计 ${chain.totalSp} SP") {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            chain.steps.forEachIndexed { index, step ->
                val parts = step.split('\n')
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier
                            .padding(top = 3.dp)
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(if (index == chain.steps.lastIndex) Gold else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "${index + 1}",
                            fontSize = 9.sp,
                            color = if (index == chain.steps.lastIndex) MaterialTheme.colorScheme.surface
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            parts.firstOrNull().orEmpty(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        if (parts.size > 1) {
                            Text(
                                parts[1],
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
            if (chain.finalEffect.isNotBlank()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text(
                    "满级效果：${chain.finalEffect}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            if (chain.note.isNotBlank()) {
                Text(
                    chain.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                )
            }
        }
    }
}