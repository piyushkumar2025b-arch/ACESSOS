# 05 — Installing the App (APK vs Build It Yourself)

## Option A: Install the prebuilt APK (fastest, non-dev)
1. On the repo: *Releases → latest → app-debug.apk* (or grab
   `app/build/outputs/apk/debug/app-debug.apk` from someone who built it).
2. Copy the APK to your Android phone (USB, cloud link, or QR).
3. On the phone: *Settings → Security → Install unknown apps* → allow your
   file manager/browser → tap the APK → Install.
4. First launch: accept **Nearby devices**, **Location**, **Notifications**
   prompts. NFC prompt only if your phone has NFC.

No Google Play account, no root, no Android Studio needed.

## Option B: Build it yourself (developers — any machine)
Requirements: **Git + Java 17** (`java -version` must show 17, NOT 21) +
Android SDK **API 36**. Full zero-to-build walkthrough for Windows/macOS/Linux
is in `docs/10-any-machine-setup.md` (CLI tools, env vars, licenses).

```powershell
git clone https://github.com/piyushkumar2025b-arch/ACESSOS
cd ACESSOS
# Windows example (see guide 10 for macOS/Linux):
$env:ANDROID_SDK_ROOT='E:\android\sdk'; $env:GRADLE_USER_HOME='E:\android\gradle-home'
.\gradlew assembleDebug
```
APK lands at `app\build\outputs\apk\debug\app-debug.apk`.

First-ever build downloads ~1 GB and takes 5–15 min; later builds 1–2 min.
`debug.keystore not found`? Generate once with the `keytool -genkey …` line in
the root `README.md` §3, then rebuild.

## Install your fresh build
```powershell
& "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe" install -r app\build\outputs\apk\debug\app-debug.apk
```
(or copy the APK to the phone like Option A).

**Checkpoint:** app opens → home shows hero card + Emergency/Daily Care
sections → go to 06 and pair the hardware.
