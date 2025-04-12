/*
 * Emerband Smart Watch - Emergency Response System
 * 
 * This sketch is designed for Arduino Nano 33 BLE or similar boards
 * It creates a BLE peripheral that sends emergency signals to the
 * Emerband Android application.
 * 
 * Features:
 * - Emergency alert (E)
 * - Fake call trigger (F)
 * - Cyber cell alert (C)
 * - General alert (A)
 * - Optional GPS integration
 * - Optional fall detection
 * 
 * Created by Emerband Team
 */

#include <ArduinoBLE.h>
#include <Wire.h>

// Uncomment to enable optional features
//#define ENABLE_GPS
//#define ENABLE_FALL_DETECTION
//#define ENABLE_DISPLAY
//#define ENABLE_POWER_SAVING

// Optional includes based on features
#ifdef ENABLE_GPS
  #include <TinyGPS++.h>
  #include <SoftwareSerial.h>
#endif

#ifdef ENABLE_FALL_DETECTION
  #include <Arduino_LSM9DS1.h>
#endif

#ifdef ENABLE_DISPLAY
  #include <Adafruit_GFX.h>
  #include <Adafruit_SSD1306.h>
#endif

#ifdef ENABLE_POWER_SAVING
  #include <ArduinoPower.h>
#endif

// BLE UUIDs
#define BLE_UUID_EMERBAND_SERVICE "0000180D-0000-1000-8000-00805f9b34fb" // Using Heart Rate service UUID
#define BLE_UUID_ALERT_CHAR "00002A37-0000-1000-8000-00805f9b34fb" // Using Heart Rate Measurement characteristic UUID

// Button pin definitions
const int EMERGENCY_BTN_PIN = 2;   // Emergency button (E)
const int FAKE_CALL_BTN_PIN = 3;   // Fake call button (F)
const int CYBER_CELL_BTN_PIN = 4;  // Cyber cell button (C)
const int ALERT_BTN_PIN = 5;       // Alert button (A)

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

// BLE Service and Characteristic
BLEService emerbandService(BLE_UUID_EMERBAND_SERVICE);

#ifdef ENABLE_GPS
  // GPS setup
  static const int RXPin = 10, TXPin = 11;
  static const uint32_t GPSBaud = 9600;
  TinyGPSPlus gps;
  SoftwareSerial ss(RXPin, TXPin);
  
  // Characteristic with space for GPS data (20 bytes)
  BLECharacteristic alertCharacteristic(BLE_UUID_ALERT_CHAR, BLERead | BLENotify, 20);
#else
  // Basic characteristic for signal only (1 byte)
  BLECharacteristic alertCharacteristic(BLE_UUID_ALERT_CHAR, BLERead | BLENotify, 1);
#endif

#ifdef ENABLE_FALL_DETECTION
  // Accelerometer thresholds
  const float IMPACT_THRESHOLD = 3.0;  // G-forces
  const float STEADY_THRESHOLD = 0.8;  // G-forces
  const long STEADY_TIME = 2000;       // milliseconds
  
  // State variables
  boolean possibleFall = false;
  long fallTimestamp;
#endif

#ifdef ENABLE_DISPLAY
  #define SCREEN_WIDTH 128
  #define SCREEN_HEIGHT 32
  #define OLED_RESET    -1
  #define SCREEN_ADDRESS 0x3C
  
  Adafruit_SSD1306 display(SCREEN_WIDTH, SCREEN_HEIGHT, &Wire, OLED_RESET);
#endif

void setup() {
  Serial.begin(9600);
  while (!Serial && millis() < 5000);  // Wait for Serial monitor, but timeout after 5 seconds
  
  Serial.println("Emerband Smartwatch starting...");
  
  // Button setup
  pinMode(EMERGENCY_BTN_PIN, INPUT_PULLUP);
  pinMode(FAKE_CALL_BTN_PIN, INPUT_PULLUP);
  pinMode(CYBER_CELL_BTN_PIN, INPUT_PULLUP);
  pinMode(ALERT_BTN_PIN, INPUT_PULLUP);
  pinMode(LED_BUILTIN, OUTPUT);
  
  // Initialize BLE
  if (!BLE.begin()) {
    Serial.println("BLE initialization failed!");
    while (1) {
      // Blink LED rapidly to indicate failure
      digitalWrite(LED_BUILTIN, HIGH);
      delay(200);
      digitalWrite(LED_BUILTIN, LOW);
      delay(200);
    }
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
  
  Serial.println("BLE initialized, advertising as 'EmergencyWatch'");
  
  // Optional feature initializations
  #ifdef ENABLE_GPS
    ss.begin(GPSBaud);
    Serial.println("GPS initialized");
  #endif
  
  #ifdef ENABLE_FALL_DETECTION
    if (!IMU.begin()) {
      Serial.println("Failed to initialize IMU!");
    } else {
      Serial.println("Fall detection initialized");
    }
  #endif
  
  #ifdef ENABLE_DISPLAY
    if(!display.begin(SSD1306_SWITCHCAPVCC, SCREEN_ADDRESS)) {
      Serial.println(F("SSD1306 allocation failed"));
    } else {
      display.display();
      delay(1000);
      display.clearDisplay();
      updateDisplay("Ready");
      Serial.println("Display initialized");
    }
  #endif
  
  #ifdef ENABLE_POWER_SAVING
    // Disable unused peripherals
    //Power.disableUSB();  // Uncomment in production, but keep USB for debugging
    Serial.println("Power saving initialized");
  #endif
  
  Serial.println("Emerband smartwatch started. Waiting for connections...");
  
  // Signal ready status with LED
  for (int i = 0; i < 3; i++) {
    digitalWrite(LED_BUILTIN, HIGH);
    delay(100);
    digitalWrite(LED_BUILTIN, LOW);
    delay(100);
  }
}

void loop() {
  // Process GPS data if enabled
  #ifdef ENABLE_GPS
    while (ss.available() > 0) {
      gps.encode(ss.read());
    }
  #endif
  
  // Check for falls if enabled
  #ifdef ENABLE_FALL_DETECTION
    checkForFall();
  #endif
  
  // Listen for BLE connections
  BLEDevice central = BLE.central();
  
  if (central) {
    #ifdef ENABLE_DISPLAY
      updateDisplay("Connected");
    #endif
    
    Serial.print("Connected to central: ");
    Serial.println(central.address());
    
    // Light up LED while connected
    digitalWrite(LED_BUILTIN, HIGH);
    
    // While connected
    while (central.connected()) {
      checkButtons();
      
      #ifdef ENABLE_FALL_DETECTION
        checkForFall();
      #endif
      
      delay(50); // Short delay to prevent bouncing
    }
    
    // Turn off LED when disconnected
    digitalWrite(LED_BUILTIN, LOW);
    
    #ifdef ENABLE_DISPLAY
      updateDisplay("Ready");
    #endif
    
    Serial.println("Disconnected from central");
  }
  
  // Power management
  #ifdef ENABLE_POWER_SAVING
    checkSleep();
  #endif
}

void checkButtons() {
  // Check Emergency button
  int emergencyButtonState = digitalRead(EMERGENCY_BTN_PIN);
  if (emergencyButtonState != lastEmergencyButtonState && emergencyButtonState == LOW) {
    sendAlert(EMERGENCY_SIGNAL);
    Serial.println("Emergency signal sent");
    
    #ifdef ENABLE_DISPLAY
      updateDisplay("Emergency Sent!");
    #endif
  }
  lastEmergencyButtonState = emergencyButtonState;
  
  // Check Fake Call button
  int fakeCallButtonState = digitalRead(FAKE_CALL_BTN_PIN);
  if (fakeCallButtonState != lastFakeCallButtonState && fakeCallButtonState == LOW) {
    sendAlert(FAKE_CALL_SIGNAL);
    Serial.println("Fake call signal sent");
    
    #ifdef ENABLE_DISPLAY
      updateDisplay("Fake Call Sent!");
    #endif
  }
  lastFakeCallButtonState = fakeCallButtonState;
  
  // Check Cyber Cell button
  int cyberCellButtonState = digitalRead(CYBER_CELL_BTN_PIN);
  if (cyberCellButtonState != lastCyberCellButtonState && cyberCellButtonState == LOW) {
    sendAlert(CYBER_CELL_SIGNAL);
    Serial.println("Cyber cell signal sent");
    
    #ifdef ENABLE_DISPLAY
      updateDisplay("Cyber Alert Sent!");
    #endif
  }
  lastCyberCellButtonState = cyberCellButtonState;
  
  // Check Alert button
  int alertButtonState = digitalRead(ALERT_BTN_PIN);
  if (alertButtonState != lastAlertButtonState && alertButtonState == LOW) {
    sendAlert(ALERT_SIGNAL);
    Serial.println("Alert signal sent");
    
    #ifdef ENABLE_DISPLAY
      updateDisplay("Alert Sent!");
    #endif
  }
  lastAlertButtonState = alertButtonState;
}

void sendAlert(byte signal) {
  #ifdef ENABLE_GPS
    // Create data packet with GPS data if available
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
      
      // Send the data packet
      if (alertCharacteristic.writeValue(dataPacket, 20)) {
        blinkLED(1, 200);  // Success - blink once
      } else {
        blinkLED(3, 100);  // Failure - blink rapidly
      }
      
    } else {
      // No GPS data available
      dataPacket[9] = 0;
      
      // Send the data packet with only the signal
      if (alertCharacteristic.writeValue(dataPacket, 20)) {
        blinkLED(1, 200);
      } else {
        blinkLED(3, 100);
      }
    }
  #else
    // Simple signal without GPS
    if (alertCharacteristic.writeValue(&signal, 1)) {
      blinkLED(1, 200);
    } else {
      blinkLED(3, 100);
    }
  #endif
}

// Blink LED for visual feedback
void blinkLED(int times, int duration) {
  for (int i = 0; i < times; i++) {
    digitalWrite(LED_BUILTIN, HIGH);
    delay(duration);
    digitalWrite(LED_BUILTIN, LOW);
    if (i < times - 1) {
      delay(duration);
    }
  }
}

#ifdef ENABLE_FALL_DETECTION
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
      
      #ifdef ENABLE_DISPLAY
        updateDisplay("Possible Fall!");
      #endif
    }
    
    // Check for steady state after impact
    if (possibleFall && millis() - fallTimestamp > STEADY_TIME) {
      if (magnitude > 0.5 && magnitude < STEADY_THRESHOLD) {
        // Fall confirmed - send alert
        Serial.println("Fall detected");
        
        #ifdef ENABLE_DISPLAY
          updateDisplay("Fall Detected!");
        #endif
        
        sendAlert(FAKE_CALL_SIGNAL);
        
        // Reset fall detection
        possibleFall = false;
      } else {
        // Not a fall
        possibleFall = false;
        
        #ifdef ENABLE_DISPLAY
          updateDisplay("Ready");
        #endif
      }
    }
  }
}
#endif

#ifdef ENABLE_DISPLAY
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
#endif

#ifdef ENABLE_POWER_SAVING
void checkSleep() {
  static unsigned long lastConnectionTime = 0;
  
  if (!BLE.connected()) {
    // If not connected for more than 5 minutes, sleep
    if (millis() - lastConnectionTime > 300000) {
      #ifdef ENABLE_DISPLAY
        updateDisplay("Sleeping...");
        delay(1000);
        display.clearDisplay();
        display.display();
      #endif
      
      Serial.println("Entering sleep mode...");
      delay(100);  // Allow serial to complete
      
      // Enter deep sleep
      Power.sleep();
      
      // Code continues here after waking up
      Serial.println("Waking up from sleep");
      
      #ifdef ENABLE_DISPLAY
        updateDisplay("Ready");
      #endif
    }
  } else {
    lastConnectionTime = millis();
  }
}
#endif 