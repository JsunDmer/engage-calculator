package com.engage.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** 一次升级的成长结算结果。 */
@Serializable
data class Character(
    @SerialName("n") val name: String,
    /** 加入时的初始职业名。 */
    @SerialName("c") val initialClass: String,
    @SerialName("lv") val joinLevel: Int,
    /** 个人成长率，顺序同 [Dataset.stats]。 */
    @SerialName("g") val growth: List<Int>,
    /** 初始能力值，顺序同 [Dataset.stats]。 */
    @SerialName("b") val base: List<Int>,
    /** 天生武器资质，如「剑、体术」。 */
    @SerialName("f") val weapons: String,
    /** 上限修正，长度为 7，对应力/魔/技/速/幸/防/魔防。 */
    @SerialName("m") val capModifier: List<Int>,
    /** 职业成长率倍率：贾恩「努力才能」为 2，其余为 1。 */
    @SerialName("x") val classGrowthMultiplier: Int = 1,
    /** 推荐转职。 */
    @SerialName("rec") val recommendedClass: String,
    /** assets 内的头像相对路径，空串表示无头像。 */
    @SerialName("avatar") val avatar: String = "",
)

@Serializable
data class ClassInfo(
    /** 专属职业 / 通用职业。 */
    @SerialName("sec") val section: String,
    @SerialName("n") val name: String,
    /** 基础 / 高级 / 特殊。 */
    @SerialName("lv") val tier: String,
    @SerialName("mv") val move: Int,
    /** 武器熟练度与必要资质，可含多行。 */
    @SerialName("w") val weapons: String,
    @SerialName("skill") val skill: String = "",
    @SerialName("sd") val skillDetail: String = "",
    @SerialName("b") val base: List<Int>,
    @SerialName("g") val growth: List<Int>,
    @SerialName("gt") val growthTotal: Int = 0,
    @SerialName("cap") val caps: List<Int>,
    @SerialName("ct") val capTotal: Int = 0,
    @SerialName("type") val battleType: String = "",
    @SerialName("note") val note: String = "",
)

@Serializable
data class Emblem(
    @SerialName("n") val name: String,
    @SerialName("sec") val section: String,
    @SerialName("ch") val chapter: String,
    @SerialName("join") val joinCondition: String,
    @SerialName("bonus") val maxBondBonus: String,
    @SerialName("sync") val firstSyncSkill: String,
    @SerialName("inherit") val firstInheritSkill: String,
    @SerialName("engage") val firstEngageSkill: String,
    @SerialName("combo") val firstComboSkill: String,
    @SerialName("weapon") val firstEngageWeapon: String,
    @SerialName("unlock") val unlocks: String,
    /** assets 内的立绘相对路径，空串表示无立绘。 */
    @SerialName("avatar") val avatar: String = "",
)

@Serializable
data class EmblemSkill(
    @SerialName("e") val emblem: String,
    @SerialName("lv") val bondLevel: String,
    @SerialName("kind") val kind: String,
    @SerialName("n") val name: String,
    @SerialName("d") val detail: String,
    @SerialName("sp") val sp: String = "",
    @SerialName("note") val note: String = "",
)

@Serializable
data class SkillChain(
    @SerialName("e") val emblem: String,
    /** 同步 / 继承。 */
    @SerialName("kind") val kind: String,
    @SerialName("fam") val family: String,
    /** 各阶描述，形如「识破\nLv.1 · 250SP」。 */
    @SerialName("steps") val steps: List<String>,
    @SerialName("final") val finalEffect: String = "",
    @SerialName("note") val note: String = "",
    @SerialName("sp") val totalSp: String = "",
)

@Serializable
data class BattleType(
    @SerialName("n") val name: String,
    @SerialName("d") val detail: String,
)

/** 牵绊戒指的一个强化等级，bonus 为累计值而非增量。 */
@Serializable
data class RingTier(
    /** C / B / A / S。 */
    @SerialName("r") val rank: String,
    @SerialName("b") val bonuses: List<String> = emptyList(),
    /** S 级戒指特技名，低等级为空。 */
    @SerialName("k") val passive: String = "",
    @SerialName("d") val passiveDetail: String = "",
)

@Serializable
data class BondRing(
    /** 所属纹章士，同一纹章士的戒指共用卡池与外观风格。 */
    @SerialName("e") val emblem: String,
    @SerialName("n") val name: String,
    /** 卡池序号 1-10，「火焰纹章：英雄」系列为 0。 */
    @SerialName("p") val pool: Int = 0,
    @SerialName("avatar") val avatar: String = "",
    @SerialName("t") val tiers: List<RingTier> = emptyList(),
)

@Serializable
data class Dataset(
    @SerialName("stats") val stats: List<String>,
    @SerialName("starGrowth") val starGrowth: Int,
    @SerialName("chars") val characters: List<Character>,
    @SerialName("classes") val classes: List<ClassInfo>,
    @SerialName("emblems") val emblems: List<Emblem> = emptyList(),
    @SerialName("skills") val skills: List<EmblemSkill> = emptyList(),
    @SerialName("chains") val chains: List<SkillChain> = emptyList(),
    @SerialName("types") val types: List<BattleType> = emptyList(),
    @SerialName("rings") val rings: List<BondRing> = emptyList(),
) {
    val classByName: Map<String, ClassInfo> by lazy { classes.associateBy { it.name } }

    fun classOf(name: String): ClassInfo? = classByName[name]
}