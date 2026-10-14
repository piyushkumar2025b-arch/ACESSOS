# 06 — Pairing the Phone ↔ ESP32 over BLE

No system Bluetooth-settings pairing needed — the app talks directly over
BLE GATT. Keep both devices within ~2 m.

## Steps
1. **Power the ESP32** (USB). OLED: `STATUS: READY / BLE: Waiting for Phone`.
2. On the phone: Bluetooth **ON** + Location **ON** (Android <12 needs
   Location for BLE scans; Android 12+ needs **Nearby devices** permission —
   the app prompts you automatically).
3. Open AccessOS → tap the hero/status card or **Smart Safety Device** →
   **Scan & Connect**.
4. The app scans for the advertisement named **`AccessOS-ESP32`**, connects,
   discovers service **`4fafc201-1fb5-459e-8fcc-c5c9c331914b`**, and subscribes
   to notify **`beb5483e-…`** / opens write **`beb5483f-…`**.
5. Success looks like:
   - App card → **Hardware Linked** (green) · ESP32 → `STATUS: LINKED`
   - OLED says `Connected to app` and the LED heartbeat switches to 3 s.

## What runs automatically after pairing
- Phone enables **auto button-triggered SOS** + a foreground service keeps the
  link alive (battery optimizations may need exempting — see guide 09).
- ESP32 sends `READY` / `DeviceReady` (firmware version shown in app) and
  `PING` acks; the app can **Test Ping** (buzzer chirp + LED flash) from the
  device screen.
- Pressing the **physical button** → `SOS:<eventId>` over notify → phone
  vibrates, opens the SOS flow, captures GPS, logs to Firestore, notifies
  family, then writes `ACK` back → OLED `✅ PHONE RECEIVED!`.

## Unpair
Device screen → **Disconnect** → OLED returns to `READY` and keeps advertising
for the next scan.

**Checkpoint:** card says Hardware Linked + Test Ping beeps → go to 08
(run the SOS test), or 07 first if you want cloud/NFC configured.
