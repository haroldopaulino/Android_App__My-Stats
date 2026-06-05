# My Stats

Android project with separate phone and wearable modules for validating collected device statistics before running headless collection.

## Modules

- `core` — shared collector, Room database, uploader, boot/shutdown receivers, service, utilities, and models.
- `phone` — phone validation UI using Jetpack Compose and Material 3.
- `wearable` — wearable validation UI using Wear Compose with compact cards designed for small/round screens.

## Packages

- Core: `com.harold.my_stats`
- Phone app: `com.harold.my_stats.phone`
- Wearable app: `com.harold.my_stats.wearable`

## App Name

My Stats


## App Icon
Both the phone and wearable modules use the new My Stats launcher icon, designed around device metrics, CPU activity, battery/health analytics, and dashboard-style monitoring.


Build updates applied:
- Android Gradle Plugin 9.2.1
- Gradle wrapper 9.5.1
- Kotlin 2.3.21
- KSP 2.3.7 for Room annotation processing
- compileSdk/targetSdk 36
- Compose BOM 2026.05.01
- Room 2.8.4
- Activity Compose 1.13.0
- Lifecycle 2.9.3
- Coroutines 1.11.0
- Kotlin Serialization JSON 1.11.0
- Wear Compose 1.6.1
