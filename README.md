# Emerband - Emergency Response Android Application

## Overview
Emerband is a comprehensive Android emergency response application that works with a BLE-connected Arduino smartwatch. It's designed to provide various emergency response capabilities including sending alerts, making fake calls, contacting cyber cell helplines, and triggering high-visibility alerts - all triggered via BLE signals from the smartwatch.

## Features

### 1. Emergency Alert (Signal 'E')
- Immediately extracts GPS coordinates
- Retrieves user's name from stored settings
- Sends emergency SMS alerts with location data
- Makes emergency calls to pre-configured contacts
- Works offline with automatic retry when connectivity returns

### 2. Fake Call (Signal 'F')
- Simulates a realistic incoming call
- Displays caller details, photo, and call timer
- Plays actual ringtone and vibration pattern
- Provides answer/decline buttons
- Serves as a discreet escape tool from uncomfortable situations

### 3. Cyber Cell Alert (Signal 'C')
- Calls cyber cell helpline
- Sends predefined alert SMS for digital threats
- Works offline with automatic retry
- Provides feedback on alert status

### 4. Alert Mode (Signal 'A')
- Activates multi-sensory alert system
- Displays flashing red screen
- Maximizes volume and plays siren
- Activates device vibration
- Triggers flashlight in strobe pattern

### 5. Offline Mode System
- Uses Room database to store emergency events
- Monitors network/GPS connectivity changes
- Processes stored events when connectivity returns
- Prioritizes based on event type and urgency
- Provides appropriate user feedback

## Architecture

### Core Components

#### BLE Background Service
- Central component that continuously scans for BLE signals
- Processes signals and delegates to appropriate handlers
- Runs as a foreground service with priority notification
- Automatically starts on device boot

#### Permission Manager
- Centralizes all permission handling
- Provides methods for requesting and checking permissions
- Handles permission rationales and denials

#### Resource Manager
- Manages shared resources like audio, vibration, and flashlight
- Centralizes SharedPreferences access
- Provides consistent utility methods

#### Offline Mode Manager
- Handles storing and retrieving events when offline
- Uses Room database with appropriate entities and DAOs
- Monitors connectivity changes via BroadcastReceiver

### Event Handlers
- `EmergencyHandler`: Processes emergency alerts
- `FakeCallActivity`: Manages fake call UI and behavior
- `CyberCellHandler`: Handles cyber cell alerts
- `AlertActivity`: Controls the alert mode UI and behavior

## Getting Started

### Prerequisites
- Android Studio 4.0+
- Android SDK 21+
- Arduino IDE (for smartwatch programming)

### Installation
1. Clone this repository
2. Open in Android Studio
3. Sync Gradle files
4. Build and run on a physical device (emulators don't support BLE)

### Detailed Installation Steps
1. **Setup Development Environment**:
   - Install Android Studio (4.0+)
   - Clone the repository to your local machine:
     ```
     git clone https://github.com/yourusername/emerband.git
     ```

2. **Open and Build Project**:
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned repository and select it
   - Wait for Gradle sync to complete
   - Ensure all dependencies are resolved (check the build.gradle files)

3. **Deploy to Device**:
   - Connect a physical Android device via USB
   - Enable Developer Options on your device:
     - Go to Settings > About phone
     - Tap "Build number" 7 times to enable Developer Options
   - Enable USB Debugging in Developer Options
   - In Android Studio, select your device from the dropdown menu
   - Click the Run button (green triangle) to build and install the app

### Smartwatch Setup
Complete Arduino smartwatch instructions are available in the `app/src/main/assets/arduino/` directory, including:

- Full Arduino sketch code (`EmerbandWatch.ino`)
- Detailed setup and wiring instructions (`README.md`)
- Hardware component list and recommendations
- 3D printable enclosure files (in the `enclosure/` subdirectory)
- Advanced features like GPS integration and fall detection

The smartwatch is designed to send four different signals ('E', 'F', 'C', 'A') to the Android app via BLE, triggering the corresponding emergency responses.

## Testing

### Using TestingUtils

The application includes a TestingUtils class that allows simulation of various scenarios without needing the actual BLE smartwatch:

```java
// Simulate BLE signals
TestingUtils.simulateBleSignal(context, 'E'); // Emergency
TestingUtils.simulateBleSignal(context, 'F'); // Fake Call
TestingUtils.simulateBleSignal(context, 'C'); // Cyber Cell
TestingUtils.simulateBleSignal(context, 'A'); // Alert

// Simulate offline mode
TestingUtils.simulateOfflineMode(context, 10000); // 10 seconds

// Test recovery scenarios
TestingUtils.testRecoveryFromForceClose(context);
```

### Testing Without Arduino Smartwatch

1. **Add test buttons to MainActivity** or use Android Studio's Evaluate Expression feature:
   - Open your app and navigate to the main screen
   - Use Android Studio's "Evaluate Expression" (Alt+F8 in debug mode) to run test commands

2. **Trigger specific scenarios**:
   ```java
   // Emergency alert
   TestingUtils.simulateBleSignal(getApplicationContext(), 'E');
   
   // Fake call
   TestingUtils.simulateBleSignal(getApplicationContext(), 'F');
   
   // Cyber cell alert
   TestingUtils.simulateBleSignal(getApplicationContext(), 'C');
   
   // Alert mode
   TestingUtils.simulateBleSignal(getApplicationContext(), 'A');
   ```

3. **Test offline functionality**:
   - Enable Airplane mode on your device
   - Trigger an emergency event
   - Verify that the event is stored in the database
   - Disable Airplane mode and verify the event is processed

4. **Test permission scenarios**:
   - Go to your device Settings > Apps > Emerband > Permissions
   - Toggle permissions off and on to test permission handling
   - Verify that the app properly requests permissions when needed

## Permissions Required
- Location (for BLE scanning and GPS)
- SMS (for sending emergency messages)
- Phone (for making calls)
- Camera (for flashlight)
- Vibrate
- Foreground Service
- Wake Lock
- Receive Boot Completed

## Project Structure
```
app/
├── src/
│   ├── main/
│   │   ├── java/com/example/emerband/
│   │   │   ├── data/                 # Database related classes
│   │   │   │   ├── AppDatabase.java
│   │   │   │   ├── OfflineEvent.java
│   │   │   │   └── OfflineEventDao.java
│   │   │   ├── offline/              # Offline mode handling
│   │   │   │   └── OfflineModeManager.java
│   │   │   ├── utils/                # Utility classes
│   │   │   │   ├── ConnectivityUtils.java
│   │   │   │   ├── PermissionManager.java
│   │   │   │   ├── ResourceManager.java
│   │   │   │   └── TestingUtils.java
│   │   │   ├── AlertActivity.java    # Alert mode UI
│   │   │   ├── BLEBackgroundService.java # Main BLE service
│   │   │   ├── BLEGattUtils.java
│   │   │   ├── BootCompletedReceiver.java
│   │   │   ├── CyberCellHandler.kt
│   │   │   ├── CyberCellSettingsActivity.java
│   │   │   ├── EmergencyHandler.kt
│   │   │   ├── FakeCallActivity.java
│   │   │   ├── LauncherActivity.java
│   │   │   ├── MainActivity.java
│   │   │   └── SetupActivity.java    # First-run setup
│   │   ├── assets/
│   │   │   ├── arduino/              # Arduino smartwatch code and instructions
│   │   │   │   ├── EmerbandWatch.ino # Complete Arduino sketch
│   │   │   │   ├── README.md         # Setup and programming instructions
│   │   │   │   └── enclosure/        # 3D printable watch enclosure files
│   │   └── res/                      # Android resources
│   └── androidTest/                  # Instrumented tests
└── build.gradle                      # App build configuration
```

## Future Enhancements
- User-configurable emergency contacts
- Dynamic SMS templates
- Multi-language support
- Background execution optimizations
- Integration with wearables and Android Auto
- Cloud backup of settings

## Contributing
Contributions are welcome. Please feel free to submit a Pull Request.

## License
This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments
- Arduino community for smartwatch support
- Android developer documentation
- Room persistence library documentation 