# 09 — Troubleshooting Field Manual

Fastest fix first. Symptom → cause → do-this.

## Hardware / firmware
| Symptom | Likely cause | Fix |
|---|---|---|
| OLED dark | power/wiring | VCC→3V3 (or VIN if 5V OLED), GND to rail, reseat jumpers |
| OLED gibberish / nothing but backlight | wrong address or pins | firmware tries 0x3C then 0x3D; check SDA→21, SCL→22 swapped? |
| Upload stuck `Connecting…` | wrong mode/port | hold **BOOT** during upload; swap USB data cable |
| No COM port | driver/cheap cable | install CP2102 or CH340 driver; use a data cable |
| LED always on/off, button dead | wiring/polarity | button legs must be diagonal; LED long leg toward resistor |
| Buzzer silent | active vs passive | use **active** buzzer; check `+`→GPIO 25 |

## BLE pairing
| Symptom | Likely cause | Fix |
|---|---|---|
| Scan finds nothing | BT/Location off, far away | enable both, move within 2 m, re-flash & confirm Serial advertising |
| Connects then drops | interference/battery saver | unpair, forget old bond in system BT, disable battery optimization for app |
| `Hardware Linked` but no SOS events | permission revoked | re-grant Nearby devices/Location; re-tap Scan & Connect |
| Works once, not after reboot | service not restarted | open app once after boot; check foreground service notification present |
| ACK never shown on OLED | app not in foreground | keep app open or allow background; OLED auto-recovers after 8 s |

## App / build
| Symptom | Likely cause | Fix |
|---|---|---|
| `debug.keystore not found` | fresh clone | run the `keytool -genkey …` line from README §3 once, rebuild |
| `Failed to find target with hash android-36` | SDK missing | `sdkmanager "platforms;android-36" "build-tools;36.0.0"` (guide 10) |
| Gradle wants wrong Java | JDK 21 on PATH | install **Java 17**, set `JAVA_HOME` to it, rebuild |
| `google-services.json` missing | no Firebase | add it (guide 07) or ignore — app runs offline without it |
| Build writes junk to C: / fails on locked dirs | default TEMP | point `TEMP`/`TMP`/`GRADLE_USER_HOME` to your data drive (guide 10) |

## GPS / NFC / cloud
- **No GPS coords:** grant Location + go outdoors/near window; Android 12+
  must allow "Precise".
- **No family notification:** Firestore/FCM configured? (guide 07) + signed in.
- **NFC does nothing:** phone lacks NFC or tag is plain — use the in-app demo
  pulse (works always).

**Still stuck?** Serial Monitor output + exact screen message → open an issue
with both; those two facts identify nearly every fault.
