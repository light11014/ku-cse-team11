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

enum class Tier(val label: String) { S("S"), F("F"), UNKNOWN("Unknown") }

@Composable
fun TierSelector(
    contentId: Int,
    modifier: Modifier = Modifier,
    initial: Tier = Tier.UNKNOWN,
    onChanged: (Tier) -> Unit = {}     // 나중에 API 저장 연결용
) {
    // 재조립/회전까지 유지(앱 재시작시엔 초기화됨)
    var tierName by rememberSaveable(contentId) { mutableStateOf(initial.name) }
    val tier = remember(tierName) { Tier.valueOf(tierName) }

    Column(modifier = modifier) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tier.values().forEach { t ->
                FilterChip(
                    selected = (t == tier),
                    onClick = {
                        tierName = t.name
                        onChanged(t)  // 훗날 서버 저장 호출만 연결
                    },
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
        Spacer(Modifier.height(10.dp))
        AssistChip(
            onClick = {},
            label = { Text("현재 선택: ${tier.label}") },
            leadingIcon = {
                Box(
                    Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(colorForTier(tier))
                )
            }
        )
    }
}

@Composable
private fun colorForTier(t: Tier): Color = when (t) {
    Tier.S -> Color(0xFF2ECC71)      // 그린
    Tier.F -> Color(0xFFE74C3C)      // 레드
    Tier.UNKNOWN -> Color(0xFF95A5A6) // 그레이
}
