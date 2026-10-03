package dev.mahin.core.healthconnect

/**
 * Verified against androidx.health.connect:connect-client 1.1.0-alpha11 (see docs/health-connect/SDK_POLICY_VERIFICATION.md).
 */
object HealthConnectPermissionEducation {
    val permissionRationaleFa: List<HealthConnectPermissionRationale> =
        listOf(
            HealthConnectPermissionRationale(
                recordLabelFa = "جریان پریود (Menstruation Flow)",
                reasonFa =
                    "برای همگام‌سازی روزهای پریودی که در ماهین ثبت می‌کنید با Health Connect " +
                        "و دریافت دادهٔ مشابه از سایر اپ‌ها (در صورت انتخاب شما).",
                mapsToFeatureFa = "ثبت پریود و تقویم چرخه",
            ),
        )
}

data class HealthConnectPermissionRationale(
    val recordLabelFa: String,
    val reasonFa: String,
    val mapsToFeatureFa: String,
)
