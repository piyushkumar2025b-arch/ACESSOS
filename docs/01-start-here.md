# 01 — AccessOS Start Here (Overview & Map)

Welcome! AccessOS = Android eldercare app + ESP32 BLE SOS button.
Read these 10 guides in order — each is short and ends with a checkpoint.

| # | Guide | What you get |
|---|---|---|
| 01 | Start Here (this file) | map + 30-min path |
| 02 | Parts & Shopping List | exact kit to buy |
| 03 | Wiring the Breadboard | pins + rails, no soldering |
| 04 | Flashing the ESP32 Firmware | Arduino IDE → OLED says READY |
| 05 | Installing the App | APK vs build-it-yourself |
| 06 | Pairing Phone ↔ ESP32 over BLE | Scan & Connect → Hardware Linked |
| 07 | Data, Cloud Sync & NFC Cards | Firebase, vault, care-card unlock |
| 08 | Testing Playbook | tick-box proof everything works |
| 09 | Troubleshooting Field Manual | fix any failure in minutes |
| 10 | Any-Machine Setup Scripts | replicate this lab on any PC |

**Fastest path (30 min):** 02 → 03 → 04 → 05 (APK option) → 06 → 08.
Developers add 07 + 10. Stuck anywhere → jump to 09.

**One-line architecture:**
`Button(GPIO27) → ESP32 BLE → Phone(BLE notify) → GPS+FCM+Firestore → Family
→ ACK → OLED/LED/Buzzer`. Passwords stay offline in Room; everything else syncs.
