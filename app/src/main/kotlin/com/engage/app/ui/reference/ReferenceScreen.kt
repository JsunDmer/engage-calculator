package com.engage.app.ui.reference

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.engage.app.ui.common.InfoBanner
import com.engage.app.ui.common.SectionCard
import com.engage.app.ui.theme.Gold
import com.engage.core.BattleType
import com.engage.core.Dataset

@Composable
fun ReferenceScreen(dataset: Dataset) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            SectionCard(title = "结算规则") {
                RuleList(
                    listOf(
                        "每升 1 级，各项按「个人成长率 + 当前职业成长补正 + 星玉」累加，满 100 结算 +1 并结转余数。",
                        "贾恩的固有技能「努力才能」令职业成长补正翻倍，计算时按 ×2 计入。",
                        "转职时若能力值低于新职业基础值，则抬升到职业基础值（不会降低）。",
                        "任何时刻不超过「当前职业上限 + 角色上限修正」，修正只作用于力~魔防，HP 与体格不受影响。",
                        "若某属性在中间职业就顶格，超出部分按游戏规则作废，切换更高上限的职业后不会补回。",
                        "红色 = 已达该职业上限；绿色 = 本段有提升。",
                    )
                )
            }
        }

        item {
            SectionCard(title = "等级上限") {
                RuleList(
                    listOf(
                        "基础职业：Lv.20，练到 Lv.10 即可使用转职道具转为高级职业。",
                        "高级职业：Lv.20，转职后等级重置为 Lv.1。",
                        "特殊职业：Lv.40，仅能在本职业内继续升级，无法再转职。",
                        "转职消耗：大师证明 / 上级转职道具，部分职业需额外资质。",
                    )
                )
            }
        }

        item {
            SectionCard(title = "专属职业限制") {
                RuleList(
                    listOf(
                        "专属职业仅对应角色可转：如「神龙之王」限琉尔、「邪龙之王」限邪龙玛琉斯。",
                        "本计算器的职业下拉框已按当前角色过滤，只显示可转职业。",
                        "通用职业对全角色开放，但需满足武器资质要求才能发挥全部威力。",
                    )
                )
            }
        }

        item {
            SectionCard(title = "数据源") {
                Text(
                    "数据来自《火焰纹章 Engage》中文资料表（工具人小 / 江雪之舞），" +
                        "涵盖 ${dataset.characters.size} 名角色、${dataset.classes.size} 个职业、" +
                        "${dataset.emblems.size} 位纹章士、${dataset.skills.size} 条纹章士技能、" +
                        "${dataset.chains.size} 条技能进化链。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        item {
            Text(
                "兵种类型",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp),
            )
        }

        items(dataset.types.size) { index ->
            TypeCard(dataset.types[index])
        }

        item {
            InfoBanner(
                text = "本工具为非官方资料整理，实际数值以游戏内为准。" +
                    "计算器结果为固定成长模式的期望结算，不包含随机成长。",
            )
        }
        item { Spacer(Modifier.height(4.dp)) }
    }
}

@Composable
private fun RuleList(items: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { text ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    Modifier
                        .padding(top = 6.dp)
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Gold),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text,
                    style = MaterialTheme.typography.bodySmall,
                    lineHeight = 19.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TypeCard(type: BattleType) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            type.name,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = Gold,
            modifier = Modifier.width(64.dp),
        )
        Text(
            type.detail,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
    }
}