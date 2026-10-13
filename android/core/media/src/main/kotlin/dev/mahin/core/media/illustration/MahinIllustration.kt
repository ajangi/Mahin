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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.media.R

/**
 * Illustration slot for editorial and CMS-delivered imagery.
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
    val showImagery = metadata?.mayRenderImagery() == true
    val frameShape = RoundedCornerShape(MahinSpacing.md)
    val resolvedDescription =
        when {
            decorative -> null
            contentDescription != null -> contentDescription
            showImagery && metadata?.contentDescriptionRes != null ->
                stringResource(requireNotNull(metadata.contentDescriptionRes))
            !metadata?.altText.isNullOrBlank() -> metadata?.altText
            metadata != null && isMedicalGovernedIllustrationId(assetId) ->
                stringResource(R.string.ill_desc_medical_gated)
            else -> null
        }

    val frameModifier =
        modifier
            .size(120.dp)
            .clip(frameShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = frameShape,
            ).testTag(MahinIllustrationTestTags.FRAME)
            .then(
                if (!showImagery && resolvedDescription != null) {
                    Modifier.semantics { this.contentDescription = resolvedDescription }
                } else {
                    Modifier
                },
            )

    val tint = MaterialTheme.colorScheme.onSurfaceVariant
    val context = LocalContext.current

    Box(
        modifier = frameModifier,
        contentAlignment = Alignment.Center,
    ) {
        if (showImagery) {
            when {
                metadata?.drawableRes != null -> {
                    Image(
                        painter = painterResource(requireNotNull(metadata.drawableRes)),
                        contentDescription = resolvedDescription,
                        modifier =
                            Modifier
                                .fillMaxSize()
                                .testTag(MahinIllustrationTestTags.IMAGERY),
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(tint),
                    )
                }
                !metadata?.remoteUrl.isNullOrBlank() -> {
                    AsyncImage(
                        model =
                            ImageRequest
                                .Builder(context)
                                .data(metadata.remoteUrl)
                                .crossfade(false)
                                .build(),
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
    }
}

object MahinIllustrationTestTags {
    const val FRAME = "mahin_illustration_frame"
    const val IMAGERY = "mahin_illustration_imagery"
}
