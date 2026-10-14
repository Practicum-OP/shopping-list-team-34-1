package ru.practicum.shoppinglist.di

import androidx.room.Room
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import ru.practicum.shoppinglist.data.local.database.ShoppingListDatabase
import ru.practicum.shoppinglist.data.local.database.ShoppingListMigrations
import ru.practicum.shoppinglist.data.repository.ShoppingItemRepositoryImpl
import ru.practicum.shoppinglist.data.repository.ShoppingListRepositoryImpl
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingItemInteractor
import ru.practicum.shoppinglist.domain.api.interactor.ShoppingListInteractor
import ru.practicum.shoppinglist.domain.api.repository.ShoppingItemRepository
import ru.practicum.shoppinglist.domain.api.repository.ShoppingListRepository
import ru.practicum.shoppinglist.domain.impl.ShoppingItemInteractorImpl
import ru.practicum.shoppinglist.domain.impl.ShoppingListInteractorImpl

internal val appModule = module {

    single {
        Room.databaseBuilder(
            androidContext(),
            ShoppingListDatabase::class.java,
            ShoppingListDatabase.DATABASE_NAME,
        ).addMigrations(
            ShoppingListMigrations.MIGRATION_1_2,
        ).build()
    }

    single {
        get<ShoppingListDatabase>().shoppingListDao()
    }

    single {
        get<ShoppingListDatabase>().shoppingItemDao()
    }

    single {
        get<ShoppingListDatabase>().productNameDao()
    }

    single<ShoppingListRepository> {
        ShoppingListRepositoryImpl(
            database = get(),
            shoppingListDao = get(),
            shoppingItemDao = get(),
        )
    }

    single<ShoppingItemRepository> {
        ShoppingItemRepositoryImpl(
            database = get(),
            shoppingItemDao = get(),
            productNameDao = get(),
        )
    }

    single<ShoppingListInteractor> {
        ShoppingListInteractorImpl(
            repository = get(),
        )
    }

    single<ShoppingItemInteractor> {
        ShoppingItemInteractorImpl(
            repository = get(),
        )
    }
}
