package com.engage.app.ui.calculator

import androidx.lifecycle.ViewModel
import com.engage.core.Character
import com.engage.core.ClassInfo
import com.engage.core.Dataset
import com.engage.core.EngageRules
import com.engage.core.GrowthRow
import com.engage.core.RouteResult
import com.engage.core.Stage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CalcUiState(
    val dataset: Dataset,
    val character: Character,
    val stages: List<Stage>,
    val starGem: Boolean,
    val result: RouteResult,
    val growthRows: List<GrowthRow>,
    /** 该角色可选职业，按「专属 / 通用」分组，保留原始顺序。 */
    val classGroups: List<Pair<String, List<ClassInfo>>>,
) {
    val stats: List<String> get() = dataset.stats
    val totalGrowth: Int get() = character.growth.sum()
    val isBoostedCharacter: Boolean get() = character.classGrowthMultiplier > 1
    val recommendedShown: String?
        get() = character.recommendedClass
            .takeIf { it.isNotEmpty() && it != stages.lastOrNull()?.className }
}

class CalculatorViewModel(private val dataset: Dataset) : ViewModel() {

    private var selectedIndex = 0
    private var stages: List<Stage> = EngageRules.defaultRoute(dataset.characters[selectedIndex], dataset)
    private var starGem = false

    private val _state = MutableStateFlow(build())
    val state: StateFlow<CalcUiState> = _state.asStateFlow()

    fun selectCharacter(index: Int) {
        if (index !in dataset.characters.indices) return
        selectedIndex = index
        stages = EngageRules.defaultRoute(dataset.characters[index], dataset)
        refresh()
    }

    fun toggleStarGem() {
        starGem = !starGem
        refresh()
    }

    fun selectClass(stageIndex: Int, className: String) {
        val current = stages.getOrNull(stageIndex) ?: return
        val updated = EngageRules.autoLevelFor(stageIndex, current.copy(className = className), character(), dataset)
        stages = stages.replaceAt(stageIndex, updated)
        refresh()
    }

    fun setLevel(stageIndex: Int, from: Int? = null, to: Int? = null) {
        val current = stages.getOrNull(stageIndex) ?: return
        val max = dataset.classOf(current.className)?.let { EngageRules.levelCap(it.tier) }
            ?: EngageRules.DEFAULT_LEVEL_CAP
        val updated = current.copy(
            from = (from ?: current.from).coerceIn(1, max),
            to = (to ?: current.to).coerceIn(1, max),
        )
        stages = stages.replaceAt(stageIndex, updated)
        refresh()
    }

    fun addStage() {
        if (stages.size >= MAX_STAGES) return
        val allowed = allowedClasses()
        val last = stages.lastOrNull()?.let { dataset.classOf(it.className) }
        // 与原网页一致：上一段是基础职业时补一段高级职业，否则沿用当前职业再练一轮。
        val target = if (last == null || last.tier == "基础") {
            allowed.firstOrNull { it.tier == "高级" } ?: allowed.firstOrNull()
        } else {
            last
        } ?: return
        stages = stages + Stage(target.name, 1, EngageRules.levelCap(target.tier))
        refresh()
    }

    fun removeStage(stageIndex: Int) {
        if (stages.size <= 1) return
        stages = stages.filterIndexed { i, _ -> i != stageIndex }
        refresh()
    }

    fun reset() {
        stages = EngageRules.defaultRoute(character(), dataset)
        refresh()
    }

    private fun character(): Character = dataset.characters[selectedIndex]

    private fun allowedClasses(): List<ClassInfo> = EngageRules.classesFor(character(), dataset)

    private fun refresh() {
        _state.value = build()
    }

    private fun build(): CalcUiState {
        val character = character()
        return CalcUiState(
            dataset = dataset,
            character = character,
            stages = stages,
            starGem = starGem,
            result = EngageRules.compute(character, stages, starGem, dataset),
            growthRows = EngageRules.growthRows(character, stages, starGem, dataset),
            classGroups = allowedClasses().groupBy { it.section }.map { it.key to it.value },
        )
    }

    companion object {
        const val MAX_STAGES = 4
    }
}

private fun <T> List<T>.replaceAt(index: Int, value: T): List<T> =
    toMutableList().also { it[index] = value }