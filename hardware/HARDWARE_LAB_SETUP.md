# AccessOS Hardware Lab — setup on ANY machine + testing

This guide gets **any Windows PC** from zero to "phone talks to ESP32" in
~30 minutes. It mirrors the exact setup already proven on the maintainer PC
(E-drive CLI tools, API 36, Java 17).

## A. Any-machine app setup (5 min, no Android Studio)

1. Install **Git** + **Java 17** (Temurin/Adoptium zip or system JDK —
   `java -version` must say 17.x, NOT 21)
2. Download Android **commandlinetools-win** (Google) → extract to
   `<YOUR_DISK>:\android\sdk\cmdline-tools\latest`
   (e.g. `E:\android\sdk\cmdline-tools\latest\bin\sdkmanager.bat` must exist)
3. Set env (user-level, restart terminal after):
   `ANDROID_HOME` + `ANDROID_SDK_ROOT` → `<DISK>:\android\sdk`
   `GRADLE_USER_HOME` → `<DISK>:\android\gradle-home`
   `TEMP`/`TMP` → `<DISK>:\android\temp` (keeps C: clean)
4. Install packages (accept all licenses):
   `sdkmanager --licenses`
   `sdkmanager "platform-tools" "platforms;android-36" "build-tools;36.0.0"`
5. Clone + build:
   `git clone https://github.com/piyushkumar2025b-arch/ACESSOS`
   `cd ACESSOS` → `./gradlew assembleDebug`
   APK → `app\build\outputs\apk\debug\app-debug.apk`
6. First build downloads ~1 GB (5–15 min); later builds ~1–2 min.
   Missing `debug.keystore`? Generate once with the `keytool -genkey …`
   line in the main README.

No Android Studio needed. Studio is only for `@Preview`/emulator.

## B. ESP32 bench setup (Arduino IDE, any machine)

1. Install **Arduino IDE 2.x** + **ESP32 board support** (Espressif URL in
   main README) + libraries `Adafruit SSD1306` + `Adafruit GFX`
2. Install your board's USB driver (CP2102 / CH340) if no COM port shows
3. Wire per `hardware/HARDWARE_SETUP.md`:
   OLED 3V3/GND/SDA-GPIO21/SCL-GPIO22 · Button GPIO27→GND ·
   LED+330Ω GPIO26→GND · Buzzer+ GPIO25, − GND
4. Board **ESP32 Dev Module**, correct **Port**, open
   `hardware/firmware/AccessOs_ESP32_Firmware.ino` → Upload
   (hold BOOT if stuck at `Connecting…`)
5. Serial Monitor @115200 → `AccessOS BLE Advertising started.`
   OLED: `STATUS: READY / BLE: Waiting for Phone`, LED pulses ~1.5 s.

BLE identity (must match app): name `AccessOS-ESP32`,
service `4fafc201-1fb5-459e-8fcc-c5c9c331914b`,
notify `beb5483e-…`, write `beb5483f-…`.

## C. Hardware testing checklist (phone + ESP32)

- [ ] Phone: BT + Location ON, Nearby-devices permission granted, within 2 m
- [ ] Unlock app (demo card tap OK) → **Smart Safety Device → Scan & Connect**
- [ ] Card turns **Hardware Linked** (green); OLED → `STATUS: LINKED`
- [ ] **Test Ping** → buzzer chirps + LED flashes
- [ ] Press **physical button** → LED strobes + buzzer pulses → phone vibrates
      SOS → GPS captured → Firestore logged → SOS screen → ACK sent →
      OLED `✅ PHONE RECEIVED!`
- [ ] No ESP32? **Test SOS** button runs the full pipeline simulated
- [ ] Disconnect → OLED back to `READY`, app card to offline

## D. Diagnose fast

| Sees … | Means | Do |
|---|---|---|
| OLED dark | power/I2C wrong | check 3V3 vs 5V, SDA/SCL swap, addr 0x3C/0x3D |
| No `AccessOS-ESP32` in scan | BT/Location off, too far, firmware not advertising | re-flash, Serial check, 2 m range |
| `platforms;android-36` missing (CI) | stale cmdline-tools | workflow re-installs latest automatically |
| `debug.keystore not found` | first local build | `keytool -genkey …` once (README §3) |
| GPS empty | indoors / permission | outdoors + grant FINE location |
