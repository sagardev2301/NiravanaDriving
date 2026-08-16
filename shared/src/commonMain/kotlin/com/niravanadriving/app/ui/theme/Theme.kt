package com.niravanadriving.app.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import niravanadriving.shared.generated.resources.Res
import niravanadriving.shared.generated.resources.metropolis_bold
import niravanadriving.shared.generated.resources.metropolis_medium
import niravanadriving.shared.generated.resources.metropolis_regular
import niravanadriving.shared.generated.resources.metropolis_semibold
import org.jetbrains.compose.resources.Font


// ─────────────────────────────────────────────────────────────
// COLORS — exact values from DESIGN.md
// ─────────────────────────────────────────────────────────────

private val Surface = Color(0xFFFDF8FB)
private val SurfaceDim = Color(0xFFDDD9DB)
private val SurfaceBright = Color(0xFFFDF8FB)
private val SurfaceContainerLowest = Color(0xFFFFFFFF)
private val SurfaceContainerLow = Color(0xFFF7F2F5)
private val SurfaceContainer = Color(0xFFF1EDEF)
private val SurfaceContainerHigh = Color(0xFFEBE7E9)
private val SurfaceContainerHighest = Color(0xFFE6E1E4)
private val OnSurface = Color(0xFF1C1B1D)
private val OnSurfaceVariant = Color(0xFF484554)
private val InverseSurface = Color(0xFF313032)
private val InverseOnSurface = Color(0xFFF4EFF2)
private val Outline = Color(0xFF797586)
private val OutlineVariant = Color(0xFFCAC4D7)
private val SurfaceTint = Color(0xFF6341D5)

private val Primary = Color(0xFF4C22BD)
private val OnPrimary = Color(0xFFFFFFFF)
private val PrimaryContainer = Color(0xFF6442D6)
private val OnPrimaryContainer = Color(0xFFDED4FF)
private val InversePrimary = Color(0xFFCBBEFF)

private val Secondary = Color(0xFF7F5700)
private val OnSecondary = Color(0xFFFFFFFF)
private val SecondaryContainer = Color(0xFFFEB316)
private val OnSecondaryContainer = Color(0xFF6A4800)

private val Tertiary = Color(0xFF4C2FA6)
private val OnTertiary = Color(0xFFFFFFFF)
private val TertiaryContainer = Color(0xFF644AC0)
private val OnTertiaryContainer = Color(0xFFDED4FF)

private val Error = Color(0xFFBA1A1A)
private val OnError = Color(0xFFFFFFFF)
private val ErrorContainer = Color(0xFFFFDAD6)
private val OnErrorContainer = Color(0xFF93000A)

private val PrimaryFixed = Color(0xFFE7DEFF)
private val PrimaryFixedDim = Color(0xFFCBBEFF)
private val OnPrimaryFixed = Color(0xFF1E0061)
private val OnPrimaryFixedVariant = Color(0xFF4B21BD)

private val SecondaryFixed = Color(0xFFFFDEAD)
private val SecondaryFixedDim = Color(0xFFFFBA3B)
private val OnSecondaryFixed = Color(0xFF281900)
private val OnSecondaryFixedVariant = Color(0xFF604100)

private val TertiaryFixed = Color(0xFFE7DEFF)
private val TertiaryFixedDim = Color(0xFFCCBEFF)
private val OnTertiaryFixed = Color(0xFF1E0060)
private val OnTertiaryFixedVariant = Color(0xFF4B2EA6)

private val Background = Color(0xFFFEFBFF)
private val OnBackground = Color(0xFF1C1B1D)
private val SurfaceVariant = Color(0xFFF2ECEE)

// Semantic colors — NOT part of standard M3 ColorScheme, exposed separately below
private val Caution = Color(0xFFF2A900)
private val Success = Color(0xFF2E7D32)

private val NirvanaLightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    inversePrimary = InversePrimary,

    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,

    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,

    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer,

    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceTint = SurfaceTint,

    outline = Outline,
    outlineVariant = OutlineVariant,

    inverseSurface = InverseSurface,
    inverseOnSurface = InverseOnSurface,

    surfaceDim = SurfaceDim,
    surfaceBright = SurfaceBright,
    surfaceContainerLowest = SurfaceContainerLowest,
    surfaceContainerLow = SurfaceContainerLow,
    surfaceContainer = SurfaceContainer,
    surfaceContainerHigh = SurfaceContainerHigh,
    surfaceContainerHighest = SurfaceContainerHighest,

    // NOTE: "fixed" color roles require Compose Material3 1.3.0+.
    // If this doesn't compile, remove these 12 params — your BOM version
    // may predate expressive M3 support. Everything else still works fine.
    primaryFixed = PrimaryFixed,
    primaryFixedDim = PrimaryFixedDim,
    onPrimaryFixed = OnPrimaryFixed,
    onPrimaryFixedVariant = OnPrimaryFixedVariant,
    secondaryFixed = SecondaryFixed,
    secondaryFixedDim = SecondaryFixedDim,
    onSecondaryFixed = OnSecondaryFixed,
    onSecondaryFixedVariant = OnSecondaryFixedVariant,
    tertiaryFixed = TertiaryFixed,
    tertiaryFixedDim = TertiaryFixedDim,
    onTertiaryFixed = OnTertiaryFixed,
    onTertiaryFixedVariant = OnTertiaryFixedVariant
)

// ─────────────────────────────────────────────────────────────
// EXTENDED (semantic) COLORS — caution & success aren't standard
// M3 roles, so they're provided via their own CompositionLocal.
// Usage: NirvanaTheme.extendedColors.success
// ─────────────────────────────────────────────────────────────

data class ExtendedColors(
    val caution: Color,
    val onCaution: Color = Color.White,
    val success: Color,
    val onSuccess: Color = Color.White
)

private val LightExtendedColors = ExtendedColors(
    caution = Caution,
    success = Success
)

private val LocalExtendedColors = staticCompositionLocalOf {
    LightExtendedColors
}

object NirvanaTheme {
    val extendedColors: ExtendedColors
        @Composable
        get() = LocalExtendedColors.current
}

// ─────────────────────────────────────────────────────────────
// TYPOGRAPHY — Metropolis
//
// Metropolis is the free, open-source geometric sans-serif that
// Zomato's proprietary "Okra" typeface is built on top of — same
// design language (Swiggy/Zomato-style geometric sans), legally
// bundleable. (Swiggy's actual font, Proxima Nova, is a paid
// commercial license and isn't used here for that reason.)
//
// TODO: Currently FontFamily.Default (system font) as a placeholder.
// To use real Metropolis:
//   1. Download Metropolis .otf/.ttf files (free): https://fonts.cdnfonts.com/css/metropolis
//      or https://github.com/olafleur/metropolis (Regular + Medium + SemiBold weights)
//   2. Place them in composeApp/src/commonMain/composeResources/font/
//      (e.g. metropolis_regular.ttf, metropolis_medium.ttf, metropolis_semibold.ttf)
//   3. Replace `FontFamily.Default` below with:
//      FontFamily(
//          Font(Res.font.metropolis_regular, FontWeight.Normal),
//          Font(Res.font.metropolis_medium, FontWeight.Medium),
//          Font(Res.font.metropolis_semibold, FontWeight.SemiBold)
//      )
//      (requires `import nirvanadrive.composeapp.generated.resources.Res`
//      generated by the Compose Resources plugin)
// ─────────────────────────────────────────────────────────────

@Composable
fun getNirvanaTypography(): Typography {
    val metropolisFontFamily = FontFamily(
        Font(Res.font.metropolis_regular, FontWeight.Normal),
        Font(Res.font.metropolis_medium, FontWeight.Medium),
        Font(Res.font.metropolis_semibold, FontWeight.SemiBold),
        Font(Res.font.metropolis_bold, FontWeight.Bold)
    )

    return Typography(
        displayLarge = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        headlineLarge = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp
        ),
        // "headline-lg-mobile" from spec — used as headlineMedium since
        // mobile is this app's only real target for now
        headlineMedium = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp
        ),
        titleLarge = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp
        ),
        titleMedium = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
        labelLarge = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = metropolisFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

// ─────────────────────────────────────────────────────────────
// SHAPES — from DESIGN.md `rounded` scale
// sm=4dp, DEFAULT=8dp, md=12dp, lg=16dp, xl=24dp, full=pill
// ─────────────────────────────────────────────────────────────

val NirvanaShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // rounded-sm
    small = RoundedCornerShape(8.dp),         // rounded (buttons, text fields)
    medium = RoundedCornerShape(12.dp),       // rounded-md
    large = RoundedCornerShape(16.dp),        // rounded-lg (cards, modals)
    extraLarge = RoundedCornerShape(24.dp)    // rounded-xl (large FABs)
)

val PillShape = CircleShape // for fully circular/pill FABs and chips

// ─────────────────────────────────────────────────────────────
// THEME ENTRY POINT
// ─────────────────────────────────────────────────────────────

@Composable
fun NirvanaDriveTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalExtendedColors provides LightExtendedColors) {
        MaterialTheme(
            colorScheme = NirvanaLightColorScheme,
            typography = getNirvanaTypography(),
            shapes = NirvanaShapes,
            content = content
        )
    }
}