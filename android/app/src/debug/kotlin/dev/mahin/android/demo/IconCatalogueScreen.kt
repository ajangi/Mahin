package dev.mahin.android.demo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTheme
import dev.mahin.core.designsystem.MahinTypographyRole
import dev.mahin.core.designsystem.icon.IconCatalogueFamilyGoldenSheet
import dev.mahin.core.designsystem.icon.IconCatalogueGrid
import dev.mahin.core.designsystem.icon.MahinIcons
import dev.mahin.core.designsystem.mahinTextStyle

/**
 * Debug-only catalogue of every [MahinIcons] entry (M14b). Not linked from production navigation.
 */
@Composable
fun IconCatalogueScreen(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = false,
) {
    MahinTheme(darkTheme = darkTheme) {
        Column(
            modifier =
                modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(MahinSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(MahinSpacing.xl),
        ) {
            Text(
                text = stringResource(R.string.icon_catalogue_title),
                style = mahinTextStyle(MahinTypographyRole.TitleLarge),
                color = MaterialTheme.colorScheme.onBackground,
            )
            catalogueSection(R.string.icon_catalogue_family_nav, "nav")
            catalogueSection(R.string.icon_catalogue_family_action, "action")
            catalogueSection(R.string.icon_catalogue_family_flow, "flow")
            catalogueSection(R.string.icon_catalogue_family_symptom, "symptom")
            catalogueSection(R.string.icon_catalogue_family_mood, "mood")
            catalogueSection(R.string.icon_catalogue_family_discharge, "discharge")
            catalogueSection(R.string.icon_catalogue_family_tests, "tests")
            catalogueSection(R.string.icon_catalogue_family_lifestyle, "lifestyle")
        }
    }
}

@Composable
private fun catalogueSection(
    titleRes: Int,
    familyPrefix: String,
) {
    val title = stringResource(titleRes)
    Column(verticalArrangement = Arrangement.spacedBy(MahinSpacing.md)) {
        Text(
            text = title,
            style = mahinTextStyle(MahinTypographyRole.Title),
            color = MaterialTheme.colorScheme.onBackground,
        )
        IconCatalogueGrid(icons = MahinIcons.all.filter { it.semanticId.startsWith("$familyPrefix/") })
    }
}

/** Convenience wrapper for debug previews. */
@Composable
fun IconCatalogueFamilySheetDebug(
    familyPrefix: String,
    titleRes: Int,
    darkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    IconCatalogueFamilyGoldenSheet(
        familyPrefix = familyPrefix,
        title = stringResource(titleRes),
        darkTheme = darkTheme,
        modifier = modifier,
    )
}
