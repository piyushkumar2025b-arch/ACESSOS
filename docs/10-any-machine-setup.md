# 10 — Any-Machine Setup (Windows / macOS / Linux)

Recreate this whole lab on any PC. Keeps everything off the tiny C: drive.

## 0. Prerequisites (all OSes)
- **Git** + **Java 17** — `java -version` → 17.x (JDK 21 breaks this build)
- Disk with ~5 GB free (called `<DISK>` below; examples use `E:`)

## 1. Android SDK (CLI only — no Android Studio)
1. Download **commandlinetools** for your OS from
   [developer.android.com/studio#command-line-tools-only](https://developer.android.com/studio#command-line-tools-only).
2. Extract so this exists: `<DISK>/android/sdk/cmdline-tools/latest/…`
3. Env vars (Windows: *System Properties → Environment Variables*; macOS/Linux:
   `~/.zshrc` / `~/.bashrc`):
   - `ANDROID_HOME` = `ANDROID_SDK_ROOT` = `<DISK>/android/sdk`
   - `GRADLE_USER_HOME` = `<DISK>/android/gradle-home`
   - `TEMP` = `TMP` = `<DISK>/android/temp` (Windows)
4. Install + license:
   ```bash
   sdkmanager --licenses                      # accept all (y)
   sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"
   ```

## 2. Build the app
```bash
git clone https://github.com/piyushkumar2025b-arch/ACESSOS
cd ACESSOS
# Windows PowerShell:
$env:ANDROID_SDK_ROOT='E:\android\sdk'; $env:GRADLE_USER_HOME='E:\android\gradle-home'
.\gradlew assembleDebug            # cmd/bat: gradlew.bat assembleDebug
# macOS/Linux:
./gradlew assembleDebug
```
APK → `app/build/outputs/apk/debug/app-debug.apk`.
First `debug.keystore`? Run once (README §3):
`keytool -genkey -v -keystore debug.keystore -alias androiddebugkey -keyalg RSA -keysize 2048 -validity 10000 -storepass android -keypass android -dname "CN=Android Debug,O=Android,C=US"`

## 3. Flash the ESP32 (any OS)
Arduino IDE 2.x → Espressif board URL (guide 04) → install `esp32` +
`Adafruit SSD1306`/`GFX` → Board **ESP32 Dev Module** → open
`hardware/firmware/AccessOs_ESP32_Firmware.ino` → Upload (hold BOOT if stuck).
macOS: `/dev/cu.SLAB_USBtoUART` or `/dev/cu.usbserial-*` · Linux: add yourself
to `dialout` (`sudo usermod -aG dialout $USER`, re-login) · Windows: CP2102/CH340 driver.

## 4. Install on phone
`adb install -r app/build/outputs/apk/debug/app-debug.apk` or copy the APK
over and allow "unknown sources" (guide 05).

**Checkpoint:** `./gradlew assembleDebug` green + firmware advertising →
you're fully set up → guide 06 (pair) then 08 (test).
