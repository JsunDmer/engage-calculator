package com.engage.app.ui.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class CellRole { Normal, Muted, Emphasis, Up, Hit }

data class TableCell(val value: String, val role: CellRole = CellRole.Normal)

data class TableRowData(val label: String, val cells: List<TableCell>)

private val CellWidth = 62.dp
private val RowHeight = 30.dp
private val FirstColumnWidth = 62.dp

/**
 * 横向滚动的数据表。首列锁定在左侧，其余列共享同一个滚动状态，
 * 表头与每行数据因此始终对齐。
 */
@Composable
internal fun DataTable(
    firstHeader: String,
    columnHeaders: List<String>,
    columnSubHeaders: List<String>,
    rows: List<List<TableCell>>,
    firstColumn: List<String>,
    footer: TableRowData? = null,
) {
    val scroll = rememberScrollState()
    val headBg = MaterialTheme.colorScheme.surfaceVariant
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp)),
    ) {
        Row {
            HeaderCell(firstHeader, FirstColumnWidth, headBg, align = TextAlign.Start)
            Row(Modifier.horizontalScroll(scroll)) {
                columnHeaders.forEachIndexed { i, header ->
                    Column(
                        Modifier
                            .width(CellWidth)
                            .background(headBg)
                            .padding(vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            header,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 2.dp),
                        )
                        val sub = columnSubHeaders.getOrNull(i).orEmpty()
                        if (sub.isNotEmpty()) {
                            Text(
                                sub,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 2.dp),
                            )
                        }
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)

        rows.forEachIndexed { index, cells ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                RowLabel(firstColumn.getOrElse(index) { "" })
                Row(Modifier.horizontalScroll(scroll)) {
                    cells.forEach { cell -> ValueCell(cell) }
                }
            }
            if (index != rows.lastIndex) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
            }
        }

        if (footer != null) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RowLabel(footer.label)
                Row(Modifier.horizontalScroll(scroll)) {
                    footer.cells.forEach { cell -> ValueCell(cell, bold = true) }
                }
            }
        }
    }
}

@Composable
private fun RowLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .width(FirstColumnWidth)
            .padding(start = 8.dp),
    )
}

@Composable
private fun HeaderCell(text: String, width: Dp, bg: Color, align: TextAlign) {
    Box(
        Modifier
            .width(width)
            .height(38.dp)
            .background(bg),
        contentAlignment = if (align == TextAlign.Start) Alignment.CenterStart else Alignment.Center,
    ) {
        Text(
            text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = if (align == TextAlign.Start) 8.dp else 0.dp),
        )
    }
}

@Composable
private fun ValueCell(cell: TableCell, bold: Boolean = false) {
    val color = when (cell.role) {
        CellRole.Muted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
        CellRole.Emphasis -> MaterialTheme.colorScheme.primary
        CellRole.Up -> MaterialTheme.colorScheme.tertiary
        CellRole.Hit -> MaterialTheme.colorScheme.error
        CellRole.Normal -> MaterialTheme.colorScheme.onSurface
    }
    Box(
        Modifier
            .width(CellWidth)
            .height(RowHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            cell.value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (bold || cell.role == CellRole.Emphasis || cell.role == CellRole.Hit) {
                FontWeight.Bold
            } else {
                FontWeight.Normal
            },
            color = color,
            maxLines = 1,
        )
    }
}