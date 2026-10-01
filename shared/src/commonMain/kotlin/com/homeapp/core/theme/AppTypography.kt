package com.homeapp.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import homeapp.shared.generated.resources.DMSans
import homeapp.shared.generated.resources.Poppins_Bold
import homeapp.shared.generated.resources.Poppins_Medium
import homeapp.shared.generated.resources.Poppins_Regular
import homeapp.shared.generated.resources.Poppins_SemiBold
import homeapp.shared.generated.resources.Res

// Google Fonts: 'Poppins' (headings) + 'DM Sans' (body), bundled via composeResources/font
// Font(resource, ...) is @Composable in CMP 1.11, so the families are exposed via composable getters.
val PoppinsFontFamily: FontFamily
    @Composable get() = FontFamily(
        Font(resource = Res.font.Poppins_Regular, weight = FontWeight.Normal),
        Font(resource = Res.font.Poppins_Medium, weight = FontWeight.Medium),
        Font(resource = Res.font.Poppins_SemiBold, weight = FontWeight.SemiBold),
        Font(resource = Res.font.Poppins_Bold, weight = FontWeight.Bold),
    )

val DmSansFontFamily: FontFamily
    @Composable get() = FontFamily(
        // Variable DM Sans file re-registered per weight for reliable axis mapping
        Font(resource = Res.font.DMSans, weight = FontWeight.Normal),
        Font(resource = Res.font.DMSans, weight = FontWeight.Medium),
        Font(resource = Res.font.DMSans, weight = FontWeight.Bold),
    )

/**
 * The single type ramp for the whole app.
 *
 * Every slot carries an explicit `lineHeight` and `letterSpacing`. Previously
 * these were left to the platform default, which meant the same `titleMedium`
 * rendered at different leading on Android, iOS and desktop — the main reason
 * the three modules read as separately generated screens.
 */
@Composable
fun appTypography(): Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.displayLarge,
        lineHeight = TypeScale.displayLargeLine,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.displayMedium,
        lineHeight = TypeScale.displayMediumLine,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.displaySmall,
        lineHeight = TypeScale.displaySmallLine,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.2).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.headlineLarge,
        lineHeight = TypeScale.headlineLargeLine,
        fontWeight = FontWeight.Bold,
        letterSpacing = (-0.5).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.headlineMedium,
        lineHeight = TypeScale.headlineMediumLine,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.4).sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.headlineSmall,
        lineHeight = TypeScale.headlineSmallLine,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.3).sp,
    ),
    titleLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.titleLarge,
        lineHeight = TypeScale.titleLargeLine,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = (-0.1).sp,
    ),
    titleMedium = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.titleMedium,
        lineHeight = TypeScale.titleMediumLine,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.titleSmall,
        lineHeight = TypeScale.titleSmallLine,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = TypeScale.bodyLarge,
        lineHeight = TypeScale.bodyLargeLine,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = TypeScale.bodyMedium,
        lineHeight = TypeScale.bodyMediumLine,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = TypeScale.bodySmall,
        lineHeight = TypeScale.bodySmallLine,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = PoppinsFontFamily,
        fontSize = TypeScale.labelLarge,
        lineHeight = TypeScale.labelLargeLine,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = TypeScale.labelMedium,
        lineHeight = TypeScale.labelMediumLine,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.1.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = DmSansFontFamily,
        fontSize = TypeScale.labelSmall,
        lineHeight = TypeScale.labelSmallLine,
        fontWeight = FontWeight.Normal,
        letterSpacing = 0.2.sp,
    ),
)

/**
 * Semantic type roles.
 *
 * Screens should reach for one of these instead of `MaterialTheme.typography.x`
 * plus an inline `fontWeight` / `fontSize` override. Because the roles are
 * derived from the ramp, restyling the app means editing [appTypography] alone.
 */
object AppText {

    /** Large currency figure, e.g. wallet balance. */
    val balance: TextStyle
        @Composable get() = MaterialTheme.typography.displayLarge

    /** Top-level screen title in a hub header. */
    val screenTitle: TextStyle
        @Composable get() = MaterialTheme.typography.displayMedium

    /** Large figure inside a hero banner. */
    val heroTitle: TextStyle
        @Composable get() = MaterialTheme.typography.displayLarge

    /** Section heading above a group of content. */
    val sectionTitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleLarge

    /** Heading on a top app bar. */
    val barTitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleLarge

    /** Title of a card or list row. */
    val cardTitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)

    /** Subordinate title, e.g. a stat label. */
    val cardSubtitle: TextStyle
        @Composable get() = MaterialTheme.typography.titleSmall

    /** Emphasised monetary value. */
    val price: TextStyle
        @Composable get() = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)

    /** Default running text. */
    val body: TextStyle
        @Composable get() = MaterialTheme.typography.bodyLarge

    /** Running text at reduced emphasis. */
    val bodyMuted: TextStyle
        @Composable get() = MaterialTheme.typography.bodyMedium

    /** Chip / pill / button label. */
    val label: TextStyle
        @Composable get() = MaterialTheme.typography.labelLarge

    /** Small inline label, e.g. inside a filter chip. */
    val chipLabel: TextStyle
        @Composable get() = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)

    /** Metadata line under a title. */
    val caption: TextStyle
        @Composable get() = MaterialTheme.typography.labelSmall

    /** Small all-caps eyebrow label. */
    val overline: TextStyle
        @Composable get() = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
        )
}
