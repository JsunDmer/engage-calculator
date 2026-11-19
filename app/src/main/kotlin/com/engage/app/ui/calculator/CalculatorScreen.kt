package com.engage.app.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engage.app.ui.common.CharacterAvatar
import com.engage.app.ui.common.GrowthScale
import com.engage.app.ui.common.InfoBanner
import com.engage.app.ui.common.SectionCard
import com.engage.app.ui.common.TierBadge
import com.engage.app.ui.common.growthColor
import com.engage.app.ui.common.growthSlashText
import com.engage.app.ui.theme.Gold
import com.engage.core.Character
import com.engage.core.ClassInfo
import com.engage.core.EngageRules
import com.engage.core.Stage

@Composable
fun CalculatorScreen(
    state: CalcUiState,
    onSelectCharacter: (Int) -> Unit,
    onToggleStar: () -> Unit,
    onSelectClass: (Int, String) -> Unit,
    onLevelChange: (Int, Int?, Int?) -> Unit,
    onAddStage: () -> Unit,
    onRemoveStage: (Int) -> Unit,
    onReset: () -> Unit,
) {
    var showCharacterSheet by remember { mutableStateOf(false) }
    var classSheetStage by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            CharacterCard(state, onClick = { showCharacterSheet = true }, onToggleStar = onToggleStar)
        }
        item {
            RouteCard(
                state = state,
                onPickClass = { classSheetStage = it },
                onLevelChange = onLevelChange,
                onAddStage = onAddStage,
                onRemoveStage = onRemoveStage,
                onReset = onReset,
            )
        }
        item { GrowthCard(state) }
        item { ResultCard(state) }
        item { Spacer(Modifier.height(6.dp)) }
    }

    if (showCharacterSheet) {
        CharacterSheet(
            characters = state.dataset.characters,
            selected = state.character,
            onPick = {
                onSelectCharacter(state.dataset.characters.indexOf(it))
                showCharacterSheet = false
            },
            onDismiss = { showCharacterSheet = false },
        )
    }

    classSheetStage?.let { index ->
        val current = state.stages.getOrNull(index)
        ClassSheet(
            groups = state.classGroups,
            selected = current?.className,
            character = state.character,
            onPick = {
                onSelectClass(index, it.name)
                classSheetStage = null
            },
            onDismiss = { classSheetStage = null },
        )
    }
}

@Composable
private fun CharacterCard(state: CalcUiState, onClick: () -> Unit, onToggleStar: () -> Unit) {
    SectionCard(title = "人物") {
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .clickable(onClick = onClick)
                .padding(horizontal = 10.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = state.character.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CharacterAvatar(state.character.avatar, 54.dp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    state.character.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    "${state.character.initialClass} Lv.${state.character.joinLevel}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    state.character.weapons,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "成长合计",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    state.totalGrowth.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = growthColor(GrowthScale.characterTotal(state.totalGrowth)),
                )
            }
        }

        Spacer(Modifier.height(10.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(horizontal = 11.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("装备星玉", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.width(8.dp))
            Text(
                "全能力成长 +${state.dataset.starGrowth}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Switch(checked = state.starGem, onCheckedChange = { onToggleStar() })
        }
    }
}

@Composable
private fun RouteCard(
    state: CalcUiState,
    onPickClass: (Int) -> Unit,
    onLevelChange: (Int, Int?, Int?) -> Unit,
    onAddStage: () -> Unit,
    onRemoveStage: (Int) -> Unit,
    onReset: () -> Unit,
) {
    SectionCard(title = "转职路线") {
        state.stages.forEachIndexed { index, stage ->
            val cls = state.dataset.classOf(stage.className)
            StageCard(
                index = index,
                stage = stage,
                cls = cls,
                canRemove = state.stages.size > 1,
                onPickClass = { onPickClass(index) },
                onLevelChange = { from, to -> onLevelChange(index, from, to) },
                onRemove = { onRemoveStage(index) },
            )
            if (index != state.stages.lastIndex) Spacer(Modifier.height(9.dp))
        }

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onAddStage,
                enabled = state.stages.size < CalculatorViewModel.MAX_STAGES,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("增加一段", style = MaterialTheme.typography.labelLarge)
            }
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1.6f),
                shape = RoundedCornerShape(8.dp),
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("重置为推荐路线", style = MaterialTheme.typography.labelLarge)
            }
        }

        Spacer(Modifier.height(10.dp))

        InfoBanner(
            text = buildString {
                append("总升级次数 ${state.result.totalLevels}　星玉：${if (state.starGem) "开" else "关"}")
                if (state.isBoostedCharacter) append("\n贾恩「努力才能」：职业成长率翻倍已计入")
                state.recommendedShown?.let { append("\n推荐职业：$it") }
            },
        )
    }
}

@Composable
private fun StageCard(
    index: Int,
    stage: Stage,
    cls: ClassInfo?,
    canRemove: Boolean,
    onPickClass: () -> Unit,
    onLevelChange: (Int?, Int?) -> Unit,
    onRemove: () -> Unit,
) {
    val cap = cls?.let { EngageRules.levelCap(it.tier) } ?: EngageRules.DEFAULT_LEVEL_CAP
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TierBadge(cls?.tier ?: "?")
            Spacer(Modifier.width(8.dp))
            Text(
                "第 ${index + 1} 段",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            if (canRemove) {
                IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "删除该段",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable(onClick = onPickClass)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = cls?.name ?: "选择职业",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(9.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            LevelStepper(
                label = "起始 Lv",
                value = stage.from,
                range = 1..cap,
                onChange = { onLevelChange(it, null) },
                modifier = Modifier.weight(1f),
            )
            Text(
                "→",
                modifier = Modifier.padding(horizontal = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            LevelStepper(
                label = "结束 Lv",
                value = stage.to,
                range = 1..cap,
                onChange = { onLevelChange(null, it) },
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(7.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                "练级 ${stage.levels} 级（上限 Lv.$cap）",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val growth = cls?.growth ?: emptyList()
            Text(
                text = growthSlashText(growth, GrowthScale::classStat),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun LevelStepper(
    label: String,
    value: Int,
    range: IntRange,
    onChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    var editing by remember(value) { mutableStateOf(false) }
    var draft by remember(value) { mutableStateOf(value.toString()) }

    Column(modifier) {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(3.dp))
        Row(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(7.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(7.dp))
                .background(MaterialTheme.colorScheme.surface),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton("−", enabled = value > range.first) { onChange(value - 1) }
            Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                if (editing) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { input ->
                            draft = input.filter { it.isDigit() }.take(2)
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(textAlign = TextAlign.Center),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                    )
                } else {
                    Text(
                        value.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editing = true; draft = value.toString() }
                            .padding(vertical = 10.dp),
                        textAlign = TextAlign.Center,
                    )
                }
            }
            StepperButton("+", enabled = value < range.last) { onChange(value + 1) }
        }
        if (editing) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { editing = false }) {
                    Text("取消", style = MaterialTheme.typography.labelMedium)
                }
                TextButton(onClick = {
                    draft.toIntOrNull()?.let { onChange(it.coerceIn(range)) }
                    editing = false
                }) {
                    Text("确定", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
private fun StepperButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        Modifier
            .size(36.dp)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            fontSize = 17.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
        )
    }
}

@Composable
private fun GrowthCard(state: CalcUiState) {
    SectionCard(
        title = "成长率构成",
        subtitle = "个人成长率 + 职业成长补正 + 星玉 = 每级总成长率（%）",
    ) {
        val stageNames = state.stages.map { state.dataset.classOf(it.className)?.name ?: "—" }
        val multiplier = state.character.classGrowthMultiplier.coerceAtLeast(1)
        DataTable(
            firstHeader = "属性",
            columnHeaders = buildList {
                add("个人")
                stageNames.forEachIndexed { i, _ -> add("段${i + 1} 补正"); add("段${i + 1} 合计") }
                add("星玉")
            },
            columnSubHeaders = buildList {
                add("")
                stageNames.forEach {
                    add(it.take(4) + if (multiplier > 1) " ×2" else "")
                    add("")
                }
                add(if (state.starGem) "+${state.dataset.starGrowth}" else "—")
            },
            rows = state.growthRows.map { row ->
                buildList {
                    add(TableCell(row.personal.toString(), CellRole.Muted))
                    row.perStage.forEach { bonus ->
                        add(TableCell(if (bonus == 0) "—" else "+$bonus", CellRole.Muted))
                        val combined = row.personal + bonus + row.star
                        add(TableCell(combined.toString(), GrowthScale.combined(combined).toCellRole()))
                    }
                    add(TableCell(if (row.star > 0) "+${row.star}" else "—", CellRole.Muted))
                }
            },
            firstColumn = state.stats,
            footer = TableRowData(
                label = "合计",
                cells = buildList {
                    val personalTotal = state.character.growth.sum()
                    add(
                        TableCell(
                            personalTotal.toString(),
                            GrowthScale.characterTotal(personalTotal).toCellRole(),
                        )
                    )
                    val starPerStat = if (state.starGem) state.dataset.starGrowth else 0
                    state.stages.indices.forEach { i ->
                        val bonusTotal = state.growthRows.sumOf { it.perStage.getOrElse(i) { 0 } }
                        add(TableCell(if (bonusTotal == 0) "—" else "+$bonusTotal", CellRole.Muted))
                        val combinedTotal = personalTotal + bonusTotal + starPerStat * state.stats.size
                        // 合计是 9 项之和，按单属性均值落档，与行内配色保持一致
                        val tier = GrowthScale.combined(combinedTotal / state.stats.size.coerceAtLeast(1))
                        add(TableCell(combinedTotal.toString(), tier.toCellRole()))
                    }
                    val starTotal = starPerStat * state.stats.size
                    add(TableCell(if (starTotal > 0) "+$starTotal" else "—", CellRole.Muted))
                },
            ),
        )
    }
}

@Composable
private fun ResultCard(state: CalcUiState) {
    val result = state.result
    SectionCard(
        title = "最终能力值",
        subtitle = "${state.character.name}：${state.character.initialClass} Lv.${state.character.joinLevel} → " +
            "${state.stages.lastOrNull()?.className ?: "—"}" +
            (if (state.isBoostedCharacter) "　职业成长率 ×2" else ""),
    ) {
        DataTable(
            firstHeader = "属性",
            columnHeaders = buildList {
                add("初始")
                result.stages.forEach { add(it.className) }
                add("最终")
                add("上限")
            },
            columnSubHeaders = buildList {
                add("")
                result.stages.forEach { add("Lv.${it.from}→${it.to}") }
                add("含修正")
                add("含修正")
            },
            rows = state.stats.mapIndexed { index, _ ->
                buildList {
                    add(TableCell(state.character.base[index].toString(), CellRole.Muted))
                    result.stages.forEach { stage ->
                        val v = stage.values[index]
                        add(
                            TableCell(
                                value = v.toString(),
                                role = when {
                                    v >= stage.caps[index] -> CellRole.Hit
                                    v > state.character.base[index] -> CellRole.Up
                                    else -> CellRole.Normal
                                },
                            )
                        )
                    }
                    add(TableCell(result.values[index].toString(), CellRole.Emphasis))
                    add(TableCell(result.caps[index].toString(), CellRole.Muted))
                }
            },
            firstColumn = state.stats,
            footer = TableRowData(
                label = "合计",
                cells = buildList {
                    add(TableCell(state.character.base.sum().toString(), CellRole.Muted))
                    result.stages.forEach { stage ->
                        add(TableCell(stage.values.sum().toString(), CellRole.Normal))
                    }
                    add(TableCell(result.total.toString(), CellRole.Up))
                    add(TableCell("", CellRole.Muted))
                },
            ),
        )

        Spacer(Modifier.height(10.dp))

        if (result.errors.isNotEmpty()) {
            InfoBanner(text = result.errors.joinToString("\n"), warn = true)
            Spacer(Modifier.height(8.dp))
        }

        val tips = buildList {
            result.stages.forEachIndexed { index, stage ->
                result.missingWeapons[stage.className]?.let { missing ->
                    add("第 ${index + 1} 段 ${stage.className} 缺少资质：${missing.joinToString("、")}（需先在该职业练或转职补）")
                }
            }
            if (result.waste.isNotEmpty()) {
                add(
                    "上限溢出：" + result.waste.entries.joinToString("、") { "${it.key} ${it.value} 点" } +
                        "（超出部分作废）"
                )
            }
        }
        if (tips.isNotEmpty()) {
            InfoBanner(text = tips.joinToString("\n"))
            Spacer(Modifier.height(8.dp))
        }

        Text(
            "结算规则：每升 1 级按「个人成长率 + 当前职业成长补正 + 星玉」累加，满 100 结算 +1 并结转余数；" +
                "转职时低于新职业基础值则抬升到基础值，任何时刻不超过职业上限 + 角色上限修正。" +
                "红色 = 已达该职业上限，绿色 = 本段有提升。",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            lineHeight = 17.sp,
        )
    }
}
