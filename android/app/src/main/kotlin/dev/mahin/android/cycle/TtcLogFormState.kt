package dev.mahin.android.cycle

import dev.mahin.core.model.CervicalMucusType
import dev.mahin.core.model.OvulationTestResult
import dev.mahin.core.model.PregnancyTestResult

data class TtcLogFormState(
    val bbtInput: String = "",
    val ovulationTest: OvulationTestResult? = null,
    val cervicalMucus: CervicalMucusType? = null,
    val intercourseLogged: Boolean = false,
    val intercourseProtected: Boolean? = null,
    val pregnancyTest: PregnancyTestResult? = null,
)

data class TtcLogFormCallbacks(
    val onBbtChange: (String) -> Unit,
    val onOvulationTestSelected: (OvulationTestResult) -> Unit,
    val onCervicalMucusSelected: (CervicalMucusType) -> Unit,
    val onIntercourseToggle: () -> Unit,
    val onIntercourseProtectedSelected: (Boolean?) -> Unit,
    val onPregnancyTestSelected: (PregnancyTestResult) -> Unit,
)
