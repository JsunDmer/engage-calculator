package com.engage.app.ui.classes

/** 武器筛选 chips 的固定顺序，与游戏内资质排列一致。 */
val WeaponTypes = listOf("剑", "枪", "斧", "弓", "短剑", "体术", "魔道书", "杖", "龙石", "炮弹")

/**
 * 解析职业武器字段为「武器类型 → 资质串」。
 * 数据形如「剑A、斧C（无法剑S）」：按 、/／// 与换行切分，括号备注不参与匹配，
 * 武器名按词边界精确比对，避免「剑」误中「短剑」。
 */
fun weaponGrades(weapons: String): Map<String, String> {
    val out = linkedMapOf<String, String>()
    weapons.split('、', '/', '／', '\n').forEach { part ->
        val item = part.substringBefore('（').substringBefore('(').trim()
        if (item.isEmpty()) return@forEach
        // 等级是结尾的 S/A/B/C 字母，其余前缀即武器名
        val type = item.trimEnd('S', 'A', 'B', 'C').trim()
        if (type.isNotEmpty()) out.putIfAbsent(type, item)
    }
    return out
}

fun hasWeapon(weapons: String, type: String): Boolean = type in weaponGrades(weapons)