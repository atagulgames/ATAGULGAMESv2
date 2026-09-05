package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.LevelEntity

@Composable
fun LevelSelectDialog(
    levels: List<LevelEntity>,
    currentLevelIndex: Int,
    completedLevels: Set<Int>,
    onSelectLevel: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTier by remember { mutableIntStateOf((currentLevelIndex / 25).coerceIn(0, 9)) }
    val tierRanges = listOf(
        "1-25 Başlangıç",
        "26-50 Kilit & Anahtar",
        "51-75 Zaman & Hız",
        "76-100 Usta Ağı",
        "101-125 Çift Yörünge",
        "126-150 İkiz Geçit",
        "151-175 Takımyıldız",
        "176-200 Kristal Kafes",
        "201-225 Kuantum Matris",
        "226-250 ECHO OMEGA"
    )

    val currentTierLevels = remember(levels, selectedTier) {
        val startId = selectedTier * 25 + 1
        val endId = (selectedTier + 1) * 25
        levels.filter { it.id in startId..endId }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("level_select_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridOn,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(19.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "250 Seviye",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Tamamlanan: ${completedLevels.size}/250",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_level_select_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tier tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTier,
                    containerColor = Color(0xFFF1F5F9),
                    contentColor = Color(0xFF0284C7),
                    edgePadding = 8.dp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    tierRanges.forEachIndexed { idx, label ->
                        Tab(
                            selected = selectedTier == idx,
                            onClick = { selectedTier = idx },
                            text = {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTier == idx) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTier == idx) Color(0xFF0284C7) else Color(0xFF64748B)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Grid of 25 levels for selected tier
                LazyVerticalGrid(
                    columns = GridCells.Fixed(5),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    items(currentTierLevels) { levelEntity ->
                        val index = levelEntity.id - 1
                        val isCompleted = levelEntity.isCompleted
                        val isCurrent = index == currentLevelIndex
                        val isUnlocked = levelEntity.isUnlocked || index <= currentLevelIndex || isCompleted

                        val bgColor = when {
                            isCurrent -> Color(0xFF0284C7)
                            isCompleted -> Color(0xFFF0FDF4)
                            isUnlocked -> Color.White
                            else -> Color(0xFFF8FAFC)
                        }

                        val contentColor = when {
                            isCurrent -> Color.White
                            isCompleted -> Color(0xFF15803D)
                            isUnlocked -> Color(0xFF0F172A)
                            else -> Color(0xFF94A3B8)
                        }

                        val borderColor = when {
                            isCurrent -> Color(0xFF0284C7)
                            isCompleted -> Color(0xFFBBF7D0)
                            isUnlocked -> Color(0xFFCBD5E1)
                            else -> Color(0xFFE2E8F0)
                        }

                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable(enabled = isUnlocked) {
                                    onSelectLevel(index)
                                }
                                .testTag("level_item_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isUnlocked) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Kilitli",
                                    tint = contentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${levelEntity.id}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = contentColor
                                    )
                                    if (isCompleted) {
                                        Row(
                                            modifier = Modifier.padding(top = 1.dp),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            repeat(levelEntity.stars.coerceIn(1, 3)) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFF59E0B),
                                                    modifier = Modifier.size(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
