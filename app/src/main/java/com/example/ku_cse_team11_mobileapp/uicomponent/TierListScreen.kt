package com.example.ku_cse_team11_mobileapp.uicomponent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.saveable.rememberSaveable

enum class Tier(val label: String) { S("S"), A("A"), B("B"), C("C"), D("D"),F("F"), UNKNOWN("Unknown") }
@Composable
fun TierSelector(
    selected: Tier,
    onSelect: (Tier) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tier.entries.forEach { t ->
                FilterChip(
                    selected = (t == selected),
                    onClick = { onSelect(t) },
                    label = { Text(t.label) },
                    leadingIcon = {
                        Box(
                            Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(colorForTier(t))
                        )
                    }
                )
            }
        }
        Spacer(Modifier.height(3.dp))
        AssistChip(
            onClick = {},
            label = { Text("현재 선택: ${selected.label}") },
            leadingIcon = {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorForTier(selected))
                )
            }
        )
    }
}


@Composable
private fun colorForTier(t: Tier): Color = when (t) {
    Tier.S -> Color(0xFF2ECC71)      // 그린
    Tier.A -> Color(0xFFF1C40F)      // 노랑
    Tier.B -> Color(0xFF3498DB)      // 블루
    Tier.C -> Color(0xFF9B59B6)      // 보라
    Tier.D -> Color(0xFFE67E22)      // 오렌지
    Tier.F -> Color(0xFFE74C3C)      // 레드
    Tier.UNKNOWN -> Color(0xFF95A5A6) // 그레이
}
