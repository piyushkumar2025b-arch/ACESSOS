# 04 — Flashing the ESP32 Firmware

Goal: OLED shows `STATUS: READY / BLE: Waiting for Phone`, LED pulses ~1.5 s,
Serial says `AccessOS BLE Advertising started.`

## 1. Arduino IDE + ESP32 support (once per PC)
1. Install **Arduino IDE 2.x** from arduino.cc.
2. *File → Preferences → Additional Board Manager URLs*, add:
   `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`
3. *Tools → Board → Boards Manager* → search `esp32` (Espressif) → Install.
4. *Tools → Manage Libraries* → install **`Adafruit SSD1306`** +
   **`Adafruit GFX Library`** (SH1106 OLED? also add `Adafruit SH110X`).
5. No COM port? Install your board's USB driver: CP2102 (Silicon Labs) or
   CH340 (WCH). Retest with a data cable.

## 2. Flash
1. *Tools → Board → ESP32 Arduino → **ESP32 Dev Module***, select your **Port**.
2. Open `hardware/firmware/AccessOs_ESP32_Firmware.ino` → **Upload**.
   Stuck at `Connecting…_____`? Hold the board's **BOOT** button until upload starts.
3. *Tools → Serial Monitor*, 115200 baud → expect the advertising line above.

## 3. What the firmware does (so you trust it)
- Advertises **`AccessOS-ESP32`**, service `4fafc201-…`, event-notify
  `beb5483e-…`, command-write `beb5483f-…` (identical in `AccessOsBleManager.kt`).
- Button (GPIO 27, debounce 50 ms) → builds `SOS:<eventId>` → notifies phone.
- Phone's `ACK` on the command characteristic → OLED `✅ PHONE RECEIVED!` +
  chime; 8 s with no ACK → back to idle.
- OLED tries address `0x3C` then `0x3D`; LED heartbeat (1.5 s idle / 3 s linked /
  200 ms SOS); `PING` command → chirp + flash.
- On disconnect it restarts advertising automatically.

**Checkpoint:** OLED READY + Serial advertising line → go to 05 (app), then 06.
