package com.quest.cbt.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.Icon
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
import com.quest.cbt.ui.theme.CardBorder
import com.quest.cbt.ui.theme.CardSurface
import com.quest.cbt.ui.theme.OnPeriwinkle
import com.quest.cbt.ui.theme.PeriwinkleAccent
import com.quest.cbt.ui.theme.TextMuted

@Composable
fun QuestBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CardSurface)
            .border(width = 1.dp, color = CardBorder)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Home Item
            val isHome = currentRoute == Screen.Home.route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.Home.route) }
                    .testTag("nav_home"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isHome) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    tint = if (isHome) PeriwinkleAccent else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Home",
                    color = if (isHome) PeriwinkleAccent else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (isHome) FontWeight.Bold else FontWeight.Normal
                )
            }

            // Center Prominent "+" Button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .offset(y = (-6).dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .shadow(elevation = 8.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(PeriwinkleAccent)
                        .clickable { onNavigate(Screen.Upload.route) }
                        .testTag("nav_upload"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Import Test",
                        tint = OnPeriwinkle,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            // Progress Item
            val isProgress = currentRoute == Screen.Progress.route
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onNavigate(Screen.Progress.route) }
                    .testTag("nav_progress"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (isProgress) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Progress",
                    tint = if (isProgress) PeriwinkleAccent else TextMuted,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Progress",
                    color = if (isProgress) PeriwinkleAccent else TextMuted,
                    fontSize = 11.sp,
                    fontWeight = if (isProgress) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
