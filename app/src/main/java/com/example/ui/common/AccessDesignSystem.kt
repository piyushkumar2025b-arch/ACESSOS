package com.example.ui.common

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccessBackgroundDeep
import com.example.ui.theme.AccessSurfaceBorder
import com.example.ui.theme.HeroGradientBottom
import com.example.ui.theme.HeroGradientTop
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/** AccessOS professional design tokens — single source of truth. */
object AccessDs {
    val ScreenHPadding: Dp = 20.dp
    val CardRadius: Dp = 22.dp
    val CardRadiusSmall: Dp = 16.dp
    val SectionSpacing: Dp = 18.dp
    val ItemSpacing: Dp = 14.dp
    val HeroPadding: Dp = 22.dp
    val BorderSubtle = AccessSurfaceBorder.copy(alpha = 0.55f)
    val HeroBrush = Brush.verticalGradient(listOf(HeroGradientTop, HeroGradientBottom))
    val ScrimBrush = Brush.verticalGradient(
        0f to Color.Transparent,
        1f to AccessBackgroundDeep.copy(alpha = 0.55f)
    )
}

/** Consistent section header: eyebrow label + title + optional trailing slot. */
@Composable
fun SectionHeader(
    eyebrow: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = eyebrow.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = title,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.width(12.dp))
            trailing()
        }
    }
}

/** Small caption used under cards for reassurance copy. */
@Composable
fun SupportCaption(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = TextSecondary.copy(alpha = 0.85f),
        lineHeight = 17.sp,
        modifier = modifier
    )
}

/** Premium elevated card with gradient sheen — use for all content cards. */
@Composable
fun ProCard(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AccessDs.CardRadius),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = com.example.ui.theme.AccessSurface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, AccessDs.BorderSubtle),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color.White.copy(alpha = 0.04f), Color.Transparent)
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

/**
 * Professional screen header: back button + icon tile + title/subtitle + action.
 * Replaces the old flat Surface header on every sub-screen.
 */
@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(14.dp),
            color = com.example.ui.theme.AccessSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, AccessDs.BorderSubtle),
            modifier = Modifier
                .size(48.dp)
                .clickable(onClick = onBack)
        ) {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(AccessDs.CardRadiusSmall),
            color = accent.copy(alpha = 0.15f),
            border = androidx.compose.foundation.BorderStroke(1.dp, accent.copy(alpha = 0.4f)),
            modifier = Modifier.size(52.dp)
        ) {
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                lineHeight = 27.sp
            )
            Text(
                text = subtitle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextSecondary
            )
        }
        if (action != null) action()
    }
}

/** Styled text field colors shared by all forms. */
@Composable
fun accessFieldColors() = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = com.example.ui.theme.AccessTertiary,
    unfocusedBorderColor = AccessDs.BorderSubtle,
    focusedLabelColor = com.example.ui.theme.AccessTertiary,
    unfocusedLabelColor = TextMuted,
    cursorColor = com.example.ui.theme.AccessTertiary
)

