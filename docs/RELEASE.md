# Release Signing

Android requires every release build to be signed with the *same* key for
the lifetime of the app (Play Store enforces this — you cannot switch keys
later without losing update continuity). This document covers generating
that key once and wiring it into GitHub Actions so CI can produce signed
builds without the key ever being committed to the repository.

**Never commit a keystore file or its passwords to git.** The `.gitignore`
in this project already excludes `*.keystore`, `*.jks`, and
`keystore.properties` for exactly this reason.

## 1. Generate a keystore (one-time, do this locally)

Requires a JDK (the same one used to build Android apps) — `keytool` ships
with every JDK.

```bash
keytool -genkeypair -v \
  -keystore arrowzen-release.keystore \
  -alias arrowzen \
  -keyalg RSA -keysize 2048 -validity 10000
```

You'll be prompted for:
- A **keystore password** (protects the file itself)
- Your name/organization details (used in the certificate; not user-facing)
- A **key password** (protects this specific key inside the file — can be
  the same as the keystore password if you prefer one password to remember)

**Store `arrowzen-release.keystore` somewhere safe and back it up.** If you
lose it, you can never update your app on the Play Store again under the
same listing — Google cannot recover or reset this for you.

## 2. Add it to GitHub as encrypted secrets

1. Convert the keystore file to base64 so it can be stored as a text secret:

   ```bash
   base64 -w 0 arrowzen-release.keystore > keystore_base64.txt
   ```

   (On macOS, drop `-w 0`: `base64 arrowzen-release.keystore > keystore_base64.txt`)

2. On GitHub: your repository → **Settings** → **Secrets and variables** →
   **Actions** → **New repository secret**. Add these four, one at a time:

   | Secret name | Value |
   |---|---|
   | `KEYSTORE_BASE64` | the entire contents of `keystore_base64.txt` |
   | `KEYSTORE_PASSWORD` | the keystore password from step 1 |
   | `KEY_ALIAS` | `arrowzen` (or whatever you passed to `-alias`) |
   | `KEY_PASSWORD` | the key password from step 1 |

3. Delete `keystore_base64.txt` from your computer once it's pasted into
   GitHub — it's no longer needed locally and contains sensitive data in
   plain text.

## 3. Re-run the build

Once all four secrets are set, re-run the GitHub Actions workflow (Actions
tab → Android Build → Run workflow). The **Build Release AAB** job will now:

1. Decode the secret back into a keystore file (server-side, never exposed
   in logs).
2. Write a `keystore.properties` pointing at it (also server-side only).
3. Run `./gradlew bundleRelease`, producing a properly signed `.aab`.
4. Upload it as the **ArrowZen-Release-AAB** artifact.

## 4. Building a signed release locally instead (optional)

If you'd rather sign locally than via CI, create `keystore.properties` in
the project root (this file is gitignored — it will never be committed):

```properties
storeFile=/absolute/path/to/arrowzen-release.keystore
storePassword=your_keystore_password
keyAlias=arrowzen
keyPassword=your_key_password
```

Then:

```bash
./gradlew bundleRelease   # -> app/build/outputs/bundle/release/app-release.aab
./gradlew assembleRelease # -> app/build/outputs/apk/release/app-release.apk
```

`app/build.gradle.kts` automatically detects `keystore.properties` and
applies it to the `release` signing config — no other changes needed.

## 5. Version bumps for future updates

Every Play Store update needs a higher `versionCode`. Edit
`gradle.properties`:

```properties
ARROWZEN_VERSION_CODE=2
ARROWZEN_VERSION_NAME=1.0.1
```

`versionCode` must strictly increase on every submission; `versionName` is
the human-readable string shown to users and has no format requirement
beyond that.
