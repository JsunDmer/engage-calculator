package com.engage.app.ui.emblems

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.engage.app.ui.common.AssetAvatar
import com.engage.app.ui.common.InfoBanner
import com.engage.app.ui.common.KeyValue
import com.engage.app.ui.common.SectionCard
import com.engage.app.ui.common.SkillIcon
import com.engage.app.ui.common.multiline
import com.engage.app.ui.theme.Gold
import com.engage.app.ui.theme.GoldLight
import com.engage.app.ui.theme.WarmInk
import com.engage.core.Dataset
import com.engage.core.Emblem
import com.engage.core.EmblemSkill

private val SectionFilters = listOf("全部", "本篇", "DLC")
private val ViewFilters = listOf("纹章士", "资质", "牵绊戒指")

@Composable
fun EmblemsScreen(dataset: Dataset) {
    var query by remember { mutableStateOf("") }
    var sectionFilter by remember { mutableStateOf("全部") }
    var view by remember { mutableStateOf("纹章士") }
    var selected by remember { mutableStateOf<Emblem?>(null) }

    selected?.let { emblem ->
        EmblemDetailScreen(emblem = emblem, dataset = dataset, onBack = { selected = null })
        return
    }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ViewFilters.forEach { label ->
                FilterChip(
                    selected = view == label,
                    onClick = { view = label },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                )
            }
        }

        if (view == "资质") {
            QualificationsScreen(dataset = dataset, onOpenEmblem = { selected = it })
            return@Column
        }

        if (view == "牵绊戒指") {
            BondRingsScreen(dataset = dataset)
            return@Column
        }

        EmblemList(dataset, query, { query = it }, sectionFilter, { sectionFilter = it }) { selected = it }
    }
}

@Composable
private fun EmblemList(
    dataset: Dataset,
    query: String,
    onQueryChange: (String) -> Unit,
    sectionFilter: String,
    onSectionChange: (String) -> Unit,
    onSelect: (Emblem) -> Unit,
) {
    val filtered = dataset.emblems.filter { emblem ->
        (sectionFilter == "全部" || emblem.section == sectionFilter) &&
            (query.isBlank() || emblem.name.contains(query) || emblem.chapter.contains(query))
    }

    Column(Modifier.fillMaxSize()) {
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("搜索纹章士 / 章节", style = MaterialTheme.typography.bodySmall) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) {
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
                        onClick = { onSectionChange(section) },
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
                EmblemRow(emblem = emblem, onClick = { onSelect(emblem) })
            }
        }
    }
}

@Composable
internal fun EmblemRow(emblem: Emblem, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssetAvatar(emblem.avatar, 42.dp, fallback = emblem.name.take(1))
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
    val syncLines = remember(emblem, dataset) { chainLinesOf(dataset, emblem, "同步") }
    val inheritLines = remember(emblem, dataset) { chainLinesOf(dataset, emblem, "继承") }
    val engageWeapons = remember(emblem, dataset) { skillsOf(dataset, emblem, "结合武器") }
    val engageCmds = remember(emblem, dataset) { skillsOf(dataset, emblem, "结合指令技") }
    val engageSkills = remember(emblem, dataset) { skillsOf(dataset, emblem, "结合特技") }
    val unlocks = remember(emblem, dataset) {
        skillsOf(dataset, emblem, "解锁功能") + skillsOf(dataset, emblem, "解锁资质")
    }
    val qualifications = remember(emblem, dataset) {
        skillsOf(dataset, emblem, "解锁资质")
    }
    val bondRings = remember(emblem, dataset) {
        dataset.rings.filter { it.emblem == emblem.name }
    }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回列表")
                }
                AssetAvatar(emblem.avatar, 56.dp, fallback = emblem.name.take(1))
                Spacer(Modifier.width(12.dp))
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
                Spacer(Modifier.height(8.dp))
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
            }
        }

        if (qualifications.isNotEmpty()) {
            item {
                SectionCard(title = "提供的资质") {
                    qualifications.forEachIndexed { index, skill ->
                        SkillLine(skill, iconPath = dataset.skillIcon(skill.name), emphasizeName = true)
                        if (index != qualifications.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        if (bondRings.isNotEmpty()) {
            item {
                SectionCard(
                    title = "牵绊戒指",
                    subtitle = "${bondRings.size} 枚　C→B→A→S 递进，S 级解锁戒指特技",
                ) {
                    bondRings.forEachIndexed { index, ring ->
                        RingRow(ring)
                        if (index != bondRings.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 7.dp),
                            )
                        }
                    }
                }
            }
        }

        if (syncLines.isNotEmpty()) {
            item {
                ChainSection(
                    title = "同步特技",
                    subtitle = "装备该纹章士即生效，随牵绊等级递进",
                    lines = syncLines,
                    dataset = dataset,
                )
            }
        }

        if (engageWeapons.isNotEmpty() || engageCmds.isNotEmpty() || engageSkills.isNotEmpty()) {
            item {
                SectionCard(title = "结合", subtitle = "结合状态下可用的武器、指令与特技") {
                    EngageGroup("结合武器", engageWeapons, dataset)
                    if (engageCmds.isNotEmpty()) {
                        EngageGroup("结合指令技", engageCmds, dataset)
                    }
                    if (engageSkills.isNotEmpty()) {
                        EngageGroup("结合特技", engageSkills, dataset)
                    }
                }
            }
        }

        if (inheritLines.isNotEmpty()) {
            item {
                ChainSection(
                    title = "继承特技",
                    subtitle = "花费 SP 继承并装备后才生效",
                    lines = inheritLines,
                    dataset = dataset,
                )
            }
        }

        if (unlocks.isNotEmpty()) {
            item {
                SectionCard(title = "解锁功能", subtitle = "牵绊等级提升后开放") {
                    unlocks.forEachIndexed { index, skill ->
                        SkillLine(skill, iconPath = dataset.skillIcon(skill.name))
                        if (index != unlocks.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        item {
            InfoBanner(text = "同步特技只对被装备纹章士的角色生效；继承特技需花费 SP 继承并装备，二者同名时不叠加。")
        }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

/** 一组「名称 + 等级」明细，用于结合武器/指令技/特技这种非递进内容。 */
@Composable
private fun EngageGroup(title: String, skills: List<EmblemSkill>, dataset: Dataset) {
    if (skills.isEmpty()) return
    Text(
        title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = Gold,
        modifier = Modifier.padding(top = 2.dp, bottom = 4.dp),
    )
    skills.forEachIndexed { index, skill ->
        SkillLine(skill, iconPath = dataset.skillIcon(skill.name))
        if (index != skills.lastIndex) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                modifier = Modifier.padding(vertical = 6.dp),
            )
        }
    }
    Spacer(Modifier.height(4.dp))
}

@Composable
private fun SkillLine(skill: EmblemSkill, iconPath: String, emphasizeName: Boolean = false) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SkillIcon(iconPath, 22.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                skill.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (emphasizeName) FontWeight.Bold else FontWeight.SemiBold,
                color = if (emphasizeName) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(6.dp))
            Text(
                skill.bondLevel,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.weight(1f))
            if (skill.sp.isNotBlank() && skill.sp != "—" && skill.sp != "--") {
                Text("${skill.sp} SP", style = MaterialTheme.typography.labelSmall, color = Gold)
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

/** 一条技能链占一行：横向滚动展示各阶，避免同类技能重复铺开。 */
@Composable
private fun ChainSection(title: String, subtitle: String, lines: List<ChainLine>, dataset: Dataset) {
    val notes = lines.map { it.note }.filter { it.isNotBlank() }.distinct()
    SectionCard(title = title, subtitle = subtitle) {
        lines.forEachIndexed { index, line ->
            ChainRow(line, dataset)
            if (index != lines.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
        }
        notes.forEach { note ->
            Spacer(Modifier.height(6.dp))
            Text(
                "※ $note",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
            )
        }
    }
}

@Composable
private fun ChainRow(line: ChainLine, dataset: Dataset) {
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                line.family,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(62.dp),
            )
            Row(
                Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                line.steps.forEachIndexed { index, (name, meta) ->
                    if (index > 0) {
                        Text(
                            "›",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 3.dp),
                        )
                    }
                    ChainStep(
                        name = name,
                        meta = meta,
                        iconPath = dataset.skillIcon(name),
                        isFinal = index == line.steps.lastIndex,
                    )
                }
                if (line.totalSp > 0) {
                    Column(
                        Modifier.padding(start = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            "合计",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "${line.totalSp} SP",
                            style = MaterialTheme.typography.labelSmall,
                            color = Gold,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }
        }
        if (line.maxEffect.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "满级：${line.maxEffect}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 62.dp),
            )
        }
    }
}

@Composable
private fun ChainStep(name: String, meta: String, iconPath: String, isFinal: Boolean) {
    Column(
        Modifier
            .widthIn(max = 118.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFinal) GoldLight else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkillIcon(iconPath, 26.dp)
        Spacer(Modifier.height(2.dp))
        Text(
            name,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isFinal) WarmInk else MaterialTheme.colorScheme.onSurface,
        )
        if (meta.isNotBlank()) {
            Text(
                meta,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}