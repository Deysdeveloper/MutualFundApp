# Mutual Fund App

An Android app for exploring, searching, and tracking mutual funds — built as an assignment for the **Groww Android Intern** role.

---
Demo link : https://drive.google.com/file/d/1072qeq3mzehwXqPs1klQ0Gw_zpYImvgU/view?usp=sharing

## Features

- **Explore** — Browse mutual funds across curated categories (Index, Bluechip, Tax Saver, Large Cap) with offline-first caching
- **Search** — Real-time fund search with 300ms debounce to avoid API spam
- **Analysis** — Full fund details with NAV history chart, time filters (6M / 1Y / ALL), and NAV change indicator
- **My Portfolios** — Create watchlist folders, save funds into them, swipe left to remove a fund, and tap the delete icon to remove a portfolio
- **Offline Support** — Explore screen shows cached data from Room instantly even with no internet

---

## Architecture

The app follows **MVVM (Model-View-ViewModel)** with a strict separation of concerns:

```
UI Layer          ViewModel Layer       Repository Layer       Data Layer
─────────         ───────────────       ────────────────       ──────────
Composables  ←→   StateFlow /      ←→   FundRepository    ←→   Retrofit (API)
                  UiState sealed         WatchlistRepo     ←→   Room DB
                  classes
```

### Layer breakdown

| Layer | Responsibility |
|---|---|
| **UI** (`ui/`) | Composables that observe `StateFlow` via `collectAsState()`. Zero business logic. |
| **ViewModel** (`ui/*/ViewModel`) | Holds `UiState` sealed classes, triggers repository calls, survives rotation. |
| **Repository** (`data/repository/`) | Single source of truth — coordinates between API and Room cache. |
| **DAO** (`data/local/dao/`) | Room `@Query` / `@Insert` / `@Delete` methods returning `Flow` for reactivity. |
| **API** (`data/api/`) | Retrofit interface over `https://api.mfapi.in/`. |
| **DI** (`di/`) | Hilt modules wiring everything together at compile time. |

### Key design patterns

- **Offline-first with `channelFlow`** — `FundRepository.getFundsByCategory()` emits cached Room data immediately, fetches from the API in parallel, then Room's reactive `Flow` re-emits the updated list automatically. No internet = cached data is still shown.
- **Sealed `UiState`** — Each screen's ViewModel exposes `Loading | Success | Error` (or similar) so the UI is always in a defined, exhaustive state.
- **Reactive Room** — `@Query` methods return `Flow<List<T>>`, so the UI updates automatically whenever the DB changes — no manual polling or callbacks.
- **Debounced search** — `MutableStateFlow<String>` with `.debounce(300ms).distinctUntilChanged()` prevents excessive API calls while typing.
- **Cascade deletes** — `WatchlistFund` has a foreign key on `WatchlistFolder` with `CASCADE` on delete, so removing a portfolio folder automatically removes all its saved funds.

---

## Tech Stack

| Library | Version | Purpose |
|---|---|---|
| **Kotlin** | 2.2.10 | Language |
| **Jetpack Compose** | BOM 2024.12.01 | Declarative UI |
| **Material 3** | via Compose BOM | Design system, components |
| **Navigation 3** | 1.0.1 | Type-safe navigation with serializable routes |
| **Hilt** | 2.56 | Compile-time dependency injection |
| **Room** | 2.6.1 | Local SQLite database with reactive `Flow` queries |
| **KSP** | 2.3.2 | Annotation processor for Room + Hilt |
| **Retrofit** | 2.9.0 | HTTP client for REST API calls |
| **Gson Converter** | 2.9.0 | JSON deserialization |
| **OkHttp Logging** | 4.12.0 | Network request/response logging |
| **MPAndroidChart** | 3.1.0 | NAV history line chart |
| **Coroutines** | 1.7.3 | Async work, Flow operators |
| **Kotlinx Serialization** | 1.6.3 | Route serialization for Navigation 3 |

---

## Project Structure

```
app/src/main/java/com/deysdeveloper/mutualfundapp/
│
├── data/
│   ├── api/
│   │   └── MfApiService.kt          # Retrofit interface (mfapi.in)
│   ├── local/
│   │   ├── AppDatabase.kt           # Room database (version 2)
│   │   ├── dao/
│   │   │   ├── WatchlistDao.kt      # Folder + fund CRUD
│   │   │   └── CachedFundDao.kt     # Explore cache CRUD
│   │   └── entity/
│   │       ├── WatchlistFolder.kt
│   │       ├── WatchlistFund.kt     # FK → WatchlistFolder (CASCADE)
│   │       └── CachedFund.kt        # Explore offline cache
│   └── repository/
│       ├── FundRepository.kt        # API + cache coordination
│       └── WatchlistRepository.kt   # Watchlist DB operations
│
├── di/
│   ├── NetworkModule.kt             # Retrofit, OkHttp, MfApiService
│   ├── DatabaseModule.kt            # Room DB, DAOs
│   └── RepositoryModule.kt          # Repository bindings
│
├── domain/model/
│   ├── Fund.kt                      # Search result model
│   └── FundDetails.kt               # FundDetailsResponse, FundMeta, NavEntry
│
└── ui/
    ├── explore/
    │   ├── ExploreScreen.kt
    │   ├── ExploreViewModel.kt      # Per-category Flow collection
    │   ├── CategoryListScreen.kt    # "See All" full fund list
    │   └── CategoryListViewModel.kt
    ├── search/
    │   ├── SearchScreen.kt
    │   └── SearchViewModel.kt       # Debounced search
    ├── watchlist/
    │   ├── WatchlistScreen.kt       # Portfolio list + delete dialog
    │   ├── FolderDetailScreen.kt    # Swipe-to-delete funds
    │   └── WatchlistViewModel.kt
    ├── product/
    │   ├── ProductScreen.kt         # Analysis screen + chart + time filters
    │   ├── ProductViewModel.kt
    │   └── WatchlistBottomSheet.kt  # Save fund to portfolio(s)
    ├── navigation/
    │   ├── NavRoutes.kt             # Sealed Route interface (typed Nav 3 keys)
    │   └── AppNavigation.kt         # NavDisplay + bottom bar (2 tabs)
    └── theme/
        └── ...                      # Material 3 color + typography
```

---

## API

Uses the free, public [mfapi.in](https://mfapi.in) API — no API key required.

| Endpoint | Usage |
|---|---|
| `GET /mf/search?q={query}` | Search funds by name/keyword |
| `GET /mf/{schemeCode}` | Fetch NAV history + fund metadata |

---

## Database Schema

```
watchlist_folders          watchlist_funds               cached_funds
─────────────────          ───────────────               ────────────
id     INTEGER PK          id         INTEGER PK          id          INTEGER PK
name   TEXT                schemeCode TEXT                category    TEXT
                           folderId   INTEGER FK→folders  schemeCode  TEXT
                                      (CASCADE DELETE)    fundName    TEXT
                                                          nav         TEXT
```

---

## Run Instructions

### Prerequisites

| Tool | Version |
|---|---|
| Android Studio | Meerkat (2024.3) or newer |
| JDK | 17 |
| Android SDK | API 26+ (minSdk 26), API 36 (compileSdk) |
| Kotlin | 2.2.10 |

### Steps

```bash
# 1. Clone the repository
git clone <your-repo-url>
cd MutualFundApp

# 2. Open in Android Studio
#    File → Open → select the MutualFundApp folder

# 3. Let Gradle sync finish (it will download all dependencies automatically)

# 4. Run on an emulator or physical device
#    Run → Run 'app'  (or press Shift+F10)
```

> No API keys, no Firebase config, no `.env` files required. The app works out of the box — internet is only needed for fresh data; the Explore screen will show cached data offline after the first load.

### Build from terminal

```bash
# Debug APK
./gradlew assembleDebug

# Output location
app/build/outputs/apk/debug/app-debug.apk
```

---

## Screens

| Screen | Route |
|---|---|
| Explore | `Route.Explore` (default) |
| Search | `Route.Search` (pushed from Explore search bar) |
| Category List | `Route.CategoryList(label, query)` (pushed from "See All") |
| Analysis / Fund Detail | `Route.Product(schemeCode)` |
| My Portfolios | `Route.Watchlist` |
| Folder / Portfolio Detail | `Route.Folder(folderId, folderName)` |
