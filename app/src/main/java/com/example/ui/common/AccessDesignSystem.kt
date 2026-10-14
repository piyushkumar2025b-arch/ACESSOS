package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
