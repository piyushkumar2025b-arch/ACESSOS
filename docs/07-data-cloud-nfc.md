# 07 — Data, Cloud Sync & NFC Cards

## What lives where
| Data | Store | Why |
|---|---|---|
| Medicines, med logs, family members | **Room** (local DB) | instant, offline |
| SOS events + GPS | **Firestore** + local | family sees them remotely |
| Password vault entries | **Room only** (AES via `CryptoHelper`) | never leaves device |
| Alerts/reminder history | Room | offline |

**No passwords in Firestore — by design.** If you only test BLE, you don't
need any cloud setup; the app works fully offline except SOS-to-family.

## Firebase setup (needed for SOS → family + AI features)
1. [console.firebase.google.com](https://console.firebase.google.com) → add
   project → *Add app → Android* → package name
   **`com.aistudio.accessos.kdwbrr`** (the `applicationId` in `app/build.gradle.kts`).
2. Download `google-services.json` → drop it at
   `app/google-services.json` (the `google-services` Gradle plugin reads it).
3. In Console enable **Firestore** (start in test mode) + **Cloud Messaging**.
4. `GeminiAiService.kt` (medicine label AI) uses a Gemini key — put yours in
   `local.properties` as `GEMINI_API_KEY=…` or skip AI; everything else works.
5. Rebuild/install. Sign in on the elder phone; family members sign in with
   their own accounts to receive SOS notifications (`DataSyncManager` +
   `NotificationHelper` push the FCM alert with the GPS link).

## NFC care cards (card-tap unlock)
- App ships **demo mode**: tapping the NFC icon or the Home hero's
  fingerprint chip simulates a success pulse + unlocks (for labs without tags).
- For real tags: write an **NDEF URL** to any NTAG213/215 sticker (e.g. via
  "NFC Tools" app) containing your unlock token, then read it in the app —
  `MainActivity` handles the NFC intent flow. No tags? Live happily on demo
  mode; BLE SOS is completely independent of NFC.

**Checkpoint:** SOS appears in Firestore/family feed, vault saves → go to 08.
