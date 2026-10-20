package dev.mahin.core.testing

import android.content.Context
import androidx.room.Room
import dev.mahin.core.database.MahinDatabase
import java.util.concurrent.Executor

/** Runs Room work on the calling thread (deterministic teardown with [allowMainThreadQueries]). */
val MahinDirectRoomExecutor: Executor = Executor { command -> command.run() }

fun buildMahinInMemoryDatabase(context: Context): MahinDatabase =
    buildMahinInMemoryDatabase(
        context = context,
        queryExecutor = MahinDirectRoomExecutor,
    )

fun buildMahinInMemoryDatabase(
    context: Context,
    queryExecutor: Executor,
    transactionExecutor: Executor = queryExecutor,
): MahinDatabase {
    val database =
        Room
            .inMemoryDatabaseBuilder(context, MahinDatabase::class.java)
            .setQueryExecutor(queryExecutor)
            .setTransactionExecutor(transactionExecutor)
            .allowMainThreadQueries()
            .build()
    database.openHelper.writableDatabase
    return database
}
