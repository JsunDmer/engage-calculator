package com.engage.core

import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/** 结合武器面板：71 条技能行全部能查到数值，关键字段抽查。 */
class WeaponStatsTest {

    private val dataset: Dataset = DatasetLoader.parse(
        File("../app/src/main/assets/engage.json").readText()
    )

    @Test
    fun `every engage weapon skill has stats`() {
        val missing = dataset.skills
            .filter { it.kind == "结合武器" }
            .filter { dataset.weaponOf(it.emblem, it.name) == null }
        assertTrue(missing.isEmpty(), "missing stats: ${missing.map { "${it.emblem}/${it.name}" }}")
    }

    @Test
    fun `spot check sigurd brave lance`() {
        val w = dataset.weaponOf("西格尔特", "勇者枪")!!
        assertEquals(7, w.might)
        assertEquals(60, w.hit)
        assertEquals(12, w.weight)
        assertEquals("1", w.range)
    }

    @Test
    fun `同名武器按纹章士区分`() {
        // 马尔斯版与艾莉可版刺剑数值不同（重量 3 vs 5）
        assertEquals(3, dataset.weaponOf("马尔斯", "刺剑")!!.weight)
        assertEquals(5, dataset.weaponOf("艾莉可", "刺剑")!!.weight)
        // 法尔西昂：马尔斯版命中 100，库洛武版命中 80
        assertEquals(100, dataset.weaponOf("马尔斯", "法尔西昂")!!.hit)
        assertEquals(80, dataset.weaponOf("库洛武 / 鲁弗莱", "神剑法尔西昂")!!.hit)
    }

    @Test
    fun `accessory weapons have null combat stats`() {
        val shield = dataset.weaponOf("贝雷特", "埃癸斯之盾")!!
        assertEquals(null, shield.might)
        assertTrue(shield.effect.contains("防守+6"))
    }

    @Test
    fun `legacy json without emblemWeapons still parses`() {
        val legacy = DatasetLoader.parse(
            """{"stats":[],"starGrowth":15,"chars":[],"classes":[]}"""
        )
        assertTrue(legacy.emblemWeapons.isEmpty())
        assertEquals(null, legacy.weaponOf("马尔斯", "刺剑"))
    }
}