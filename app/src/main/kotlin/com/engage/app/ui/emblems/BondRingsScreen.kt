package com.engage.app.ui.emblems

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engage.app.ui.common.AssetAvatar
import com.engage.app.ui.common.InfoBanner
import com.engage.app.ui.common.SectionCard
import com.engage.app.ui.theme.Gold
import com.engage.app.ui.theme.GoldLight
import com.engage.app.ui.theme.WarmInk
import com.engage.core.BondRing
import com.engage.core.Dataset

/** S 级戒指的解锁成本，来源同数据表。 */
private const val RING_RULE_TEXT =
    "C 提供 1 级属性；B、A 各再多 1 级不同属性；S 再多 1 级与 C 同种类的属性，并解锁戒指特技。" +
        "每级 1 点，HP 每级 2.5 点向下取整。2C+100 碎片→B，3B+1000→A，4A+10000→S。"

/**
 * 牵绊戒指检索：按纹章士分组，每枚戒指把 C→B→A→S 的递进压在一行，
 * 便于横向比较同卡池戒指的属性走向。
 */
@Composable
internal fun BondRingsScreen(dataset: Dataset) {
    var filter by remember { mutableStateOf("全部") }
    val rings = dataset.rings
    val emblemNames = remember(rings) { rings.map { it.emblem }.distinct() }
    val visible = if (filter == "全部") rings else rings.filter { it.emblem == filter }
    val grouped = remember(visible) { visible.groupBy { it.emblem } }
    val passiveCount = remember(rings) { rings.count { ring -> ring.tiers.any { it.passive.isNotBlank() } } }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (listOf("全部") + emblemNames).forEach { label ->
                FilterChip(
                    selected = filter == label,
                    onClick = { filter = label },
                    label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                )
            }
        }

        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                Text(
                    "共 ${rings.size} 枚戒指　${emblemNames.size} 位纹章士　$passiveCount 枚带戒指特技",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item { InfoBanner(text = RING_RULE_TEXT) }
            grouped.forEach { (emblem, emblemRings) ->
                item(key = emblem) {
                    SectionCard(
                        title = emblem,
                        subtitle = "${emblemRings.size} 枚戒指　按卡池顺序排列",
                    ) {
                        emblemRings.forEachIndexed { index, ring ->
                            RingRow(ring)
                            if (index != emblemRings.lastIndex) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(vertical = 7.dp),
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(2.dp)) }
        }
    }
}

@Composable
internal fun RingRow(ring: BondRing) {
    val steps = remember(ring) { ringSteps(ring) }
    val last = ring.tiers.lastOrNull()

    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AssetAvatar(ring.avatar, 40.dp, fallback = ring.name.take(1))
            Spacer(Modifier.width(10.dp))
            Text(
                ring.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.weight(1f))
            if (ring.pool > 0) {
                Text(
                    "卡池 ${ring.pool}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(6.dp))
        Row(
            Modifier.horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            steps.forEachIndexed { index, step ->
                if (index > 0) {
                    Text(
                        "›",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 4.dp),
                    )
                }
                RingStepBox(step, isFinal = index == steps.lastIndex)
            }
        }

        if (last != null && last.bonuses.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                "${last.rank} 总加成　${last.bonuses.joinToString(" ")}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (last != null && last.passive.isNotBlank()) {
            Spacer(Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text(
                    last.passive,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarmInk,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(GoldLight)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    last.passiveDetail,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RingStepBox(step: RingStep, isFinal: Boolean) {
    Column(
        Modifier
            .widthIn(min = 42.dp, max = 110.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isFinal) GoldLight else MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 7.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            step.rank,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (isFinal) WarmInk else Gold,
        )
        Text(
            step.gain,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (isFinal) WarmInk else MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
        )
    }
}