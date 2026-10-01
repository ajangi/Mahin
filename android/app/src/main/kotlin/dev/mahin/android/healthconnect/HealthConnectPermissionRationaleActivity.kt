package dev.mahin.android.healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.mahin.android.R
import dev.mahin.core.designsystem.MahinSpacing
import dev.mahin.core.designsystem.MahinTheme

/**
 * Required entry for Health Connect permission rationale (Android 14+).
 */
class HealthConnectPermissionRationaleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MahinTheme {
                Text(
                    text = stringResource(R.string.health_connect_rationale_activity),
                    modifier = Modifier.padding(MahinSpacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
