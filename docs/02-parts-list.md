# 02 — Parts & Shopping List (Exact Kit)

Buy exactly this — nothing more needed for the full build.

## Microcontroller & display
- **ESP32 Dev Board, 38-pin** (CP2102 or CH340 USB chip) ×1 — BLE server +
  GPIO controller. Note which USB chip yours has; you need its driver (guide 04).
- **1.3" I2C OLED, 128×64** (SSD1306 or SH1106) ×1 — status screen.
  Check its voltage label: most take 3.3V (`3V3`), some 5V (`VIN`).

## Inputs & outputs
- **4-pin tactile push button** ×1 — the physical SOS trigger (GPIO 27, active LOW).
- **5mm red LED** ×1 + **330 Ω resistor** ×1 — alarm/status flasher (GPIO 26).
  No resistor = burnt LED. Orange-orange-brown bands = 330 Ω.
- **5V active buzzer** ×1 — beeps/chimes (GPIO 25). Active (built-in oscillator)
  is easier than passive.

## Prototyping
- **GL12 breadboard, 840 points** ×1 — ESP32 straddles the center divide.
- **Jumper wires**: ~10× male-to-female (OLED to board) + ~8× male-to-male
  (rails). Get a mixed pack; extras never hurt.
- **Micro-USB data cable** ×1 — must carry data, not charge-only (test: phone
  shows file transfer on PC with it).

## Phone + PC
- **Android phone, v7.0+ (API 24+)**, with Bluetooth + GPS + NFC (NFC only for
  real card unlock; everything else works without it).
- **Windows 10/11 PC** with ~3 GB free (build tools live on any drive — guide 10).

## Software (free, guide 04/05/10 cover install)
Arduino IDE 2.x · ESP32 board support (Espressif) · Adafruit SSD1306 + GFX
libraries · Java 17 · Android SDK API 36 · Git.

**Checkpoint:** all hardware on your desk + Arduino IDE installed → go to 03.
