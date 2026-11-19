package com.engage.app.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

/** 成长率高低档位，各页面共用同一套阈值与配色。 */
enum class GrowthTier { High, Mid, Low }

/**
 * 成长率分档阈值。取值依据数据分布的中位数 / 四分位：
 * 单属性每级总成长率（个人 + 职业补正 + 星玉）中位数约 55，
 * 职业单属性成长率只有 0/5/10/15/20/25/30 几档，
 * 职业成长合计中位数 85，角色个人成长合计中位数 285。
 */
object GrowthScale {
    const val COMBINED_HIGH = 60
    const val COMBINED_LOW = 40

    const val CLASS_STAT_HIGH = 20
    const val CLASS_STAT_LOW = 10

    const val CLASS_TOTAL_HIGH = 90
    const val CLASS_TOTAL_LOW = 80

    const val CHARACTER_TOTAL_HIGH = 300
    const val CHARACTER_TOTAL_LOW = 265

    fun combined(value: Int): GrowthTier = tier(value, COMBINED_HIGH, COMBINED_LOW)

    fun classStat(value: Int): GrowthTier = tier(value, CLASS_STAT_HIGH, CLASS_STAT_LOW)

    fun classTotal(value: Int): GrowthTier = tier(value, CLASS_TOTAL_HIGH, CLASS_TOTAL_LOW)

    fun characterTotal(value: Int): GrowthTier = tier(value, CHARACTER_TOTAL_HIGH, CHARACTER_TOTAL_LOW)

    private fun tier(value: Int, high: Int, low: Int): GrowthTier = when {
        value >= high -> GrowthTier.High
        value < low -> GrowthTier.Low
        else -> GrowthTier.Mid
    }
}

/** 高档用墨绿、中档用暖褐、低档用次要文字色，深浅主题下都能保持对比度。 */
@Composable
fun growthColor(tier: GrowthTier): Color = when (tier) {
    GrowthTier.High -> MaterialTheme.colorScheme.tertiary
    GrowthTier.Mid -> MaterialTheme.colorScheme.onSecondaryContainer
    GrowthTier.Low -> MaterialTheme.colorScheme.onSurfaceVariant
}

/** 职业成长率数组用「/」连接，逐值着色，0 保持次要色以免抢视线。 */
@Composable
fun growthSlashText(values: List<Int>, scale: (Int) -> GrowthTier): AnnotatedString {
    val separator = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    return buildAnnotatedString {
        values.forEachIndexed { index, value ->
            if (index > 0) withStyle(SpanStyle(color = separator)) { append("/") }
            val color = if (value == 0) {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f)
            } else {
                growthColor(scale(value))
            }
            withStyle(SpanStyle(color = color)) { append(value.toString()) }
        }
    }
}