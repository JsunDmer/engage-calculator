package com.engage.app.ui.classes

import com.engage.core.DatasetLoader
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** 武器字段解析与筛选匹配：词边界、括号备注、全数据集覆盖。 */
class ClassWeaponsTest {

    @Test
    fun `parses multiple weapons and ignores parenthesized note`() {
        assertEquals(
            mapOf("剑" to "剑A", "斧" to "斧C"),
            weaponGrades("剑A、斧C（无法剑S）"),
        )
    }

    @Test
    fun `sword filter does not match knife`() {
        assertFalse(hasWeapon("短剑B、体术B", "剑"))
        assertTrue(hasWeapon("短剑B、体术B", "短剑"))
    }

    @Test
    fun `cannon with no-qualification note still matches`() {
        assertTrue(hasWeapon("炮弹S（无需资质）", "炮弹"))
    }

    @Test
    fun `compound tome staff art matches staff`() {
        assertTrue(hasWeapon("魔道书B、杖S、体术C", "杖"))
    }

    @Test
    fun `every dataset weapon type is covered by filter list`() {
        val dataset = DatasetLoader.parse(
            File("src/main/assets/engage.json").readText()
        )
        dataset.classes.forEach { cls ->
            val unknown = weaponGrades(cls.weapons).keys - WeaponTypes.toSet()
            assertTrue("${cls.name} has unknown weapons $unknown", unknown.isEmpty())
        }
    }
}