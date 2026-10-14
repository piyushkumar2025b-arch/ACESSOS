<div align="center">
<img width="1200" height="475" alt="GHBanner" src="https://ai.google.dev/static/site-assets/images/share-ais-513315318.png" />
</div>

# AccessOS — Smart Eldercare & SOS App (+ ESP32 Hardware)

AccessOS is a native Android (Kotlin + Jetpack Compose) eldercare app with a
professional "Midnight Care" dark UI, offline password vault, medicine reminders,
family calling, GPS SOS alerts — and a real ESP32 BLE SOS button with OLED,
LED and buzzer feedback.

- App: `app/` (package `com.aistudio.accessos.kdwbrr`)
- Firmware: `hardware/firmware/AccessOs_ESP32_Firmware.ino`
- Wiring guide: `hardware/HARDWARE_SETUP.md`
- CI: `.github/workflows/build-apk.yml` builds the APK on every push to `main`

---

## 1. What you need (software)

### A. To run the app (pick ONE)

**Option 1 — Just install the APK (easiest, no tools needed)**
- An Android phone (Android 7.0 / API 24 or newer, Bluetooth + GPS on)
- The APK from GitHub: repo → **Actions** → latest run → artifact
  `AccessOS-Hardware-Debug-APK` → `app-debug.apk`
- On the phone: allow *Install unknown apps* → install the APK

**Option 2 — Full CLI build on your PC (no Android Studio, ~2–3 GB)**
- Windows 10/11 (these steps use `E:\android` so C: stays untouched)
- Java 17 — portable zip (Temurin 17) **or** the JDK already on your PC
  (`java -version` → 17.x). Do NOT use Java 21 for local builds.
- Android SDK command-line tools + packages:
  - `platform-tools`, `platforms;android-36`, `build-tools;36.0.0`
- Gradle is auto-downloaded by the wrapper (`gradle-9.3.1`, in `GRADLE_USER_HOME`)
- Git (to clone/push)

**Option 3 — Android Studio (only needed for UI preview / emulator)**
- Android Studio Ladybug or newer (Koala+ recommended for API 36)
- During setup install: JDK 17, *Android SDK Platform 36*, *Build-tools 36*
- Open this folder as a project → open any `*Screen.kt` → **Split/Design** for
  `@Preview` rendering, or run on emulator / USB device

> You do **not** need Android Studio to build the APK — GitHub Actions does
> that in the cloud, and Option 2 builds it locally.

### B. To flash the ESP32 hardware (only if using the physical SOS button)

- **Arduino IDE 2.x** — https://www.arduino.cc/ (Windows installer)
- **ESP32 board support** (Espressif): in Arduino IDE → *Settings* →
  *Additional Board Manager URLs* add
  `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`
  → *Tools → Board → Boards Manager* → search `esp32` → Install
- **Arduino libraries** (*Tools → Manage Libraries*): `Adafruit SSD1306` +
  `Adafruit GFX Library` (a 1.3" SH1106 OLED also works with `Adafruit SH110X`)
- **USB serial driver** for your ESP32 board (CP2102 or CH340 — check your board;
  install from Silicon Labs / WCH site if no COM port appears)
- **Your kit**: ESP32 38-pin board, 1.3" I2C OLED, 4-pin push button,
  5mm red LED + 330 Ω resistor, 5V buzzer, breadboard + jumper wires
  (full wiring table in `hardware/HARDWARE_SETUP.md`)

### C. Accounts / keys (optional but recommended)

| Key | Where | What breaks without it |
|---|---|---|
| `GEMINI_API_KEY` in `.env` (see `.env.example`) | Google AI Studio | AI chat/help answers fall back to offline text |
| Firebase project (`app/google-services.json` — already included) | Firebase console | Push notifications + Firestore family sync won't reach the cloud (local Room DB still works) |

---

## 2. Connect the ESP32 hardware to the app (step by step)

### Step 1 — Wire the breadboard (5 minutes)

| Component | Pin | → ESP32 |
|---|---|---|
| OLED `VCC` / `GND` / `SDA` / `SCL` | 4 wires | `3V3` / `GND` / `GPIO 21` / `GPIO 22` |
| Push button leg A → `GPIO 27`, diagonal leg → `GND` | 2 wires | internal pull-up, press = LOW |
| Red LED anode (+) via 330 Ω → `GPIO 26`, cathode (−) → `GND` | — | — |
| Buzzer `+` → `GPIO 25`, `−` → `GND` | 2 wires | — |

Photos/diagrams: `hardware/HARDWARE_SETUP.md`. Double-check OLED voltage label
(3.3V vs 5V) before powering.

### Step 2 — Flash the firmware (Arduino IDE)

1. Connect ESP32 via micro-USB, note the COM port (*Tools → Port*)
2. Board: *Tools → Board → ESP32 Arduino → **ESP32 Dev Module***
3. Open `hardware/firmware/AccessOs_ESP32_Firmware.ino` → **Upload**
   (hold `BOOT` on the board if it sticks at `Connecting…`)
4. Open Serial Monitor (115200 baud) — you should see
   `AccessOS BLE Advertising started. Ready to connect!`
5. The OLED shows `STATUS: READY / BLE: Waiting for Phone` and the LED gives a
   short pulse every ~1.5 s. If the OLED stays dark, retry address `0x3D`
   (already handled in firmware) or swap SDA/SCL.

The ESP32 advertises as **`AccessOS-ESP32`** with service
`4fafc201-1fb5-459e-8fcc-c5c9c331914b`
(event notify `beb5483e-…`, command/ACK write `beb5483f-…`).
These UUIDs are hardcoded identically in `AccessOsBleManager.kt` — do not change
one side without the other.

### Step 3 — Pair from the app

1. Install & open AccessOS → unlock (demo card tap works without NFC hardware)
2. Phone: turn **Bluetooth + Location ON** (Android requires Location for BLE scan)
3. Grant **Nearby devices / Bluetooth** permission when asked
4. Home → **Smart Safety Device** card → **Scan & Connect**
5. Pick `AccessOS-ESP32` → card turns green **"Hardware Linked"**,
   OLED switches to `STATUS: LINKED / BLE: Phone Connected`
6. Tap **Test Ping** → buzzer chirps + LED flashes = link verified
7. Press the **physical button** → LED strobes, buzzer pulses, phone vibrates SOS,
   GPS is captured, Firestore logs, SOS screen opens, phone sends ACK →
   OLED shows `✅ PHONE RECEIVED!`
8. No hardware handy? Use **Test SOS** (simulated) — full app pipeline, no ESP32

### Step 4 — NFC care-card unlock (optional)

- Real card: any NFC tag — hold to phone back; UID is validated by
  `SessionUseCases.validateAndUnlockWithCard`
- No tag: Lock screen → **DEMO CARD TAP (SIMULATE)**

---

## 3. Build the APK yourself

### Cloud build (automatic)
Push to `main` (or tag `v*`) → **Actions** → download the APK artifact.
No local tools needed.

### Local CLI build (E-drive, C: untouched)

```powershell
# one-time setup (already done on this PC)
mkdir E:\android\sdk, E:\android\gradle-home, E:\android\temp -Force
setx ANDROID_HOME "E:\android\sdk"
setx ANDROID_SDK_ROOT "E:\android\sdk"
setx GRADLE_USER_HOME "E:\android\gradle-home"
setx TEMP "E:\android\temp"
setx TMP "E:\android\temp"
# + extract cmdline-tools to E:\android\sdk\cmdline-tools\latest
# + sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"

# every build (fresh terminal picks up setx automatically)
cd 'E:\remix-remix-accessos (1)'
.\gradlew assembleDebug
# APK → app\build\outputs\apk\debug\app-debug.apk  (~26 MB)
```

First build downloads ~1 GB of Gradle/Maven deps (5–15 min); later builds ~1–2 min.
If `debug.keystore` is missing it is auto-created by CI; locally generate once:
`keytool -genkey -v -keystore debug.keystore -alias androiddebugkey -keyalg RSA
-keysize 2048 -validity 10000 -storepass android -keypass android
-dname "CN=Android Debug,O=Android,C=US"`

---

## 4. Troubleshooting

| Symptom | Fix |
|---|---|
| BLE device not found in scan | ESP32 powered? OLED shows READY? Phone BT+Location ON? Move within 2 m. `Test SOS` still works without hardware |
| `platforms;android-36` not found (CI) | Old cmdline-tools — workflow now re-installs latest (11076708) automatically |
| `Keystore file debug.keystore not found` | Generate with the `keytool` line above (CI does it for you) |
| OLED dark after flash | Try the other I2C address (`0x3D`), check `3V3` vs `5V` label, swap SDA/SCL |
| Upload stuck at `Connecting…` | Hold `BOOT` during upload; install CP2102/CH340 driver; try another cable/port |
| No COM port in Arduino IDE | Install USB-serial driver for your board; try a data cable (not charge-only) |
| GPS empty in SOS | Enable Location + go outdoors; address falls back to lat/long |
| WhatsApp share fails | Install WhatsApp; number must be in international format |
| Build slow / C: full | Keep `GRADLE_USER_HOME` + `TEMP` on E: (see above); never install SDK on C: |

---

## 5. Project map

```
app/                  Android app (Compose UI, Room, Firebase, BLE)
hardware/firmware/    ESP32 .ino (button GPIO27, LED GPIO26, buzzer GPIO25, OLED 21/22)
hardware/HARDWARE_SETUP.md   wiring + flashing + testing walkthrough
.github/workflows/    CI that builds + releases the APK
gradle/               version catalog (AGP, Kotlin, Compose BOM, Firebase BOM)
```

Minimum phone: Android 7 (API 24). Target/compile: API 36. Language: Kotlin 2.2.

