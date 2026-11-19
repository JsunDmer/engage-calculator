package com.engage.core

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/**
 * 用原 HTML 计算器逐角色跑出的结果做基准，确保 Kotlin 引擎数值完全一致。
 */
class GoldenTest {

    private val dataset: Dataset = DatasetLoader.parse(
        File("../app/src/main/assets/engage.json").readText()
    )

    private val golden: JsonArray = Json.parseToJsonElement(
        File("src/test/resources/golden.json").readText()
    ).jsonArray

    @Test
    fun `matches html calculator for every character`() {
        var checked = 0
        for (case in golden) {
            val obj = case.jsonObject
            val name = obj.str("char")
            val star = obj.str("star").toBoolean()
            val character = dataset.characters.first { it.name == name }
            val stages = obj["stages"]!!.jsonArray.map {
                val s = it.jsonObject
                Stage(s.str("c"), s.str("from").toInt(), s.str("to").toInt())
            }

            val expectedValues = obj.ints("values")
            val expectedCaps = obj.ints("caps")
            val expectedStageValues = obj["stagesResult"]!!.jsonArray.map { it.jsonObject.ints("v") }
            val expectedWaste = obj["waste"]!!.jsonObject.mapValues { it.value.jsonPrimitive.int }

            val result = EngageRules.compute(character, stages, star, dataset)

            val label = "$name (star=$star) stages=$stages"
            assertEquals(expectedValues, result.values, "最终能力值不一致：$label")
            assertEquals(expectedCaps, result.caps, "上限不一致：$label")
            assertEquals(expectedStageValues, result.stages.map { it.values }, "分段能力值不一致：$label")
            assertEquals(expectedWaste, result.waste, "溢出点数不一致：$label")
            assertTrue(result.errors.isEmpty(), "不应有错误提示：$label → ${result.errors}")
            checked++
        }
        assertEquals(82, checked, "基准用例数量")
    }

    @Test
    fun `dataset contains full encyclopedia data`() {
        assertEquals(41, dataset.characters.size)
        assertEquals(53, dataset.classes.size)
        assertEquals(23, dataset.emblems.size)
        assertEquals(505, dataset.skills.size)
        assertEquals(104, dataset.chains.size)
        assertEquals(8, dataset.types.size)
        assertEquals(9, dataset.stats.size)
        assertEquals(15, dataset.starGrowth)
    }

    @Test
    fun `exclusive classes only offered to their owner`() {
        val seadall = dataset.characters.first { it.name == "赛安达斯" }
        val allowed = EngageRules.classesFor(seadall, dataset).map { it.name }
        assertTrue(allowed.contains("舞蹈家（赛安达斯）"), "本人应能选择专属职业")
        assertTrue(allowed.none { it.contains("琉尔") }, "不应出现他人专属职业")
    }

    @Test
    fun `default route follows recommended class`() {
        val alear = dataset.characters.first { it.name == "琉尔（女）" }
        val route = EngageRules.defaultRoute(alear, dataset)
        assertEquals(2, route.size)
        assertEquals("神龙之子（主角琉尔）", route[0].className)
        assertEquals("神龙之王", route[1].className)
        assertEquals(10, route[0].to)
        assertEquals(20, route[1].to)
    }

    @Test
    fun `star gem adds fifteen growth per level`() {
        val vander = dataset.characters.first { it.name == "凡德雷" }
        val route = listOf(Stage("圣骑士", 1, 20))
        val plain = EngageRules.compute(vander, route, false, dataset)
        val starred = EngageRules.compute(vander, route, true, dataset)
        assertTrue(starred.values.sum() > plain.values.sum(), "装备星玉后总能力应更高")
    }

    private fun JsonObject.str(key: String): String = get(key)!!.jsonPrimitive.content

    private fun JsonObject.ints(key: String): List<Int> =
        get(key)!!.jsonArray.map { it.jsonPrimitive.int }
}