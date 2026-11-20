package com.engage.core

import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

/** 技能图标映射：加载、归一查找与旧数据兼容。 */
class DatasetIconsTest {

    private val dataset: Dataset = DatasetLoader.parse(
        File("../app/src/main/assets/engage.json").readText()
    )

    @Test
    fun `skillIcons loaded from assets`() {
        assertTrue(dataset.skillIcons.isNotEmpty())
        val path = dataset.skillIcon("SP转换")
        assertTrue(path.startsWith("skills/"), "expected skills/ path, got '$path'")
    }

    @Test
    fun `normalized variants hit same icon`() {
        val dotted = dataset.skillIcon("剑术对策Lv.5")
        assertTrue(dotted.isNotEmpty())
        assertEquals(dotted, dataset.skillIcon("剑术对策Lv5"))
    }

    @Test
    fun `unknown skill returns empty path`() {
        assertEquals("", dataset.skillIcon("不存在的技能"))
    }

    @Test
    fun `legacy json without skillIcons still parses`() {
        val legacy = DatasetLoader.parse(
            """{"stats":[],"starGrowth":15,"chars":[],"classes":[]}"""
        )
        assertTrue(legacy.skillIcons.isEmpty())
    }
}