# Shopping List

Командный проект Яндекс Практикума — приложение для создания и ведения списков покупок.

## Технологический стек

- Kotlin
- Jetpack Compose
- Material 3
- MVI: UiState + Action + Effect
- Room
- KSP
- Koin
- Coroutines и Flow
- Navigation Compose
- detekt
- Gradle Kotlin DSL
- Version Catalog

Сетевой слой не добавлен, поскольку в текущем техническом задании отсутствуют сетевые сценарии.

## Требования

- Android Studio
- JDK 17
- Android SDK 37
- Минимальная версия приложения: Android 10, API 29

## Сборка проекта

Для Windows PowerShell:

```powershell
.\gradlew assembleDebug


## Архитектура

Приложение использует Compose и MVI. Проект состоит из одного Gradle-модуля `app`.

### Структура пакетов

```text
ru.practicum.shoppinglist
├── data
│   ├── local
│   │   ├── dao
│   │   ├── database
│   │   └── entity
│   └── repository
├── domain
│   ├── api
│   └── impl
├── presentation
│   ├── navigation
│   ├── lists
│   ├── editor
│   └── components
├── di
├── ui
│   └── theme
├── MainActivity.kt
├── ShoppingListApp.kt
└── ShoppingListApplication.kt
```

Пустые пакеты заранее не создаются. Они добавляются вместе с соответствующими классами.

### Назначение слоёв

- `data` — Room, DAO, Entity и реализации репозиториев.
- `domain` — модели приложения и интерфейсы репозиториев.
- `presentation` — экраны Compose, ViewModel, MVI-контракты и навигация.
- `di` — модули Koin.
- `ui.theme` — цвета, типографика и тема приложения.

Слой `presentation` не должен напрямую обращаться к Room или DAO. ViewModel работает с интерфейсами репозиториев из слоя `domain`.

### MVI

Каждый экран использует собственный контракт:

- `State` — полное состояние экрана;
- `Intent` — действия пользователя;
- `Effect` — одноразовые события, например навигация или показ сообщения.

Состояние передаётся из ViewModel через `StateFlow`. Экран отправляет действия во ViewModel через функцию `onIntent()`.

Пример расположения файлов экрана (не обязательно так всё должно быть):

```text
presentation/lists
├── ShoppingListsState.kt
├── ShoppingListsIntent.kt
├── ShoppingListsEffect.kt
├── ShoppingListsViewModel.kt
└── ShoppingListsScreen.kt
```

UI-компоненты не обращаются к базе данных, репозиториям и NavController напрямую. Переходы передаются в экран через callback-функции.

### Навигация

Все маршруты объявляются в `AppRoute`. Навигационный граф находится в `ShoppingListNavHost`.

- отсутствие `listId` означает создание нового списка;
- наличие `listId` означает редактирование существующего списка.

### Dependency Injection

Koin запускается в `ShoppingListApplication`. Зависимости приложения объявляются в пакете `di`.