package dev.mahin.android.healthconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
                Column(modifier = Modifier.padding(MahinSpacing.md)) {
                    Text(
                        text = stringResource(R.string.health_connect_rationale_activity),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    TextButton(
                        onClick = {
                            startViewUriSafely(getString(R.string.health_connect_privacy_policy_url))
                        },
                    ) {
                        Text(stringResource(R.string.health_connect_privacy_policy_link))
                    }
                }
            }
        }
    }
}
