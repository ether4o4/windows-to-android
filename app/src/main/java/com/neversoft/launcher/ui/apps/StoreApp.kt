package com.neversoft.launcher.ui.apps

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neversoft.launcher.ui.LauncherState
import com.neversoft.launcher.ui.theme.NsColor
import com.neversoft.launcher.ui.theme.NsDim

private enum class StoreCategory { Apps, Games }

private data class StoreItem(
    val name: String,
    val blurb: String,
    val packageName: String,
    val category: StoreCategory,
    val featured: Boolean = false,
)

private val storeCatalog: List<StoreItem> = listOf(
    StoreItem(
        name = "Spotify",
        blurb = "Music and podcasts for every moment.",
        packageName = "com.spotify.music",
        category = StoreCategory.Apps,
        featured = true,
    ),
    StoreItem(
        name = "WhatsApp",
        blurb = "Simple, reliable, private messaging.",
        packageName = "com.whatsapp",
        category = StoreCategory.Apps,
    ),
    StoreItem(
        name = "Telegram",
        blurb = "Fast and secure cloud-based chat.",
        packageName = "org.telegram.messenger",
        category = StoreCategory.Apps,
    ),
    StoreItem(
        name = "VLC",
        blurb = "Plays everything you throw at it.",
        packageName = "org.videolan.vlc",
        category = StoreCategory.Apps,
    ),
    StoreItem(
        name = "Chrome",
        blurb = "Fast, secure browser from Google.",
        packageName = "com.android.chrome",
        category = StoreCategory.Apps,
    ),
    StoreItem(
        name = "Netflix",
        blurb = "Watch movies and TV shows anywhere.",
        packageName = "com.netflix.mediaclient",
        category = StoreCategory.Apps,
    ),
    StoreItem(
        name = "Minecraft",
        blurb = "Build, explore and survive infinite worlds.",
        packageName = "com.mojang.minecraftpe",
        category = StoreCategory.Games,
        featured = true,
    ),
    StoreItem(
        name = "Subway Surfers",
        blurb = "Dash and dodge in this endless runner.",
        packageName = "com.kiloo.subwaysurf",
        category = StoreCategory.Games,
    ),
    StoreItem(
        name = "Candy Crush Saga",
        blurb = "Switch and match your way to victory.",
        packageName = "com.king.candycrushsaga",
        category = StoreCategory.Games,
    ),
)

@Composable
fun StoreApp() {
    val accent = LauncherState.accent
    val context = LocalContext.current

    // Tabs: index 0 = Featured, 1 = Apps, 2 = Games.
    var tab by remember { mutableStateOf(0) }

    val openPlay: (String) -> Unit = { pkg ->
        runCatching {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$pkg"),
            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }.onFailure {
            runCatching {
                val web = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$pkg"),
                ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(web)
            }
        }
    }

    val featured = remember { storeCatalog.filter { it.featured } }
    val visibleItems = when (tab) {
        1 -> storeCatalog.filter { it.category == StoreCategory.Apps }
        2 -> storeCatalog.filter { it.category == StoreCategory.Games }
        else -> storeCatalog
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NsColor.Solid),
    ) {
        StoreHeader(accent = accent)
        ChipTabs(selected = tab, onSelect = { tab = it }, accent = accent)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (tab == 0) {
                items(featured) { item ->
                    FeaturedCard(item = item, accent = accent, onGet = openPlay)
                }
                item {
                    Text(
                        text = "All apps & games",
                        color = NsColor.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
                    )
                }
            }
            items(visibleItems) { item ->
                StoreCard(item = item, accent = accent, onGet = openPlay)
            }
        }
    }
}

@Composable
private fun StoreHeader(accent: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Storefront,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(22.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = "NeverSoft Store",
                color = NsColor.Text,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Discover great apps and games",
                color = NsColor.TextTertiary,
                fontSize = 12.sp,
            )
        }
    }
}

@Composable
private fun ChipTabs(selected: Int, onSelect: (Int) -> Unit, accent: Color) {
    val labels = listOf("Featured", "Apps", "Games")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        labels.forEachIndexed { index, label ->
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .background(if (isSelected) accent else NsColor.ControlActive)
                    .clickable { onSelect(index) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = label,
                    color = if (isSelected) Color.White else NsColor.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Composable
private fun FeaturedCard(
    item: StoreItem,
    accent: Color,
    onGet: (String) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusOverlay))
            .background(
                Brush.linearGradient(
                    listOf(
                        accent.copy(alpha = 0.92f),
                        darken(accent, 0.40f),
                    ),
                ),
            )
            .padding(20.dp),
    ) {
        Column {
            Text(
                text = "FEATURED",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(NsDim.RadiusControl))
                        .background(Color.White.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = item.name.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = item.blurb,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(NsDim.RadiusControl))
                    .background(Color.White)
                    .clickable { onGet(item.packageName) }
                    .padding(horizontal = 28.dp, vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Get",
                    color = darken(accent, 0.25f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun StoreCard(
    item: StoreItem,
    accent: Color,
    onGet: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(NsDim.RadiusOverlay))
            .background(NsColor.ControlActive)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .background(accent),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = item.name.take(1).uppercase(),
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                color = NsColor.Text,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = item.blurb,
                color = NsColor.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(NsDim.RadiusControl))
                .background(accent)
                .clickable { onGet(item.packageName) }
                .padding(horizontal = 22.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Get",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun darken(c: Color, amount: Float): Color = Color(
    red = c.red * (1f - amount),
    green = c.green * (1f - amount),
    blue = c.blue * (1f - amount),
    alpha = c.alpha,
)
