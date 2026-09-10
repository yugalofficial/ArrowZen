# Building ArrowZen

## Requirements

- JDK 17
- Internet access to `google()` and `mavenCentral()` (for Gradle/Android dependencies)
- Either: Android Studio (Ladybug or newer), **or** just a terminal + the committed Gradle wrapper — Android Studio is not required to build this project.

## Option A: Build via GitHub Actions (recommended, no local setup)

See [GITHUB_ACTIONS.md](GITHUB_ACTIONS.md) for the full step-by-step walkthrough:
push this repo to GitHub, the workflow builds automatically, and you download
the APK/AAB from the workflow run's **Artifacts** section. No Android Studio,
no local JDK, no local Gradle install needed on your machine.

## Option B: Build locally from a terminal

```bash
# Debug APK (installable immediately, not for Play Store)
./gradlew assembleDebug

# Run all unit tests
./gradlew test

# Release AAB (Play Store bundle format — requires signing, see RELEASE.md)
./gradlew bundleRelease

# Release APK (a direct-install file, sideloadable — requires signing, see RELEASE.md)
./gradlew assembleRelease
```

On Windows, use `gradlew.bat` instead of `./gradlew`.

### Output locations

| Artifact | Path |
|---|---|
| Debug APK | `app/build/outputs/apk/debug/app-debug.apk` |
| Release APK | `app/build/outputs/apk/release/app-release.apk` |
| Release AAB (Play Store bundle) | `app/build/outputs/bundle/release/app-release.aab` |

## Option C: Open in Android Studio

1. Clone the repository.
2. Open the root folder in Android Studio.
3. Let Gradle sync (uses the committed wrapper — no separate Gradle install needed).
4. Run the `app` configuration on an emulator or a physical device (Android 8.0 / API 26 or newer).

## Debug vs Release builds

- **Debug** (`assembleDebug`): unsigned-for-distribution (uses Android's
  auto-generated debug keystore), installable directly for testing,
  `applicationId` suffixed with `.debug` so it can coexist on a device
  alongside a release install. Never upload this to the Play Store.
- **Release** (`assembleRelease` / `bundleRelease`): requires a real signing
  key. Without one configured, the release build will still succeed as an
  **unsigned** artifact if you're only running `assembleRelease` for
  inspection, but `bundleRelease` needs signing to produce something the
  Play Store will accept — see [RELEASE.md](RELEASE.md).

## Changing the package name

Edit `ARROWZEN_PACKAGE_NAME` in `gradle.properties` (default:
`com.yugalify.arrowzen`) before publishing your own fork under a different
identity. This one property drives both `namespace` and `applicationId`.

## Verifying the build (what CI actually runs)

```bash
./gradlew clean
./gradlew test
./gradlew assembleDebug
```

All three must succeed before a change is considered safe to merge — this is
exactly what `.github/workflows/android-build.yml` runs on every push.
