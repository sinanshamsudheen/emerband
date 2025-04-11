# Arduino Smartwatch Setup Instructions

This guide provides instructions for programming your Arduino-based smartwatch to work with the Emerband emergency response Android application.

## Hardware Requirements

- **Arduino Nano 33 BLE** or similar BLE-capable Arduino board
- **Battery** (LiPo 3.7V recommended for wearable applications)
- **Push buttons** (at least one, preferably 4 for all signal types)
- **Optional components**:
  - Small OLED display (128x32 or 128x64)
  - Vibration motor
  - GPS module (for enhanced location data)
  - Accelerometer (for fall detection)
  - 3D printed enclosure for wearable form factor

## Software Setup

### Required Libraries

Install the following libraries via the Arduino Library Manager:

- **ArduinoBLE** - For Bluetooth Low Energy communication
- **Arduino_LSM9DS1** - For accelerometer/gyroscope (optional, for fall detection)
- **TinyGPS++** - For GPS module integration (optional)
- **Adafruit SSD1306** - For OLED display (optional)
- **Adafruit GFX Library** - For OLED display (optional)

### Basic Smartwatch Sketch

Here's a basic Arduino sketch that implements the core functionality:

```cpp
#include <ArduinoBLE.h>
#include <Wire.h>

// BLE UUIDs
#define BLE_UUID_EMERBAND_SERVICE "0000180D-0000-1000-8000-00805f9b34fb" // Using Heart Rate service UUID as example
#define BLE_UUID_ALERT_CHAR "00002A37-0000-1000-8000-00805f9b34fb" // Using Heart Rate Measurement characteristic UUID

// Button pin definitions
const int EMERGENCY_BTN_PIN = 2;   // Emergency button (E)
const int FAKE_CALL_BTN_PIN = 3;   // Fake call button (F)
const int CYBER_CELL_BTN_PIN = 4;  // Cyber cell button (C)
const int ALERT_BTN_PIN = 5;       // Alert button (A)

// BLE Service and Characteristic
BLEService emerbandService(BLE_UUID_EMERBAND_SERVICE);
BLECharacteristic alertCharacteristic(BLE_UUID_ALERT_CHAR, BLERead | BLENotify, 1);

// Signal values
const byte EMERGENCY_SIGNAL = 'E';
const byte FAKE_CALL_SIGNAL = 'F';
const byte CYBER_CELL_SIGNAL = 'C';
const byte ALERT_SIGNAL = 'A';

// Button states
int lastEmergencyButtonState = HIGH;
int lastFakeCallButtonState = HIGH;
int lastCyberCellButtonState = HIGH;
int lastAlertButtonState = HIGH;

void setup() {
  Serial.begin(9600);
  
  // Button setup
  pinMode(EMERGENCY_BTN_PIN, INPUT_PULLUP);
  pinMode(FAKE_CALL_BTN_PIN, INPUT_PULLUP);
  pinMode(CYBER_CELL_BTN_PIN, INPUT_PULLUP);
  pinMode(ALERT_BTN_PIN, INPUT_PULLUP);
  
  // Initialize BLE
  if (!BLE.begin()) {
    Serial.println("BLE initialization failed!");
    while (1);
  }
  
  // Set up BLE advertising
  BLE.setLocalName("EmergencyWatch");
  BLE.setAdvertisedService(emerbandService);
  
  // Add characteristic to the service
  emerbandService.addCharacteristic(alertCharacteristic);
  
  // Add the service to the BLE stack
  BLE.addService(emerbandService);
  
  // Start advertising
  BLE.advertise();
  
  Serial.println("Emerband smartwatch started. Waiting for connections...");
}

void loop() {
  // Listen for BLE connections
  BLEDevice central = BLE.central();
  
  if (central) {
    Serial.print("Connected to central: ");
    Serial.println(central.address());
    
    // While connected
    while (central.connected()) {
      checkButtons(central);
      delay(50); // Short delay to prevent bouncing
    }
    
    Serial.println("Disconnected from central");
  }
}

void checkButtons(BLEDevice central) {
  // Check Emergency button
  int emergencyButtonState = digitalRead(EMERGENCY_BTN_PIN);
  if (emergencyButtonState != lastEmergencyButtonState && emergencyButtonState == LOW) {
    sendAlert(EMERGENCY_SIGNAL);
    Serial.println("Emergency signal sent");
  }
  lastEmergencyButtonState = emergencyButtonState;
  
  // Check Fake Call button
  int fakeCallButtonState = digitalRead(FAKE_CALL_BTN_PIN);
  if (fakeCallButtonState != lastFakeCallButtonState && fakeCallButtonState == LOW) {
    sendAlert(FAKE_CALL_SIGNAL);
    Serial.println("Fake call signal sent");
  }
  lastFakeCallButtonState = fakeCallButtonState;
  
  // Check Cyber Cell button
  int cyberCellButtonState = digitalRead(CYBER_CELL_BTN_PIN);
  if (cyberCellButtonState != lastCyberCellButtonState && cyberCellButtonState == LOW) {
    sendAlert(CYBER_CELL_SIGNAL);
    Serial.println("Cyber cell signal sent");
  }
  lastCyberCellButtonState = cyberCellButtonState;
  
  // Check Alert button
  int alertButtonState = digitalRead(ALERT_BTN_PIN);
  if (alertButtonState != lastAlertButtonState && alertButtonState == LOW) {
    sendAlert(ALERT_SIGNAL);
    Serial.println("Alert signal sent");
  }
  lastAlertButtonState = alertButtonState;
}

void sendAlert(byte signal) {
  if (alertCharacteristic.writeValue(&signal, 1)) {
    // If successful, blink LED or provide feedback
    digitalWrite(LED_BUILTIN, HIGH);
    delay(200);
    digitalWrite(LED_BUILTIN, LOW);
  }
}
```

## Advanced Features

### Adding GPS Capability

If your smartwatch includes a GPS module, add this to the basic sketch:

```cpp
#include <TinyGPS++.h>
#include <SoftwareSerial.h>

// GPS setup
static const int RXPin = 10, TXPin = 11;
static const uint32_t GPSBaud = 9600;
TinyGPSPlus gps;
SoftwareSerial ss(RXPin, TXPin);

// Modified alertCharacteristic to include GPS data (20 bytes total)
BLECharacteristic alertCharacteristic(BLE_UUID_ALERT_CHAR, BLERead | BLENotify, 20);

// In setup():
ss.begin(GPSBaud);

// Modified sendAlert function to include GPS data
void sendAlert(byte signal) {
  byte dataPacket[20];
  dataPacket[0] = signal;
  
  // Add GPS data if available
  if (gps.location.isValid()) {
    // Convert latitude and longitude to byte array
    float lat = gps.location.lat();
    float lng = gps.location.lng();
    
    // Copy lat/lng bytes into packet
    memcpy(&dataPacket[1], &lat, 4);
    memcpy(&dataPacket[5], &lng, 4);
    
    // Set a flag indicating GPS data is included
    dataPacket[9] = 1;
  } else {
    // No GPS data available
    dataPacket[9] = 0;
  }
  
  // Send the data packet
  alertCharacteristic.writeValue(dataPacket, 20);
}

// Add this to loop() to continuously read GPS data
void loop() {
  // Process GPS data
  while (ss.available() > 0) {
    gps.encode(ss.read());
  }
  
  // Rest of the code...
}
```

### Fall Detection

To implement automatic fall detection using the accelerometer:

```cpp
#include <Arduino_LSM9DS1.h>

// Accelerometer thresholds
const float IMPACT_THRESHOLD = 3.0;  // G-forces
const float STEADY_THRESHOLD = 0.8;  // G-forces
const long STEADY_TIME = 2000;       // milliseconds

// State variables
boolean possibleFall = false;
long fallTimestamp;

// In setup():
if (!IMU.begin()) {
  Serial.println("Failed to initialize IMU!");
}

// Add this function and call it in your loop
void checkForFall() {
  float x, y, z;
  
  if (IMU.accelerationAvailable()) {
    IMU.readAcceleration(x, y, z);
    
    // Calculate magnitude of acceleration
    float magnitude = sqrt(x*x + y*y + z*z);
    
    // Check for impact
    if (magnitude > IMPACT_THRESHOLD && !possibleFall) {
      possibleFall = true;
      fallTimestamp = millis();
    }
    
    // Check for steady state after impact
    if (possibleFall && millis() - fallTimestamp > STEADY_TIME) {
      if (magnitude > 0.5 && magnitude < STEADY_THRESHOLD) {
        // Fall confirmed - send alert
        sendAlert(FAKE_CALL_SIGNAL);
        Serial.println("Fall detected");
        
        // Reset fall detection
        possibleFall = false;
      }
    }
  }
}
```

## OLED Display Integration

Add a small display to show status:

```cpp
#include <Adafruit_SSD1306.h>
#include <Adafruit_GFX.h>

#define SCREEN_WIDTH 128
#define SCREEN_HEIGHT 32
#define OLED_RESET    -1
#define SCREEN_ADDRESS 0x3C

Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);

// In setup():
if(!display.begin(SSD1306_SWITCHCAPVCC, SCREEN_ADDRESS)) {
  Serial.println(F("SSD1306 allocation failed"));
}
display.display();
delay(2000);
display.clearDisplay();
updateDisplay("Ready");

// Display update function
void updateDisplay(String status) {
  display.clearDisplay();
  display.setTextSize(1);
  display.setTextColor(SSD1306_WHITE);
  display.setCursor(0, 0);
  display.println("EmergencyWatch");
  display.setCursor(0, 16);
  display.println(status);
  display.display();
}
```

## Wiring Diagram

### Basic Setup
```
Arduino Nano 33 BLE:
- D2: Emergency Button (other end to GND)
- D3: Fake Call Button (other end to GND)
- D4: Cyber Cell Button (other end to GND)
- D5: Alert Button (other end to GND)
- A4/SDA: OLED Display SDA (if used)
- A5/SCL: OLED Display SCL (if used)
- 3.3V: Power for Display
- GND: Common Ground
```

### With GPS Module
```
Additional connections:
- D10: GPS RX
- D11: GPS TX
- 3.3V: GPS VCC
- GND: GPS GND
```

## Power Management

For better battery life:

```cpp
// In your Arduino sketch, add:
#include <ArduinoPower.h>

// In setup():
Power.disableUSB();  // If not debugging

// Optional: Sleep when not connected
void checkSleep() {
  static unsigned long lastConnectionTime = 0;
  
  if (!BLE.connected()) {
    // If not connected for more than 5 minutes, sleep
    if (millis() - lastConnectionTime > 300000) {
      Power.sleep();
    }
  } else {
    lastConnectionTime = millis();
  }
}
```

## Building and Testing

1. **Compile and Upload**: Connect your Arduino to your computer and upload the sketch.

2. **Testing with Serial Monitor**: Open the Serial Monitor to see debug output.

3. **Connection Testing**: Install the Emerband Android app on your phone and ensure it can discover and connect to the "EmergencyWatch" BLE device.

4. **Button Testing**: Press each button and verify the corresponding action in the Android app.

5. **Range Testing**: Test the maximum distance between the watch and phone while maintaining reliable connectivity.

## Troubleshooting

- **Connection Issues**: Ensure BLE is enabled on your phone and the Arduino is advertising.
- **Button Not Responding**: Check wiring and pullup resistors.
- **GPS Not Working**: Ensure clear sky view for satellite reception.
- **Battery Draining Quickly**: Implement power saving features or consider a larger battery.

## Enclosure Design

A 3D printable enclosure design file (STL) is available in the `arduino/enclosure` directory. The design includes:

- Slots for buttons
- Opening for USB charging
- Strap attachment points
- Battery compartment

## Further Customization

- Add vibration feedback using a small vibration motor connected to a PWM pin
- Implement long-press detection for different functions on the same button
- Add configurable emergency contact information stored in EEPROM
- Customize the signal patterns based on user preference

---

For more information or support, please open an issue on the GitHub repository. 