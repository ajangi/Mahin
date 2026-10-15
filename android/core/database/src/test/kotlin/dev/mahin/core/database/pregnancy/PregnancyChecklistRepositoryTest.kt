package dev.mahin.core.database.pregnancy

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import dev.mahin.core.database.MahinDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class PregnancyChecklistRepositoryTest {
    private lateinit var database: MahinDatabase
    private lateinit var repository: PregnancyChecklistRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
                .allowMainThreadQueries()
                .build()
        repository = PregnancyChecklistRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun addRenameDelete_persistLocally() {
        runBlocking {
            repository.seedFromTemplate(
                listOf(
                    PregnancyChecklistRepository.TemplateItem(
                        templateId = "t1",
                        section = "مدارک",
                        label = "کارت ملی",
                        order = 1,
                    ),
                ),
            )
            repository.addItem("لوازم", "شال")
            val added = repository.observeHospitalBag().first().first { it.label == "شال" }
            repository.renameItem(added.id, "شال گرم")
            assertThat(repository.observeHospitalBag().first().any { it.label == "شال گرم" }).isTrue()
            repository.deleteItem(added.id)
            assertThat(repository.observeHospitalBag().first().none { it.id == added.id }).isTrue()
        }
    }
}
