package dev.mahin.core.media.illustration

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.media.R

/**
 * Illustration slot for editorial and (future) CMS-delivered imagery.
 *
 * @param decorative When true, imagery is hidden from accessibility services.
 */
@Composable
fun MahinIllustration(
    assetId: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    decorative: Boolean = false,
    source: MahinIllustrationSource = BundledMahinIllustrationCatalog,
) {
    val metadata = source.metadataFor(assetId)
    val showImagery = metadata?.mayRenderBundledImagery() == true
    val frameShape = RoundedCornerShape(MahinSpacing.md)
    val frameModifier =
        modifier
            .size(120.dp)
            .clip(frameShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = frameShape,
            ).testTag(MahinIllustrationTestTags.FRAME)

    val resolvedDescription =
        when {
            decorative -> null
            contentDescription != null -> contentDescription
            showImagery && metadata?.contentDescriptionRes != null ->
                stringResource(requireNotNull(metadata.contentDescriptionRes))
            metadata != null && isMedicalGovernedIllustrationId(assetId) ->
                stringResource(R.string.ill_desc_medical_gated)
            else -> null
        }

    Box(
        modifier =
            frameModifier.semantics {
                if (resolvedDescription != null) {
                    this.contentDescription = resolvedDescription
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        if (showImagery) {
            Image(
                painter = painterResource(requireNotNull(metadata?.drawableRes)),
                contentDescription = resolvedDescription,
                modifier =
                    Modifier
                        .fillMaxSize()
                        .testTag(MahinIllustrationTestTags.IMAGERY),
                contentScale = ContentScale.Fit,
            )
        }
    }
}

object MahinIllustrationTestTags {
    const val FRAME = "mahin_illustration_frame"
    const val IMAGERY = "mahin_illustration_imagery"
}
