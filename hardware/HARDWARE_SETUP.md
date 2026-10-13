# AccessOS ESP32 Smart Hardware Integration Guide

This guide walks you through connecting your hardware kit with the **AccessOS** Android application.

---

## 1. Bill of Materials (From Your Invoice)

| Item | Quantity | Purpose in AccessOS |
|---|---|---|
| **ESP32 Dev Board (38-pin)** | 1 | Microcontroller, BLE Server, GPIO Controller |
| **1.3" I2C OLED Display** | 1 | Visual feedback, BLE status, emergency confirmation |
| **4-Pin Push Button** | 1 | Physical Emergency SOS trigger button |
| **5mm Red LED** | 1 | Status indicator & alarm flasher |
| **330 Ω Resistor** | 1 | Current limiting resistor for Red LED |
| **5V to 12V Buzzer** | 1 | Audible feedback & emergency alarm tones |
| **GL12 Breadboard (840 points)** | 1 | Solderless prototyping assembly board |
| **Jumper Wires (M-F & F-F)** | As needed | Interconnecting modules and breadboard rails |

---

## 2. Complete Wiring Diagram & Pinout Table

### Pin Connection Table

| Component | Component Pin | ESP32 GPIO / Rail | Notes |
|---|---|---|---|
| **1.3" OLED** | `VCC` | `3.3V` (or `VIN` if 5V) | Check your OLED module's voltage label |
| **1.3" OLED** | `GND` | `GND` | Common ground rail |
| **1.3" OLED** | `SDA` | `GPIO 21` | Hardware I2C Data line |
| **1.3" OLED** | `SCL` | `GPIO 22` | Hardware I2C Clock line |
| **4-Pin Button** | Terminal 1 (Leg A) | `GPIO 27` | Configured with internal `INPUT_PULLUP` |
| **4-Pin Button** | Terminal 2 (Diagonal) | `GND` | Pressed connects GPIO 27 to GND (Active LOW) |
| **5mm Red LED** | Anode (+) Long leg | `GPIO 26` through `330 Ω` | Connect resistor between GPIO 26 and LED Anode |
| **5mm Red LED** | Cathode (-) Short leg | `GND` | Ground rail |
| **Buzzer** | Positive (+) Pin | `GPIO 25` | Signal drive pin (or via transistor) |
| **Buzzer** | Negative (-) Pin | `GND` | Ground rail |

---

## 3. Breadboard Assembly Steps

1. **Place the ESP32**: Position the 38-pin ESP32 across the center divide of your GL12 breadboard so pins on both sides have open tie points.
2. **Setup Ground Rail**: Connect one of the ESP32 `GND` pins to the blue/negative `-` power bus rail on the breadboard.
3. **Wire the Push Button**:
   - Insert the 4-pin push button across the center divider.
   - Connect one pin to **GPIO 27**.
   - Connect the diagonally opposite pin to the **GND** rail.
4. **Wire the Red LED**:
   - Insert the 330 Ω resistor between **GPIO 26** and an empty row.
   - Insert the **Anode** (longer leg) of the 5mm Red LED into the same row as the resistor.
   - Insert the **Cathode** (shorter leg / flat rim) into the **GND** rail.
5. **Wire the Buzzer**:
   - Connect the Positive `+` pin of the buzzer to **GPIO 25**.
   - Connect the Negative `-` pin to the **GND** rail.
6. **Wire the 1.3" OLED**:
   - Use Female-to-Male jumper wires to connect the OLED pins:
     - `VCC` -> `3V3` on ESP32
     - `GND` -> `GND` rail
     - `SDA` -> `GPIO 21`
     - `SCL` -> `GPIO 22`

---

## 4. Flashing the Firmware

1. **Install Arduino IDE**: Download from [arduino.cc](https://www.arduino.cc/) (v2.x recommended).
2. **Add ESP32 Board Support**:
   - Open Arduino IDE -> **Settings / Preferences**.
   - In *Additional Board Manager URLs*, add:
     `https://raw.githubusercontent.com/espressif/arduino-esp32/gh-pages/package_esp32_index.json`
   - Go to **Tools -> Board -> Boards Manager**, search for `esp32` by Espressif Systems, and click **Install**.
3. **Install Required Libraries**:
   - Go to **Tools -> Manage Libraries...**
   - Search and install:
     - `Adafruit SSD1306`
     - `Adafruit GFX Library`
4. **Open and Flash**:
   - Open `/hardware/firmware/AccessOs_ESP32_Firmware.ino`.
   - Select your board under **Tools -> Board -> ESP32 Arduino -> ESP32 Dev Module**.
   - Select your COM Port under **Tools -> Port**.
   - Click **Upload** (Hold the `BOOT` button on the ESP32 if the upload prompt displays `Connecting........_____`).

---

## 5. End-to-End Testing with the AccessOS App

1. **Power On**: Connect the ESP32 via micro-USB. The OLED will display:
   ```
   == AccessOS SOS ==
   STATUS: READY
   BLE: Waiting for Phone
   Hold button for SOS
   Phone Disconnected
   ```
2. **Open AccessOS on Android**:
   - On the Home Dashboard, look at the **AccessOS Smart Hardware** card.
   - Tap **"Scan & Connect"**.
   - The phone scans for BLE advertisements and connects to `AccessOS-ESP32`.
   - The card transitions to **"ESP32 Connected"** with a green badge.
   - The ESP32 OLED updates to:
     ```
     STATUS: LINKED
     BLE: Phone Connected
     Button Armed: OK
     Care Circle Active
     ```
3. **Test Hardware Ping**:
   - Tap **"Test Ping"** in the app.
   - The buzzer will beep and the OLED/LED confirm the communication link.
4. **Press the Physical Button**:
   - Press the physical 4-pin push button on the breadboard.
   - The ESP32 LED blinks rapidly, the buzzer pulses, and sends the event to the phone.
   - The phone receives the event, vibrates with an SOS rhythm, fetches real GPS coordinates, logs to Firestore, and opens the **Emergency (SOS)** screen.
   - The phone sends an **ACK** back to the ESP32.
   - The ESP32 OLED displays:
     ```
     ✅ PHONE RECEIVED!
     Cloud Alert Active
     Family Dispatched
     Care Circle Alerted
     ```
