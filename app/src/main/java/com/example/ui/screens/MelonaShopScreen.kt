package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Redeem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.RedeemedGiftEntity
import com.example.ui.AvailableShopGifts
import com.example.ui.DayCheckUiState
import com.example.ui.ShopGiftItem
import com.example.ui.theme.MelonaBackground
import com.example.ui.theme.MelonaSurfaceVariant
import com.example.ui.theme.VividMintCheckActive
import com.example.ui.theme.VividMintContainer
import com.example.ui.theme.VividMintPrimary

@Composable
fun MelonaShopScreen(
    uiState: DayCheckUiState,
    onExchangeGift: (ShopGiftItem) -> Boolean,
    onDismissGiftSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("전체") }
    var giftToExchange by remember { mutableStateOf<ShopGiftItem?>(null) }
    var notEnoughMelonaItem by remember { mutableStateOf<ShopGiftItem?>(null) }

    val categories = listOf("전체", "네이버페이 포인트", "기프티콘", "내 보관함")

    val filteredGifts = when (selectedCategory) {
        "네이버페이 포인트" -> AvailableShopGifts.filter { it.category == "NAVER_PAY" }
        "기프티콘" -> AvailableShopGifts.filter { it.category == "GIFTICON" }
        else -> AvailableShopGifts
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("melona_shop_screen"),
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 12.dp,
                bottom = 96.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Current Melona Balance Hero Card
            item {
                MelonaBalanceCard(
                    currentMelonas = uiState.currentMelonas,
                    totalEarned = uiState.totalEarnedMelonas,
                    redeemedCount = uiState.redeemedGifts.size
                )
            }

            // 2. Category Filter Chips
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shop_category_row")
                ) {
                    items(categories) { category ->
                        val isSelected = (selectedCategory == category)
                        val labelText = if (category == "내 보관함" && uiState.redeemedGifts.isNotEmpty()) {
                            "내 보관함 (${uiState.redeemedGifts.size})"
                        } else {
                            category
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    text = labelText,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VividMintPrimary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("shop_chip_$category")
                        )
                    }
                }
            }

            // 3. Content: Either "내 보관함" list OR Gift Shop items
            if (selectedCategory == "내 보관함") {
                if (uiState.redeemedGifts.isEmpty()) {
                    item {
                        EmptyGiftboxCard()
                    }
                } else {
                    items(uiState.redeemedGifts, key = { it.id }) { redeemed ->
                        RedeemedGiftCard(
                            gift = redeemed,
                            onCopyCode = { code ->
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("쿠폰 번호", code)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "쿠폰 번호가 복사되었습니다!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            } else {
                // Earning Guide Card
                item {
                    MelonaEarningGuideCard()
                }

                // Gift Shop Items
                items(filteredGifts, key = { it.id }) { gift ->
                    ShopGiftCard(
                        gift = gift,
                        userMelonas = uiState.currentMelonas,
                        onExchangeClick = {
                            if (uiState.currentMelonas >= gift.requiredMelona) {
                                giftToExchange = gift
                            } else {
                                notEnoughMelonaItem = gift
                            }
                        }
                    )
                }
            }
        }
    }

    // Exchange Confirmation Dialog
    giftToExchange?.let { gift ->
        AlertDialog(
            onDismissRequest = { giftToExchange = null },
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_melona_stamp),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "선물로 교환할까요?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "‘${gift.title}’",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = VividMintPrimary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "보유 메로나 ${gift.requiredMelona}개가 차감되며,\n즉시 사용 가능한 교환권 쿠폰 번호가 발급됩니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onExchangeGift(gift)
                        giftToExchange = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = VividMintPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("button_confirm_exchange")
                ) {
                    Text("교환하기", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { giftToExchange = null },
                    modifier = Modifier.testTag("button_cancel_exchange")
                ) {
                    Text("취소")
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Not Enough Melona Dialog
    notEnoughMelonaItem?.let { gift ->
        val needed = gift.requiredMelona - uiState.currentMelonas
        AlertDialog(
            onDismissRequest = { notEnoughMelonaItem = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "메로나가 부족해요!",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "이 선물은 메로나 ${gift.requiredMelona}개가 필요해요.\n(현재 ${uiState.currentMelonas}개 보유, 앞으로 ${needed}개 더 모으면 교환 가능!)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "💡 팁: 오늘 할 일을 완료할 때마다 메로나가 1개씩 적립되고, 올클리어 시 보너스 3개를 더 드려요!",
                        style = MaterialTheme.typography.bodySmall,
                        color = VividMintPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { notEnoughMelonaItem = null },
                    colors = ButtonDefaults.buttonColors(containerColor = VividMintPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("button_confirm_not_enough")
                ) {
                    Text("할 일 실천하고 모으기", fontWeight = FontWeight.Bold)
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Gift Voucher Success Dialog
    uiState.lastExchangedGift?.let { voucher ->
        AlertDialog(
            onDismissRequest = onDismissGiftSuccess,
            icon = {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = VividMintCheckActive,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "🎉 교환 완료!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = voucher.giftTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Voucher Code Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp)),
                        color = MelonaBackground,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, VividMintPrimary.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "교환권 쿠폰 번호",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = voucher.voucherCode,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                fontFamily = FontFamily.Monospace,
                                color = VividMintPrimary,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "‘내 보관함’ 탭에서 언제든 다시 확인하실 수 있습니다.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("쿠폰 번호", voucher.voucherCode)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "쿠폰 번호가 복사되었습니다!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "복사",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("번호 복사")
                    }

                    Button(
                        onClick = onDismissGiftSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = VividMintPrimary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("확인", fontWeight = FontWeight.Bold)
                    }
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun MelonaBalanceCard(
    currentMelonas: Int,
    totalEarned: Int,
    redeemedCount: Int
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("melona_balance_card"),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MelonaBackground,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "내 메로나 포인트",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_melona_stamp),
                            contentDescription = "메로나",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${currentMelonas}개",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = VividMintPrimary,
                            fontSize = 32.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = VividMintContainer.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, VividMintPrimary.copy(alpha = 0.3f))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "누적 획득",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${totalEarned}개",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = VividMintPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MelonaEarningGuideCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MelonaBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "🎁", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "메로나 모으는 방법",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "• 할 일 1개 완료할 때마다 메로나 +1개 적립\n• 하루의 모든 할 일 완료(올클리어) 시 보너스 +3개 적립",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ShopGiftCard(
    gift: ShopGiftItem,
    userMelonas: Int,
    onExchangeClick: () -> Unit
) {
    val canAfford = userMelonas >= gift.requiredMelona

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_gift_card_${gift.id}"),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (canAfford) VividMintPrimary.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (gift.category == "NAVER_PAY") Color(0xFFE8F7ED) else MelonaBackground
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gift.iconEmoji,
                    fontSize = 26.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (gift.category == "NAVER_PAY") Color(0xFF03C75A).copy(alpha = 0.12f) else VividMintContainer
                    ) {
                        Text(
                            text = gift.brand,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (gift.category == "NAVER_PAY") Color(0xFF03C75A) else VividMintPrimary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = gift.priceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = gift.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_melona_stamp),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${gift.requiredMelona}개",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = VividMintPrimary
                    )
                }
            }

            // Exchange Button
            Button(
                onClick = onExchangeClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (canAfford) VividMintPrimary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (canAfford) Color.White else MaterialTheme.colorScheme.outline
                ),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("btn_exchange_${gift.id}")
            ) {
                Text(
                    text = if (canAfford) "교환" else "부족",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun RedeemedGiftCard(
    gift: RedeemedGiftEntity,
    onCopyCode: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("redeemed_gift_card_${gift.id}"),
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, VividMintPrimary.copy(alpha = 0.3f)),
        tonalElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = gift.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = gift.brandName,
                            style = MaterialTheme.typography.labelSmall,
                            color = VividMintPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = gift.giftTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = VividMintContainer
                ) {
                    Text(
                        text = "교환 완료",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = VividMintPrimary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Voucher Code Pill
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = MelonaBackground.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "쿠폰 번호 (${gift.redeemedDate} 교환)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(
                            text = gift.voucherCode,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Monospace,
                            color = VividMintPrimary
                        )
                    }

                    OutlinedButton(
                        onClick = { onCopyCode(gift.voucherCode) },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "복사",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("복사", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyGiftboxCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🎁", fontSize = 42.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "아직 교환한 선물이 없어요",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "할 일을 완료하고 메로나를 모아서\n네이버페이 포인트와 기프티콘으로 교환해보세요!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
