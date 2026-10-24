# d1 wallet

A simple wallet android application for Android Mobile Developer Code Challenge. You log in with demo credentials, see a list of currency accounts, open any account to see the last 30 days of transactions, and log out.

There is no backend. Authentication, accounts, and transactions all come from in-memory mock repositories, and the balances are demo data.

## Requirements

| | Version used |
|---|---|
| JDK | 17 (developed with the JetBrains Runtime 17.0.12 bundled with Android Studio) |
| Android Gradle Plugin | 9.4.1 |
| Gradle | 9.8.2, via the included wrapper |
| compileSdk / targetSdk | 36 |
| minSdk | 26 (Android 8.0). This lets the app use `java.time` without desugaring. |
| Android SDK | Platform 36 installed through the SDK Manager |

Any recent Android Studio that supports AGP 9.4 should open the project. On the command line you only need JDK 17 and an Android SDK. Gradle downloads itself through the wrapper.

## How to run

```bash
git clone https://github.com/d1vivek/d1Wallet.git
cd wallet_app_code_challange

# Tell Gradle where the Android SDK is, if ANDROID_HOME isn't set
echo "sdk.dir=$HOME/Library/Android/sdk" > local.properties   # macOS default path

./gradlew assembleDebug          # build the APK -> app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # run the unit tests
./gradlew lintDebug              # run Android lint -> app/build/reports/lint-results-debug.html
./gradlew installDebug           # install on a running emulator or connected device
```

Or open the folder in Android Studio and run the `app` configuration.

## Test credentials

- Username: `demo@example.com`
- Password: `password123`

These are mock credentials from the challenge instruction, not real credentials. The email match ignores case and surrounding spaces. The password is case-sensitive.

## What's in the app

**Login.** Empty fields get inline errors. While the request runs, the button is disabled and shows "Logging in…", so you can't submit twice. Wrong credentials and service failures each get their own message. The mock adds a 0.9 s delay so you can see the loading state.

**Dashboard.** Shows four accounts from the repository: USD, EUR, SGD, and an NPR travel account. Each card shows the name, currency, and balance. Tapping a card opens its statement. Loading, empty, and error states are all handled, and errors offer a retry. The toolbar has a logout action.

**Statement.** The header shows the account and its current balance. Below it, the last 30 days of transactions are listed newest first, each with date and time, description, signed amount, a type label (Deposit, Payment, Fee…), and the balance after the transaction. Credits are green and debits are red, also with the `+`/`−` sign respectively. The NPR account only has transactions older than 30 days, so it shows the empty state. An unknown or missing account ID shows "Account not found" instead of crashing.

**Logout.** Asks for confirmation first, then clears the mock session and opens Login on a fresh task (`FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK`), so Back can't return to the wallet. The account and statement use cases check the session too, so they refuse to load data after logout.

## Architecture

MVVM on top of a light domain/data split. Presentation depends on domain interfaces, data implements them, and domain doesn't depend on Android.

```
com.d1vivek.wallet
├── app/            WalletApplication, AppContainer (composition root)
├── presentation/   Activities, ViewModels, adapters, UI state, formatters
│   ├── login/  wallet/  statement/  common/
├── domain/         Models, repository interfaces, use cases, domain exceptions
└── data/           Mock repositories and the in-memory data source
```

How data moves, using the statement as an example:

1. `WalletActivity` passes only the account ID to `StatementActivity` in an Intent extra.
2. `StatementViewModel` runs `GetAccountStatementUseCase` on a background thread.
3. The use case checks the session, looks up the account, reads the clock once, filters the transactions to `[now − 30 days, now]`, and sorts them.
4. The ViewModel turns the result or exception into a `UiState` (loading, success, empty, or error) and publishes it through `LiveData`.
5. The Activity observes that state and renders it. It contains no business rules.

Some decisions worth calling out:

- **Dependency wiring.** `AppContainer` builds the repositories and ViewModel factories through plain constructor injection. It's the only class that knows the mocks exist, so swapping in real repositories only means changing that one class.
- **Use cases.** There are three: `LoginUseCase` (validation plus sign-in), `GetAccountsUseCase` (session check), and `GetAccountStatementUseCase` (session check, the 30-day filter, and ordering). Each one holds real logic, not just a pass-through call.
- **Background work.** Each ViewModel owns a single-thread executor through `BackgroundTasks` and shuts it down in `onCleared()`. Results come back on the main thread, and only the newest request can deliver one, so a slow, stale response can't overwrite newer state.
- **Errors.** Repositories throw checked domain exceptions (`DataUnavailableException`, `AccountNotFoundException`, `NotSignedInException`). ViewModels map them to error types, and Activities pick the matching string resource. Users never see raw exception text.

### Money and time

- Amounts are `BigDecimal` and must match the currency's minor-unit scale: 2 decimal places for USD, EUR, SGD, and NPR. Currencies with no minor unit, such as KRW, would need 0. Amounts with any other scale are rejected rather than silently rounded.
- Amounts are signed. Credits (deposit, transfer in, refund) are positive. Debits (payment, transfer out, withdrawal, fee) are negative. The `Transaction` constructor rejects a sign that doesn't match the type.
- The mock ledgers start from an opening balance and replay oldest to newest. That means each `balanceAfter` is the previous balance plus the amount, and the account balance is the last entry's `balanceAfter`. A unit test checks this for every account.
- Timestamps are stored as UTC `Instant`s and only converted to the device time zone when displayed. The statement window covers exactly 30 × 24 hours back from one `clock.instant()` reading, and both ends are inclusive. Future-dated entries are excluded. Entries with the same timestamp are ordered by transaction ID, descending.
- Money is formatted with `NumberFormat.getCurrencyInstance(locale)`, so the currency symbol comes from the device locale and isn't hardcoded. One result: in en-US, Android shows Singapore dollars as "SGD8,250.00" rather than "S$8,250.00".

## Dependencies

| Library | Why |
|---|---|
| `androidx.appcompat` | `AppCompatActivity` and theme support |
| `androidx.activity` | `EdgeToEdge.enable()`, because targetSdk 36 makes edge-to-edge the default |
| `androidx.lifecycle` ViewModel + LiveData | Screen state that survives rotation |
| `androidx.recyclerview` | Account and transaction lists |
| `androidx.constraintlayout` | Transaction rows and the width-capped login form |
| `com.google.android.material` | Text fields, buttons, cards, toolbar, progress indicators |
| `junit:junit` 4.13.2 | Unit tests |

View Binding is turned on in `app/build.gradle.kts`. Exact versions are in `gradle/libs.versions.toml`.

All application code is Java. AGP 9 includes Kotlin support by default, and AndroidX libraries depend on `kotlin-stdlib` themselves, so that library ends up on the runtime classpath anyway. No Kotlin source or Kotlin-only framework is used.

## Security

- Passwords are never logged or stored. The mock compares them with `MessageDigest.isEqual` and keeps only a signed-in flag.
- Only the launcher activity is exported. The app requests no permissions.
- Backup and device transfer are turned off (`allowBackup="false"`, `fullBackupContent="false"`, and data extraction rules), although the app writes nothing to disk anyway.
- Error messages are fixed, friendly strings. Exception details never reach the UI.

Known limitations: the session is just an in-memory flag, and the demo credentials ship inside the APK. That's fine for a mock and nothing like production authentication.

## Trade-offs

- **Mock data, no persistence.** The challenge has no backend, so repositories serve fixed in-memory data. The session and data reset when the process dies. Demo timestamps are generated relative to app start, so the 30-day view always has entries.
- **Separate Activities instead of fragments and the Navigation component.** There are three screens with simple forward and back navigation. Activities with explicit Intents keep that easy to follow.
- **Lightweight architecture.** A single module, no DI framework, no RxJava or coroutines. A hand-written composition root and an executor per ViewModel are enough at this size.
- **SDK 36.** API 37 was available, but targeting it means taking on new platform behavior changes. API 36 is a current, well-supported target.

For production, I would change the following:

- Replace the mock repositories with network-backed ones: real token-based auth, the session token in the Android Keystore, and server-side paging for statements.
- Hold amounts in minor units or in decimals that match the server's contract, and settle on the rounding rules with the backend.
- Add instrumentation and UI tests, set up a dependency vulnerability scanner in CI, and turn on R8 with keep rules.
- Consider `FLAG_SECURE` on financial screens and a session timeout.
- Encryption of sensitive data.
- Check for network certificate, checks for rooted devices etc

Technical decision that i thought of but did not implement:
- Some Dependency Injection Frameworks
- Login session with simple encrypted persistence storage
- Use of Kotlin, Compose, KMM






