package dev.mahin.core.testing

import dev.mahin.core.common.MahinDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class TestMahinDispatchers(
    private val dispatcher: TestDispatcher = StandardTestDispatcher(),
) : MahinDispatchers {
    override val io: CoroutineDispatcher = dispatcher
    override val default: CoroutineDispatcher = dispatcher
    override val main: CoroutineDispatcher = dispatcher
}
