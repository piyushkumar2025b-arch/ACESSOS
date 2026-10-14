# 08 — Testing Playbook (Prove Everything Works)

Run in order; tick each box. Total ~15 min. Works with real hardware — and
every hardware test has a no-hardware fallback.

## A. Flash sanity (2 min)
- [ ] OLED shows `STATUS: READY / BLE: Waiting for Phone`
- [ ] LED pulses every ~1.5 s · Serial @115200 shows `AccessOS BLE Advertising started.`

## B. Pairing (2 min)
- [ ] App **Scan & Connect** finds `AccessOS-ESP32`
- [ ] Card → **Hardware Linked** · OLED → `STATUS: LINKED`
- [ ] **Test Ping** → buzzer chirp + LED flash · Device screen shows
      `🟢 Hardware Ping OK`
- [ ] Disconnect → OLED back to `READY`, card back to offline

## C. Physical SOS end-to-end (5 min) — the core demo
- [ ] Re-pair, then **press the physical button** once
- [ ] Phone: vibration + SOS screen opens; GPS captured (test outdoors or by a
      window — grant Location)
- [ ] Event lands in **Firestore + family feed** with coordinates
      (skip if no Firebase — local path still runs)
- [ ] OLED: `SOS` alert → then `✅ PHONE RECEIVED!` after app sends `ACK`
- [ ] LED strobes + buzzer pulses during the window

## D. Simulated SOS (no hardware) (1 min)
- [ ] Device screen → **Test SOS** → full pipeline (vibration → SOS UI →
      GPS → log) runs with `AccessOS-ESP32 (Test Mode)`

## E. App features (5 min)
- [ ] Medicine: add dose → progress "X of Y" updates, tick logs taken
- [ ] Family: add member → list persists after restart (Room)
- [ ] Password: add entry → shows masked, decrypts on tap (vault = offline only)
- [ ] NFC: tap fingerprint chip → demo success pulse unlocks (or real tag, guide 07)
- [ ] Lock screen: status pill reflects live BLE state

**All boxes ticked → hardware-ready.** Any miss → guide 09.
