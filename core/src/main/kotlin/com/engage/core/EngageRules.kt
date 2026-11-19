package com.engage.core

/** 转职路线中的一段：在某个职业里从 from 级练到 to 级。 */
data class Stage(
    val className: String,
    val from: Int,
    val to: Int,
) {
    val levels: Int get() = (to - from).coerceAtLeast(0)
}

data class StageResult(
    val className: String,
    val tier: String,
    val from: Int,
    val to: Int,
    /** 该段结束时的能力值。 */
    val values: List<Int>,
    /** 该段开始时的能力值（含转职时抬升到职业基础值）。 */
    val before: List<Int>,
    val caps: List<Int>,
) {
    val levels: Int get() = (to - from).coerceAtLeast(0)
}

data class RouteResult(
    val values: List<Int>,
    val caps: List<Int>,
    val stages: List<StageResult>,
    /** 属性名 -> 因职业上限被吞掉的点数。 */
    val waste: Map<String, Int>,
    val errors: List<String>,
    /** 职业名 -> 该角色缺失的资质。 */
    val missingWeapons: Map<String, List<String>>,
) {
    val total: Int get() = values.sum()
    val totalLevels: Int get() = stages.sumOf { it.levels }
}

data class GrowthRow(
    val stat: String,
    /** 个人成长率。 */
    val personal: Int,
    /** 各段职业成长补正（已乘角色倍率）。 */
    val perStage: List<Int>,
    /** 星玉加成。 */
    val star: Int,
) {
    fun totalOf(stage: Int): Int = personal + perStage[stage] + star
}

object EngageRules {

    val tierLevelCap: Map<String, Int> = mapOf("基础" to 20, "高级" to 20, "特殊" to 40)
    const val DEFAULT_LEVEL_CAP = 20

    private val weaponKeywords = listOf("剑", "枪", "斧", "弓", "短剑", "魔道书", "杖", "体术", "龙石", "炮弹")
    private val exclusivePattern = Regex("""^(\S+?)限定""")

    fun levelCap(tier: String): Int = tierLevelCap[tier] ?: DEFAULT_LEVEL_CAP

    /**
     * 职业上限 + 角色上限修正。修正只作用于力~魔防共 7 项，HP 与体格不受影响。
     */
    fun capsOf(cls: ClassInfo, character: Character): List<Int> =
        cls.caps.mapIndexed { i, cap ->
            if (i in 1..7) cap + character.capModifier[i - 1] else cap
        }

    /** 专属职业只对对应角色开放；通用职业对所有人开放。 */
    fun isAllowedFor(cls: ClassInfo, character: Character): Boolean {
        val match = exclusivePattern.find(cls.note) ?: return true
        val owner = match.groupValues[1]
        val shortName = character.name.replace(Regex("""[（(].*?[)）]"""), "").trim()
        return shortName == owner || character.name.contains(owner)
    }

    fun classesFor(character: Character, dataset: Dataset): List<ClassInfo> =
        dataset.classes.filter { isAllowedFor(it, character) }

    /**
     * 缺少的资质：职业的每一行武器需求各自筛选一遍，取缺得最少的那一行。
     * 例：「剑A、短剑B\n枪A、短剑B」只要满足其中一行即可。
     */
    fun missingWeapons(cls: ClassInfo, character: Character): List<String> {
        val owned = character.weapons
        return cls.weapons.lineSequence()
            .map { line ->
                val need = weaponKeywords.filter { line.contains(it) }
                need.filterNot { owned.contains(it) }
            }
            .minByOrNull { it.size }
            ?: emptyList()
    }

    fun defaultRoute(character: Character, dataset: Dataset): List<Stage> {
        val initial = dataset.classOf(character.initialClass) ?: return emptyList()
        if (initial.tier == "特殊") {
            return listOf(Stage(initial.name, character.joinLevel, levelCap(initial.tier)))
        }
        val stages = mutableListOf(
            Stage(initial.name, character.joinLevel, maxOf(character.joinLevel, 10)),
        )
        dataset.classOf(character.recommendedClass)?.let { target ->
            stages += Stage(target.name, 1, levelCap(target.tier))
        }
        return stages
    }

    /** 换职业后按职业等级上限重置练级区间。 */
    fun autoLevelFor(stageIndex: Int, stage: Stage, character: Character, dataset: Dataset): Stage {
        val cls = dataset.classOf(stage.className) ?: return stage
        return if (stageIndex == 0) {
            val end = if (cls.tier == "基础") {
                maxOf(character.joinLevel, 10)
            } else {
                levelCap(cls.tier)
            }
            stage.copy(from = character.joinLevel, to = minOf(levelCap(cls.tier), end))
        } else {
            stage.copy(from = 1, to = levelCap(cls.tier))
        }
    }

    fun compute(character: Character, stages: List<Stage>, starGem: Boolean, dataset: Dataset): RouteResult {
        val stats = dataset.stats.size
        val values = character.base.toMutableList()
        val acc = IntArray(stats)
        val waste = linkedMapOf<String, Int>()
        val errors = mutableListOf<String>()
        val results = mutableListOf<StageResult>()
        val missing = linkedMapOf<String, List<String>>()
        val starBonus = if (starGem) dataset.starGrowth else 0
        val multiplier = character.classGrowthMultiplier.coerceAtLeast(1)
        var lastCaps = List(stats) { 0 }

        stages.forEachIndexed { index, stage ->
            val cls = dataset.classOf(stage.className)
            if (cls == null) {
                errors += "第 ${index + 1} 段：未选择职业"
                return@forEachIndexed
            }
            val caps = capsOf(cls, character)
            lastCaps = caps
            if (stage.to < stage.from) {
                errors += "第 ${index + 1} 段：结束等级小于起始等级"
            }
            if (stage.to > levelCap(cls.tier)) {
                errors += "第 ${index + 1} 段：${cls.name} 等级上限为 Lv.${levelCap(cls.tier)}"
            }

            missingWeapons(cls, character).takeIf { it.isNotEmpty() }?.let { missing[cls.name] = it }

            for (i in 0 until stats) values[i] = maxOf(values[i], cls.base[i])
            val before = values.toList()

            for (level in stage.from until stage.to) {
                for (i in 0 until stats) {
                    acc[i] += character.growth[i] + cls.growth[i] * multiplier + starBonus
                    if (acc[i] >= 100) {
                        val inc = acc[i] / 100
                        acc[i] %= 100
                        val next = values[i] + inc
                        if (next > caps[i]) {
                            val name = dataset.stats[i]
                            waste[name] = (waste[name] ?: 0) + (next - caps[i])
                            values[i] = caps[i]
                        } else {
                            values[i] = next
                        }
                    }
                }
            }
            results += StageResult(
                className = cls.name,
                tier = cls.tier,
                from = stage.from,
                to = stage.to,
                values = values.toList(),
                before = before,
                caps = caps,
            )
        }

        return RouteResult(
            values = values.toList(),
            caps = lastCaps,
            stages = results,
            waste = waste,
            errors = errors,
            missingWeapons = missing,
        )
    }

    /** 成长率构成表：个人成长率 + 每段职业补正（×倍率） + 星玉。 */
    fun growthRows(character: Character, stages: List<Stage>, starGem: Boolean, dataset: Dataset): List<GrowthRow> {
        val classes = stages.mapNotNull { dataset.classOf(it.className) }
        val star = if (starGem) dataset.starGrowth else 0
        val multiplier = character.classGrowthMultiplier.coerceAtLeast(1)
        return dataset.stats.mapIndexed { i, stat ->
            GrowthRow(
                stat = stat,
                personal = character.growth[i],
                perStage = classes.map { it.growth[i] * multiplier },
                star = star,
            )
        }
    }
}