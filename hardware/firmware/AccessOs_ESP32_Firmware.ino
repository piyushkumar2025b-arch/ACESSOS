/**
 * AccessOS ESP32 Smart Hardware Firmware
 * 
 * Hardware Features:
 * - 4-Pin Push Button on GPIO 27 (Emergency SOS trigger with debounce)
 * - 5mm Red LED with 330 ohm resistor on GPIO 26 (Status & Alarm indicator)
 * - 5V-12V Buzzer on GPIO 25 (Audible alert & ACK chime)
 * - 1.3" I2C OLED Display (SH1106 / SSD1306) on GPIO 21 (SDA) & GPIO 22 (SCL)
 * - Bluetooth Low Energy (BLE) Server advertising "AccessOS-ESP32"
 * 
 * GATT Profile:
 * - Service UUID: 4fafc201-1fb5-459e-8fcc-c5c9c331914b
 * - Event Characteristic (Notify): beb5483e-36e1-4688-b7f5-ea07361b26a8
 * - Command/ACK Characteristic (Write): beb5483f-36e1-4688-b7f5-ea07361b26a8
 * 
 * Arduino IDE Libraries Required:
 * - Adafruit GFX Library
 * - Adafruit SSD1306 (or Adafruit SH110X for 1.3" OLED)
 * - Built-in ESP32 BLE BLEDevice library
 */

#include <Wire.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

// PIN DEFINITIONS
#define BUTTON_PIN      27   // Push button to GND (INPUT_PULLUP)
#define LED_PIN         26   // Red LED anode through 330 ohm resistor
#define BUZZER_PIN      25   // Buzzer signal / transistor gate
#define I2C_SDA         21   // OLED SDA
#define I2C_SCL         22   // OLED SCL

// OLED DISPLAY CONFIGURATION
#define SCREEN_WIDTH    128
#define SCREEN_HEIGHT   64
#define OLED_RESET      -1
#define SCREEN_ADDRESS  0x3C // 0x3C or 0x3D for 1.3" OLED displays

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// BLE UUID DEFINITIONS (Must match Android app AccessOsBleManager)
#define SERVICE_UUID           "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
#define CHAR_EVENT_UUID        "beb5483e-36e1-4688-b7f5-ea07361b26a8"
#define CHAR_COMMAND_UUID      "beb5483f-36e1-4688-b7f5-ea07361b26a8"

BLEServer* pServer = NULL;
BLECharacteristic* pEventCharacteristic = NULL;
BLECharacteristic* pCommandCharacteristic = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;

// SYSTEM STATES
enum SystemState {
  STATE_IDLE_DISCONNECTED,
  STATE_IDLE_CONNECTED,
  STATE_SOS_TRIGGERED,
  STATE_PHONE_ACKNOWLEDGED
};

SystemState currentState = STATE_IDLE_DISCONNECTED;
unsigned long lastStateChange = 0;
unsigned long lastEventCounter = 1000;
String currentEventId = "";

// BUTTON DEBOUNCE VARIABLES
int lastButtonState = HIGH;
int buttonState = HIGH;
unsigned long lastDebounceTime = 0;
const unsigned long debounceDelay = 50;

// SERVER CALLBACKS
class MyServerCallbacks : public BLEServerCallbacks {
  void onConnect(BLEServer* pServer) {
    deviceConnected = true;
    currentState = STATE_IDLE_CONNECTED;
  }

  void onDisconnect(BLEServer* pServer) {
    deviceConnected = false;
    currentState = STATE_IDLE_DISCONNECTED;
  }
};

// COMMAND & ACK CALLBACK
class CommandCallbacks : public BLECharacteristicCallbacks {
  void onWrite(BLECharacteristic* pCharacteristic) {
    std::string rxValue = pCharacteristic->getValue();
    if (rxValue.length() > 0) {
      String commandStr = String(rxValue.c_str());
      Serial.print("[BLE RX] Command received: ");
      Serial.println(commandStr);

      if (commandStr.indexOf("ACK") >= 0) {
        currentState = STATE_PHONE_ACKNOWLEDGED;
        lastStateChange = millis();
        // Beep confirmation chime
        tone(BUZZER_PIN, 1500, 100);
        delay(120);
        tone(BUZZER_PIN, 2000, 200);
        digitalWrite(LED_PIN, HIGH);
      } else if (commandStr.indexOf("PING") >= 0 || commandStr.indexOf("TEST") >= 0) {
        // Hardware test ping from AccessOS App
        Serial.println("[BLE] Hardware Test Ping initiated from phone!");
        digitalWrite(LED_PIN, HIGH);
        tone(BUZZER_PIN, 1800, 100);
        delay(120);
        digitalWrite(LED_PIN, LOW);
        delay(60);
        digitalWrite(LED_PIN, HIGH);
        tone(BUZZER_PIN, 2400, 150);
        delay(160);
        digitalWrite(LED_PIN, LOW);
        updateDisplay("PING RECEIVED", "Phone Link Active", "Buzzer & LED Tested", "AccessOS Ready");
      }
    }
  }
};

void updateDisplay(const char* title, const char* line1, const char* line2, const char* footer) {
  display.clearDisplay();
  display.setTextColor(SSD1306_WHITE);

  // Header Bar
  display.setTextSize(1);
  display.setCursor(0, 0);
  display.print("== AccessOS SOS ==");

  // Title
  display.setTextSize(1);
  display.setCursor(0, 14);
  display.print(title);

  // Line 1
  display.setCursor(0, 26);
  display.print(line1);

  // Line 2
  display.setCursor(0, 38);
  display.print(line2);

  // Footer / Status
  display.setCursor(0, 52);
  display.print(footer);

  display.display();
}

void triggerSosAlert() {
  lastEventCounter++;
  currentEventId = "evt_" + String(millis()) + "_" + String(lastEventCounter);

  currentState = STATE_SOS_TRIGGERED;
  lastStateChange = millis();

  // 1. Audible & Visual Local Feedback
  digitalWrite(LED_PIN, HIGH);
  tone(BUZZER_PIN, 2500, 300);

  // 2. Transmit Event Payload to Android via BLE
  if (deviceConnected && pEventCharacteristic != NULL) {
    String payload = "{\"version\":1,\"event\":\"SOS_BUTTON_PRESSED\",\"deviceId\":\"AccessOS-ESP32\",\"eventId\":\"" +
                     currentEventId + "\",\"timestamp\":" + String(millis()) + "}";

    pEventCharacteristic->setValue(payload.c_str());
    pEventCharacteristic->notify();
    Serial.println("[BLE TX] Transmitted SOS Event: " + payload);
  } else {
    Serial.println("[LOCAL] SOS pressed but phone not currently connected via BLE.");
  }
}

void setup() {
  Serial.begin(115200);
  delay(500);
  Serial.println("\n--- Starting AccessOS ESP32 Smart Hardware ---");

  // GPIO CONFIGURATION
  pinMode(BUTTON_PIN, INPUT_PULLUP);
  pinMode(LED_PIN, OUTPUT);
  pinMode(BUZZER_PIN, OUTPUT);

  digitalWrite(LED_PIN, LOW);
  digitalWrite(BUZZER_PIN, LOW);

  // OLED INITIALIZATION
  Wire.begin(I2C_SDA, I2C_SCL);
  if (!display.begin(SSD1306_SWITCHCAPVCC, SCREEN_ADDRESS)) {
    Serial.println("SSD1306 allocation failed, retrying address 0x3D...");
    display.begin(SSD1306_SWITCHCAPVCC, 0x3D);
  }

  updateDisplay("INITIALIZING...", "Starting BLE Server", "Checking Sensors", "AccessOS v1.0");

  // Self-test LED and Buzzer
  digitalWrite(LED_PIN, HIGH);
  tone(BUZZER_PIN, 1800, 80);
  delay(100);
  digitalWrite(LED_PIN, LOW);

  // BLE INITIALIZATION
  BLEDevice::init("AccessOS-ESP32");
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create GATT Service
  BLEService* pService = pServer->createService(SERVICE_UUID);

  // Create Event Characteristic (Notify)
  pEventCharacteristic = pService->createCharacteristic(
    CHAR_EVENT_UUID,
    BLECharacteristic::PROPERTY_NOTIFY | BLECharacteristic::PROPERTY_READ
  );
  pEventCharacteristic->addDescriptor(new BLE2902());

  // Create Command/ACK Characteristic (Write)
  pCommandCharacteristic = pService->createCharacteristic(
    CHAR_COMMAND_UUID,
    BLECharacteristic::PROPERTY_WRITE | BLECharacteristic::PROPERTY_WRITE_NR
  );
  pCommandCharacteristic->setCallbacks(new CommandCallbacks());

  pService->start();

  // Start Advertising
  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_UUID);
  pAdvertising->setScanResponse(true);
  pAdvertising->setMinPreferred(0x06);
  pAdvertising->setMinPreferred(0x12);
  BLEDevice::startAdvertising();

  Serial.println("AccessOS BLE Advertising started. Ready to connect!");
  updateDisplay("READY", "BLE: Waiting for Phone", "Press Button for SOS", "AccessOS Armed");
}

void loop() {
  // 1. READ BUTTON WITH DEBOUNCE
  int reading = digitalRead(BUTTON_PIN);
  if (reading != lastButtonState) {
    lastDebounceTime = millis();
  }

  if ((millis() - lastDebounceTime) > debounceDelay) {
    if (reading != buttonState) {
      buttonState = reading;
      // Button pressed (active LOW)
      if (buttonState == LOW) {
        Serial.println("[BUTTON] Physical SOS button pressed!");
        triggerSosAlert();
      }
    }
  }
  lastButtonState = reading;

  // 2. STATE MACHINE & OLED REFRESH
  switch (currentState) {
    case STATE_IDLE_DISCONNECTED:
      digitalWrite(LED_PIN, (millis() % 1500 < 50) ? HIGH : LOW); // Short pulse every 1.5s
      updateDisplay("STATUS: READY", "BLE: Waiting for Phone", "Hold button for SOS", "Phone Disconnected");
      break;

    case STATE_IDLE_CONNECTED:
      digitalWrite(LED_PIN, (millis() % 3000 < 100) ? HIGH : LOW); // Steady heartbeat
      updateDisplay("STATUS: LINKED", "BLE: Phone Connected", "Button Armed: OK", "Care Circle Active");
      break;

    case STATE_SOS_TRIGGERED:
      // Fast alarm blink & buzzer pulse while waiting for ACK
      digitalWrite(LED_PIN, (millis() % 200 < 100) ? HIGH : LOW);
      if (millis() - lastStateChange < 4000) {
        if ((millis() % 400) < 150) {
          tone(BUZZER_PIN, 2200, 100);
        }
      }
      updateDisplay("🚨 SOS TRIGGERED!", "Sending to Phone...", "Event: " + currentEventId.substring(0, 12), "Awaiting Phone ACK");

      // Auto-revert to connected/disconnected after 8 seconds if no ACK
      if (millis() - lastStateChange > 8000) {
        currentState = deviceConnected ? STATE_IDLE_CONNECTED : STATE_IDLE_DISCONNECTED;
        digitalWrite(LED_PIN, LOW);
      }
      break;

    case STATE_PHONE_ACKNOWLEDGED:
      digitalWrite(LED_PIN, HIGH);
      updateDisplay("✅ PHONE RECEIVED!", "Cloud Alert Active", "Family Dispatched", "Care Circle Alerted");
      if (millis() - lastStateChange > 5000) {
        currentState = deviceConnected ? STATE_IDLE_CONNECTED : STATE_IDLE_DISCONNECTED;
        digitalWrite(LED_PIN, LOW);
      }
      break;
  }

  // Handle BLE Disconnection / Advertising restart
  if (!deviceConnected && oldDeviceConnected) {
    delay(500);
    pServer->startAdvertising();
    Serial.println("[BLE] Restarted advertising after disconnect.");
    oldDeviceConnected = deviceConnected;
  }

  if (deviceConnected && !oldDeviceConnected) {
    oldDeviceConnected = deviceConnected;
    Serial.println("[BLE] Phone successfully connected to AccessOS hardware.");
  }

  delay(20);
}
