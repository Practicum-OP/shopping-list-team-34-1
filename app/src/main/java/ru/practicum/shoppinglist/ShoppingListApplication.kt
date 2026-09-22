package ru.practicum.shoppinglist

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ru.practicum.shoppinglist.di.appModule
import ru.practicum.shoppinglist.presentation.lists.listsModule

class ShoppingListApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@ShoppingListApplication)
            modules(
                appModule,
                listsModule,
            )
        }
    }
}
