# 03 — Wiring the Breadboard (No Soldering)

Power OFF while wiring. ESP32 38-pin board straddles the breadboard center
divide so both sides have free tie points.

## Pin table (final — matches firmware + app)

| OLED / part | Its pin | → ESP32 / rail |
|---|---|---|
| OLED | `VCC` | `3V3` (or `VIN` if your OLED says 5V) |
| OLED | `GND` | `GND` rail |
| OLED | `SDA` | `GPIO 21` |
| OLED | `SCL` | `GPIO 22` |
| Button leg A | one corner pin | `GPIO 27` |
| Button diagonal leg | opposite corner | `GND` rail |
| LED anode (+) long leg | via 330 Ω resistor | `GPIO 26` |
| LED cathode (−) short leg | — | `GND` rail |
| Buzzer `+` | — | `GPIO 25` |
| Buzzer `−` | — | `GND` rail |

Button uses the chip's internal `INPUT_PULLUP`: idle = HIGH, pressed = LOW
(press connects GPIO 27 to GND). Debounce 50 ms is in firmware.

## Build order (10 min)
1. Seat ESP32 across the center divide.
2. Run one ESP32 `GND` pin to the blue (−) rail — this is your ground bus.
3. Button across the divide → GPIO 27 + GND rail (diagonal pins!).
4. Resistor GPIO 26 → empty row; LED anode into same row, cathode to GND rail.
5. Buzzer `+` → GPIO 25, `−` → GND rail.
6. OLED via M-F jumpers: VCC→3V3, GND→rail, SDA→21, SCL→22.

## Before power-on check
- [ ] No stray wire bridges across the divide
- [ ] LED polarity right (long leg toward resistor)
- [ ] OLED voltage matches (3V3 vs VIN)
- [ ] Button pins are diagonal, not adjacent (adjacent = always pressed)

**Checkpoint:** neat board, USB unplugged → go to 04 and flash.
Full photos version: `hardware/HARDWARE_SETUP.md`.
