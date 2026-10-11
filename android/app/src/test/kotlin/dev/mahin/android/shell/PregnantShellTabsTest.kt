package dev.mahin.android.shell

import com.google.common.truth.Truth.assertThat
import dev.mahin.android.navigation.MahinTopLevelDestination
import dev.mahin.core.model.ReproductiveMode
import org.junit.Test

class PregnantShellTabsTest {
    @Test
    fun pregnantMode_bottomBar_excludesCalendarTab() {
        val routes = MahinTopLevelDestination.forMode(ReproductiveMode.PREGNANT).map { it.route }
        assertThat(routes).doesNotContain(MahinTopLevelDestination.Calendar.route)
    }
}
