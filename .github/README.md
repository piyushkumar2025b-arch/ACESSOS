# AccessOS Android & Hardware Integration CI/CD

This repository includes automated **GitHub Actions Workflows** configured to automatically build installable Android APKs ready for physical hardware pairing (ESP32 Bluetooth Low Energy, 1.3" OLED, SOS Button, and Buzzer/LED).

---

## 🚀 Workflows Included

### 1. **Build & Release Android APK** (`.github/workflows/build-apk.yml`)
- **Triggers**:
  - Automatically runs on any `push` or `pull_request` to `main` or `master`.
  - Runs whenever a version tag is created (e.g., `git tag v1.0.0 && git push origin v1.0.0`).
  - Can be manually triggered via GitHub's **Run workflow** button (`workflow_dispatch`) with choices for `debug`, `release`, or `both`.
- **Outputs**:
  - Uploads the generated APK directly to GitHub Actions **Artifacts** (`AccessOS-Hardware-Debug-APK`).
  - When triggered by a tag (e.g. `v1.0.0`), it automatically creates a **GitHub Release** containing:
    1. The installable `AccessOS` Android APK.
    2. The ESP32 Arduino firmware (`AccessOs_ESP32_Firmware.ino`).
    3. The hardware assembly & pinout guide (`HARDWARE_SETUP.md`).

---

## 📱 Hardware Readiness in CI/CD

The workflow ensures the APK includes all required hardware drivers and manifest declarations:
1. **Bluetooth Low Energy (BLE)** permissions (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `ACCESS_FINE_LOCATION`) and the BLE feature flag.
2. **NFC Tag support** for lock/unlock card scanning.
3. Automatic creation of `.env` from `.env.example` so the Secrets Gradle Plugin builds without errors.
4. Auto-restores `debug.keystore` from the included base64 definition to ensure continuous signing integrity on clean runners.
5. Bidirectional GATT communication for sending `PING` and `ACK` to the ESP32 firmware.

---

## 📥 How to Download the Built APK from GitHub

1. Push this repository to GitHub:
   ```bash
   git add .
   git commit -m "Add GitHub Actions workflow for AccessOS APK"
   git push origin main
   ```
2. Navigate to your repository on GitHub.
3. Click the **Actions** tab at the top.
4. Select the latest run under **Build & Release AccessOS Android APK**.
5. Scroll down to the **Artifacts** section at the bottom of the page.
6. Click **AccessOS-Hardware-Debug-APK** to download the ZIP containing the APK.
7. Transfer the `.apk` file to your Android phone and install it.

---

## 🔌 Hardware Setup Reminder
Once installed on your phone:
1. Power the ESP32 board via USB.
2. The OLED screen will display **"AccessOS Ready - Adv..."**.
3. Open the **AccessOS** app on your phone.
4. Grant the requested Bluetooth & Location permissions.
5. In the app's **Hardware Status Card**, tap **Scan & Connect** to link with `AccessOS-ESP32`.
6. Tap **Test Ping** in the app or press the physical button on the breadboard to test the live connection!
