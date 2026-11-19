package com.engage.app.ui.common

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.engage.app.ui.theme.Gold
import com.engage.app.ui.theme.HitColor
import com.engage.app.ui.theme.UpColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 卡片容器：左侧金色竖条 + 标题，与原网页版 h2 一致。 */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(width = 4.dp, height = 14.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Gold)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (subtitle != null) {
                Spacer(Modifier.height(5.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

/** 分段徽标：基础=深蓝，高级=金，特殊=紫。 */
@Composable
fun TierBadge(tier: String, modifier: Modifier = Modifier) {
    val color = when (tier) {
        "高级" -> Gold
        "特殊" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
    Text(
        text = tier,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(color)
            .padding(horizontal = 7.dp, vertical = 1.dp),
    )
}

@Composable
fun InfoBanner(text: String, modifier: Modifier = Modifier, warn: Boolean = false) {
    if (text.isBlank()) return
    val container = if (warn) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.secondaryContainer
    val content = if (warn) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSecondaryContainer
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = content,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .border(1.dp, content.copy(alpha = 0.25f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 8.dp),
    )
}

@Composable
fun KeyValue(label: String, value: String, modifier: Modifier = Modifier) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(74.dp),
        )
        Text(
            text = value.ifBlank { "—" },
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f),
        )
    }
}

/** 数值单元格着色：达到上限标红，高于初始标绿。 */
@Composable
fun StatValue(
    value: Int,
    reachedCap: Boolean,
    improved: Boolean,
    modifier: Modifier = Modifier,
    emphasized: Boolean = false,
) {
    val color = when {
        reachedCap -> if (MaterialTheme.colorScheme.error != HitColor) MaterialTheme.colorScheme.error else HitColor
        improved -> UpColor
        else -> MaterialTheme.colorScheme.onSurface
    }
    Text(
        text = value.toString(),
        style = if (emphasized) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.bodyMedium,
        fontWeight = if (emphasized || reachedCap) FontWeight.Bold else FontWeight.Medium,
        color = color,
        modifier = modifier,
    )
}

private object AvatarCache {
    private val cache = LruCache<String, Bitmap>(64)

    suspend fun load(context: Context, path: String): Bitmap? {
        if (path.isBlank()) return null
        cache.get(path)?.let { return it }
        return withContext(Dispatchers.IO) {
            runCatching {
                context.assets.open(path).use { BitmapFactory.decodeStream(it) }
            }.getOrNull()?.also { cache.put(path, it) }
        }
    }
}

@Composable
fun CharacterAvatar(path: String, size: Dp = 54.dp, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var bitmap by remember(path) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(path) {
        bitmap = AvatarCache.load(context, path)
    }
    val shape = RoundedCornerShape(if (size > 48.dp) 10.dp else 9.dp)
    Box(
        modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, Gold.copy(alpha = 0.35f), shape),
        contentAlignment = Alignment.Center,
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size).clip(shape),
            )
        }
    }
}

@Composable
fun LabeledRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        trailing()
    }
}

/** 记忆化的小工具：把多行文本拆成行，去掉空行。 */
fun multiline(text: String): List<String> =
    text.split('\n').map { it.trim() }.filter { it.isNotEmpty() }