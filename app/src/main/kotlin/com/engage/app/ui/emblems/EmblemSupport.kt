package com.engage.app.ui.emblems

import com.engage.core.BondRing
import com.engage.core.Dataset
import com.engage.core.Emblem
import com.engage.core.EmblemSkill
import com.engage.core.SkillChain
import com.engage.core.normalizeSkillName

/** 资质在游戏内的固定顺序，检索页与详情页共用。 */
internal val QualificationOrder = listOf(
    "剑术资质", "枪术资质", "斧术资质", "弓术资质", "短剑资质", "体术资质", "魔法资质", "杖术资质",
)

internal data class ChainLine(
    val family: String,
    val steps: List<Pair<String, String>>,
    val maxEffect: String,
    val note: String,
    val totalSp: Int,
)

internal data class QualificationHolder(val emblem: Emblem, val level: String, val note: String)

internal data class QualificationGroup(val name: String, val holders: List<QualificationHolder>)

/**
 * 三鼎纹章士的技能链挂在「三鼎」名下，库洛武的链同时覆盖鲁弗莱。
 * 技能表则用「艾黛尔贾特 / 帝弥托利 / 库罗德」这类合写，两边需要分别归一。
 */
internal fun emblemAliases(name: String): Set<String> = buildSet {
    add(name)
    when (name) {
        "艾黛尔贾特", "帝弥托利", "库罗德" -> add("三鼎")
        "鲁弗莱" -> add("库洛武")
    }
}

private fun ownerNames(field: String): List<String> =
    field.split('/', '／').map { it.trim() }.filter { it.isNotEmpty() }

/** Lv.16 → 16，用于按牵绊等级排序。 */
internal fun levelNumber(text: String): Int =
    Regex("""\d+""").find(text)?.value?.toIntOrNull() ?: 0

internal fun skillsOf(dataset: Dataset, emblem: Emblem, kind: String): List<EmblemSkill> =
    dataset.skills.filter { skill ->
        skill.kind == kind && (ownerNames(skill.emblem).contains(emblem.name) || skill.emblem == emblem.name)
    }.sortedBy { levelNumber(it.bondLevel) }

internal fun chainsOf(dataset: Dataset, emblem: Emblem, kind: String): List<SkillChain> {
    val aliases = emblemAliases(emblem.name)
    return dataset.chains.filter { it.emblem in aliases && it.kind == kind }
}

/** 技能链没有覆盖到的零散特技，按单阶链补上，避免详情页漏内容。 */
internal fun chainLinesOf(dataset: Dataset, emblem: Emblem, kind: String): List<ChainLine> {
    val chains = chainsOf(dataset, emblem, kind)
    val covered = chains
        .flatMap { chain -> chain.steps.map { it.substringBefore('\n') } }
        .map(::normalizeSkillName)
        .toSet()
    val lines = chains.map { chain -> chain.toLine() }
    val extras = skillsOf(dataset, emblem, "${kind}特技")
        .filter { normalizeSkillName(it.name) !in covered }
        .map { skill ->
            ChainLine(
                family = skill.name,
                steps = listOf(skill.name to stepMeta(skill.bondLevel, skill.sp)),
                maxEffect = skill.detail,
                note = skill.note,
                totalSp = skill.sp.toIntOrNull() ?: 0,
            )
        }
    return lines + extras
}

private fun SkillChain.toLine(): ChainLine {
    val parsed = steps.map { step ->
        val parts = step.split('\n')
        parts.first().trim() to parts.getOrElse(1) { "" }.trim()
    }
    // 技能链是逐级升级而非叠加，合计 SP 只看最高阶那一档。
    val sp = steps.lastOrNull()?.let { step ->
        Regex("""(\d+)\s*SP""").find(step)?.groupValues?.get(1)?.toIntOrNull()
    } ?: 0
    return ChainLine(
        family = family.removeSuffix("Lv").ifEmpty { family },
        steps = parsed,
        maxEffect = finalEffect,
        note = note,
        totalSp = sp,
    )
}

private fun stepMeta(level: String, sp: String): String =
    if (sp.isBlank() || sp == "--" || sp == "—") level else "$level · ${sp}SP"

internal fun qualificationGroups(dataset: Dataset): List<QualificationGroup> {
    val byName = dataset.emblems.associateBy { it.name }
    val holders = mutableMapOf<String, MutableList<QualificationHolder>>()
    dataset.skills.filter { it.kind == "解锁资质" }.forEach { skill ->
        ownerNames(skill.emblem).forEach { owner ->
            val emblem = byName[owner] ?: return@forEach
            holders.getOrPut(skill.name) { mutableListOf() }
                .add(QualificationHolder(emblem, skill.bondLevel, skill.note))
        }
    }
    val ordered = QualificationOrder.filter { holders.containsKey(it) } +
        holders.keys.filter { it !in QualificationOrder }.sorted()
    return ordered.map { name ->
        QualificationGroup(
            name = name,
            holders = holders.getValue(name)
                .distinctBy { it.emblem.name }
                .sortedWith(compareBy({ levelNumber(it.level) }, { it.emblem.name })),
        )
    }
}

/** 「剑术资质」→「剑术」，用于筛选标签。 */
internal fun shortQualification(name: String): String = name.removeSuffix("资质")

internal data class RingStep(val rank: String, val gain: String)

/**
 * 表中每级戒指给的是累计加成，直接铺开会让「技+1 速+1」逐级重复。
 * 这里换算成增量，一行内即可看出每级新增了什么。
 */
internal fun ringSteps(ring: BondRing): List<RingStep> {
    var previous = emptyMap<String, Int>()
    return ring.tiers.map { tier ->
        val current = tier.bonuses.associate { bonus ->
            val stat = bonus.substringBefore('+').trim()
            stat to (Regex("""\d+""").find(bonus)?.value?.toIntOrNull() ?: 0)
        }
        val gain = current
            .mapNotNull { (stat, value) ->
                val delta = value - (previous[stat] ?: 0)
                if (delta > 0) "$stat+$delta" else null
            }
        previous = current
        RingStep(tier.rank, gain.joinToString(" ").ifEmpty { "—" })
    }
}