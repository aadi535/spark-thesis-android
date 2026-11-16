# Spark Thesis Android

Spark is an Android micro-learning app. When I open a monitored app such as YouTube, Instagram or TikTok, Spark can interrupt the session and offer a short lesson. I can explore the idea, leave, or use a focus pass when one is available.

The app also includes AI question suggestions, a learning journal and Mind Palace, a five-question Knowledge Check based on lessons I chose to explore, and an XP leaderboard. The Profile screen supports custom display names and a manual leaderboard refresh.

## Run the app

1. Open this repository in Android Studio and install Android SDK 36. The app supports Android 8.0 (API 26) and later.
2. Register Android package `com.spark` in your own Firebase project. For Google sign-in, enable the Google provider, add your build signing certificate's SHA fingerprints and download your `google-services.json` into `app/`. Use the corresponding web OAuth client ID for `GOOGLE_CLIENT_ID` in the backend.
3. Start the [Spark backend](https://github.com/aadi535/spark-thesis-backend). The default URL is `http://10.0.2.2:8080/` for the Android emulator. For a USB-connected phone, run `adb reverse tcp:8080 tcp:8080` and set `sparkBackendUrl=http://localhost:8080/` in your local Gradle user properties. For your own hosted backend, set `sparkBackendUrl` to its HTTPS URL with a trailing slash.
4. Select the device and run the app. Follow onboarding to grant Usage Access and overlay permission, then enable a supported app in Focus Guard. Home includes a simulation for trying the interruption flow.

Firebase client configuration belongs in the local checkout. Guest mode can show local lessons once the app is built and configured. Account features need a running backend. To try Knowledge Check, sign in and explore five different ideas via the interruption flow, then let your history sync.

Build and run JVM tests on Windows with `gradlew.bat :app:assembleDebug :app:testDebugUnitTest`.

## Review version

This snapshot comes from commit `4c459fb7ed6c84c969d5ec0f31bba91023cf3e16` on `thesis-hardening` in the original private repository. I keep the original repository private because older revisions contain credentials and development configuration. This public review copy has a fresh Git history and uses local configuration for the reviewer’s services. The development history page includes screenshots from the original GitHub repository.

- [Architecture](docs/architecture.md)
- [Development history and GitHub screenshots](docs/development-history.md#github-screenshots)
