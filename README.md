# My Stats Android Telemetry Collector

**Multi-module Android telemetry collection app built with Kotlin, Jetpack Compose, Wear Compose, Room, foreground/background services, boot/shutdown receivers, local persistence, and endpoint upload support for validating phone and wearable device statistics before headless collection.**

My Stats is a modern Android project designed to collect, store, display, and upload device statistics from Android phones and Wear OS devices. The project separates shared telemetry logic into a reusable `core` module while providing dedicated validation UIs for phone and wearable form factors.

This repository demonstrates practical Android engineering around background collection, local data persistence, endpoint synchronization, Compose UI, wearable UI design, modern Gradle/Kotlin tooling, and mobile telemetry architecture.

---

## Why This Project Matters

My Stats is more than a simple Android sample app. It is built around a real engineering workflow: collect device metrics, persist them locally, validate what was collected in a UI, upload records to an endpoint, and prepare the same collection logic for headless/background use.

The project demonstrates:

- Kotlin Android development
- Multi-module Android architecture
- Shared core library design
- Jetpack Compose phone UI
- Wear Compose wearable UI
- Room database persistence
- Coroutine-based asynchronous work
- Kotlin Serialization JSON support
- Foreground/background collection service structure
- Boot and shutdown receiver support
- Endpoint upload architecture
- Phone and wearable validation workflows
- Modern Android Gradle Plugin, Kotlin, KSP, Compose, and Room setup

---

## Project Overview

The app is designed to validate device-statistics collection before running the same logic in a headless/background mode.

The project includes three main modules:

| Module | Purpose |
|---|---|
| `core` | Shared collector, Room database, uploader, receivers, service, utilities, and models |
| `phone` | Android phone validation UI built with Jetpack Compose and Material 3 |
| `wearable` | Wear OS validation UI built with Wear Compose and compact cards for small/round screens |

The repository README identifies these three modules and describes `core` as the shared location for the collector, database, uploader, boot/shutdown receivers, service, utilities, and models. It also identifies the `phone` module as the Compose/Material 3 phone UI and the `wearable` module as the Wear Compose UI for small and round screens.

---

## Main Features

### Shared Telemetry Collection Core

The `core` module centralizes the reusable data-collection logic so both phone and wearable modules can share the same underlying collection, persistence, upload, and model behavior.

This is a strong architecture choice because it avoids duplicating telemetry logic across device types.

### Phone Validation UI

The `phone` module provides a full Android validation interface using Jetpack Compose and Material 3.

The phone UI is designed to help confirm that device statistics are being collected, stored, displayed, uploaded, and cleared correctly before moving the collection system toward background/headless operation.

### Wearable Validation UI

The `wearable` module provides a Wear OS interface using Wear Compose. The UI is designed around compact cards that fit smaller wearable screens and round-screen constraints.

This demonstrates experience with more than one Android form factor.

### Room Local Database

The project uses Room for local persistence. Collected telemetry can be stored locally before being uploaded to an endpoint.

This pattern is useful for telemetry and diagnostics apps because network availability may be intermittent, and collected data should not be lost before upload.

### Endpoint Upload Support

The shared core includes uploader support so locally collected records can be sent to a backend endpoint.

This demonstrates Android client-to-server integration and a practical data pipeline:

```text
Collect metrics → Store locally → Display/validate → Upload to endpoint → Clear synced local data
```

### Foreground and Background Collection Structure

The core module includes service support, which makes the project suitable for validating collection behavior before moving toward background/headless collection.

### Boot and Shutdown Receivers

The shared core includes boot and shutdown receiver support. This is important for telemetry and diagnostics use cases where collection may need to resume after reboot or capture lifecycle-related device events.

### Modern Android Build Stack

The repository README lists recent build tooling and dependency versions, including Android Gradle Plugin 9.2.1, Gradle wrapper 9.5.1, Kotlin 2.3.21, KSP for Room annotation processing, compile/target SDK 36, Compose BOM 2026.05.01, Room 2.8.4, Activity Compose 1.13.0, Lifecycle 2.9.3, Coroutines 1.11.0, Kotlin Serialization JSON 1.11.0, and Wear Compose 1.6.1.

---

## Technical Stack

| Area | Technology |
|---|---|
| Language | Kotlin |
| Phone UI | Jetpack Compose |
| Design system | Material 3 |
| Wearable UI | Wear Compose |
| Local persistence | Room |
| Annotation processing | KSP |
| Async work | Kotlin Coroutines |
| Serialization | Kotlin Serialization JSON |
| Architecture | Multi-module Android project |
| Modules | `core`, `phone`, `wearable` |
| Build system | Gradle Kotlin DSL |
| Target SDK | 36 |
| License | GPL-3.0 |

---

## Repository Structure

```text
Android_App__My-Stats/
├── core/
│   ├── src/main/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── phone/
│   ├── src/main/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── wearable/
│   ├── src/main/
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/wrapper/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── README.md
└── LICENSE
```

---

## Packages

The project uses separate package namespaces for the shared core and each app target:

| Module | Package |
|---|---|
| Core | `com.harold.my_stats` |
| Phone app | `com.harold.my_stats.phone` |
| Wearable app | `com.harold.my_stats.wearable` |

---

## Architecture

The project is organized around a clean separation between shared telemetry logic and platform-specific validation UIs.

```text
core
 ├── collection logic
 ├── Room database
 ├── upload logic
 ├── boot/shutdown receivers
 ├── services
 ├── utilities
 └── models

phone
 └── Jetpack Compose validation UI

wearable
 └── Wear Compose validation UI
```

This architecture allows the same telemetry pipeline to be used across multiple Android targets while keeping UI code isolated by form factor.

---

## Data Pipeline

The intended data flow is:

1. Collect device statistics.
2. Store records in the local Room database.
3. Display collected data in the validation UI.
4. Upload stored records to the configured endpoint.
5. Remove local records after successful endpoint sync.
6. Continue collecting based on the configured interval and service behavior.

This is the same pattern used in many production mobile telemetry, diagnostics, field-data, and IoT companion apps.

---

## Phone App

The phone app is designed for validation and control on a standard Android device.

It can be used to verify:

- Whether collection is active
- Whether data is being stored locally
- How much data is stored locally
- Whether records are being sent to the endpoint
- Whether failures are occurring
- Whether collection and upload behavior match expectations

The phone UI uses Jetpack Compose and Material 3, which demonstrates modern Android UI development.

---

## Wearable App

The wearable app provides a compact validation interface for Wear OS devices.

The wearable UI is designed with:

- Small-screen layout constraints
- Card-based display
- Round-screen readability
- Wear Compose components

This makes the project relevant to wearable, health, fitness, IoT, and device-monitoring use cases.

---

## Skills Demonstrated

This repository demonstrates several Android and mobile engineering skills:

- Kotlin Android development
- Jetpack Compose UI development
- Material 3 interface implementation
- Wear Compose development
- Multi-module Gradle architecture
- Shared core module design
- Room database persistence
- KSP annotation processing
- Coroutine-based asynchronous logic
- Kotlin Serialization usage
- Endpoint upload workflows
- Foreground/background service architecture
- Android boot/shutdown receiver integration
- Local telemetry validation
- Mobile diagnostics workflow design
- Phone and wearable product thinking
- Modern Android build configuration

---

## Embedded, IoT, and Firmware Relevance

Although this is an Android project, it is highly relevant to embedded, firmware, and connected-device workflows.

Mobile and wearable apps are often used to:

- Collect field diagnostics
- Validate device health
- Monitor battery, memory, CPU, and network behavior
- Upload logs or telemetry to backend services
- Support QA and engineering validation
- Run companion workflows for embedded devices
- Confirm reliability before headless/background operation

My Stats demonstrates these same engineering patterns through a phone/wearable telemetry collection system.

---

## How to Build

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync using the included wrapper.
4. Select the `phone` app target to run the phone validation UI.
5. Select the `wearable` app target to run the Wear OS validation UI.
6. Configure the endpoint if testing upload behavior.
7. Build and run on a physical Android phone, emulator, Wear OS device, or Wear OS emulator.

---

## App Identity

| Item | Value |
|---|---|
| App name | My Stats |
| Phone package | `com.harold.my_stats.phone` |
| Wearable package | `com.harold.my_stats.wearable` |
| Core package | `com.harold.my_stats` |

The phone and wearable modules use the My Stats launcher icon, which is designed around device metrics, CPU activity, battery/health analytics, and dashboard-style monitoring.

---

## Owner

by Harold Paulino

---

## License

This project is licensed under the GPL-3.0 license.
