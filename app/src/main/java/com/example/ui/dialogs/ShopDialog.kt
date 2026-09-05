package com.example.ui.dialogs

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

@Composable
fun ShopDialog(
    currentTokens: Int,
    currentBreakers: Int,
    onPurchaseTokens: (Int) -> Unit,
    onPurchaseBreakers: (Int) -> Unit,
    onWatchRewardedAd: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(16.dp, RoundedCornerShape(24.dp))
                .testTag("shop_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
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
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFEF3C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingBag,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ECHO Mağazası",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Jeton: $currentTokens | Matkap: $currentBreakers",
                                fontSize = 11.sp,
                                color = Color(0xFFD97706),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_shop_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Item 0: Ücretsiz Ödüllü Video
                ShopItemCard(
                    title = "Ödüllü Video",
                    description = "Kısa bir video izleyerek anında +2 Jeton kazan.",
                    price = "ÜCRETSİZ",
                    isOwned = false,
                    icon = Icons.Default.PlayArrow,
                    accentColor = Color(0xFF0284C7),
                    onBuy = onWatchRewardedAd,
                    tag = "watch_reward_ad_item"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item 1: Matkap Lazer Paketi (Echo Breakers)
                ShopItemCard(
                    title = "5x Matkap Lazeri",
                    description = "Yolu tıkayan kırmızı yankı bariyerini delen lazer.",
                    price = "₺24.99 (Simüle)",
                    isOwned = false,
                    icon = Icons.Default.Bolt,
                    accentColor = Color(0xFFEA580C),
                    onBuy = { onPurchaseBreakers(5) },
                    tag = "buy_breakers_item"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item 3: 5x İpucu Jetonu
                ShopItemCard(
                    title = "5x İpucu Jetonu",
                    description = "Karmaşık düğüm ağlarında en doğru rotayı gösterir.",
                    price = "₺19.99 (Simüle)",
                    isOwned = false,
                    icon = Icons.Default.Diamond,
                    accentColor = Color(0xFFD97706),
                    onBuy = { onPurchaseTokens(5) },
                    tag = "buy_5_tokens_item"
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Item 4: 15x Mega İpucu Paketi
                ShopItemCard(
                    title = "15x Mega Jeton Paketi",
                    description = "Büyük avantajlı tasarruf paketi.",
                    price = "₺39.99 (Simüle)",
                    isOwned = false,
                    icon = Icons.Default.Diamond,
                    accentColor = Color(0xFF059669),
                    onBuy = { onPurchaseTokens(15) },
                    tag = "buy_15_tokens_item"
                )
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    title: String,
    description: String,
    price: String,
    isOwned: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    onBuy: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, if (isOwned) Color(0xFF86EFAC) else Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(12.dp)
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = description,
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onBuy,
                enabled = !isOwned,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    contentColor = Color.White,
                    disabledContainerColor = Color(0xFFDCFCE7),
                    disabledContentColor = Color(0xFF15803D)
                )
            ) {
                if (isOwned) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = price,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
