package com.engage.app.ui.emblems

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.engage.app.ui.common.AssetAvatar
import com.engage.app.ui.common.InfoBanner
import com.engage.app.ui.common.SectionCard
import com.engage.app.ui.theme.Gold
import com.engage.core.Dataset
import com.engage.core.Emblem

/**
 * 资质检索：以「资质 → 提供者」为主线，比按纹章士翻更符合查表场景。
 * 牵绊等级为解锁门槛，不消耗 SP。
 */
@Composable
internal fun QualificationsScreen(
    dataset: Dataset,
    onOpenEmblem: (Emblem) -> Unit,
) {
    var filter by remember { mutableStateOf("全部") }
    val groups = remember(dataset) { qualificationGroups(dataset) }
    val visible = if (filter == "全部") groups else groups.filter { shortQualification(it.name) == filter }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            (listOf("全部") + groups.map { shortQualification(it.name) }).forEach { label ->
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
                    "共 ${groups.size} 种资质　${groups.sumOf { it.holders.size }} 条提供记录",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            items(visible, key = { it.name }) { group ->
                SectionCard(
                    title = group.name,
                    subtitle = "${group.holders.size} 位纹章士提供　按牵绊等级排序",
                ) {
                    group.holders.forEachIndexed { index, holder ->
                        HolderRow(holder, onClick = { onOpenEmblem(holder.emblem) })
                        if (index != group.holders.lastIndex) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant,
                                modifier = Modifier.padding(vertical = 6.dp),
                            )
                        }
                    }
                }
            }
            item {
                InfoBanner(text = "资质由牵绊等级解锁，无需消耗 SP；转职到对应兵种时仍受该兵种的武器熟练度要求限制。")
            }
        }
    }
}

@Composable
private fun HolderRow(holder: QualificationHolder, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(7.dp))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AssetAvatar(holder.emblem.avatar, 34.dp, fallback = holder.emblem.name.take(1))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    holder.emblem.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    holder.level,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Gold)
                        .padding(horizontal = 5.dp, vertical = 1.dp),
                )
            }
            if (holder.note.isNotBlank()) {
                Text(
                    holder.note,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(
            holder.emblem.section,
            style = MaterialTheme.typography.labelSmall,
            color = if (holder.emblem.section == "DLC") Gold else MaterialTheme.colorScheme.primary,
        )
    }
}