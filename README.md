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

### Building the Project
The project uses Gradle 8.10 for building. Key Gradle files include:

- `build.gradle` (Project level) - Contains project-wide build settings (using AGP 8.2.0)
- `app/build.gradle` - Contains app-specific dependencies and settings 
- `settings.gradle` - Defines which modules to include in the build
- `gradle.properties` - Contains properties for the Gradle build system
- `gradle/wrapper/gradle-wrapper.properties` - Specifies Gradle 8.10

#### Using Gradle Files

1. **First-Time Setup**:
   After cloning the repository, Android Studio should automatically recognize the Gradle files and offer to sync. If not, you can manually trigger a sync:
   - Click "Sync Now" in the notification bar
   - Or select File > Sync Project with Gradle Files

2. **Command Line Build**:
   ```bash
   # Build the debug APK
   ./gradlew assembleDebug

   # Install the app on a connected device
   ./gradlew installDebug

   # Run tests
   ./gradlew test
   ```

3. **Using the Gradle Wrapper**:
   The project uses the Gradle Wrapper (`gradlew`/`gradlew.bat`), which automatically downloads the correct Gradle version. You don't need to install Gradle separately.
   
   - On Linux/Mac: `./gradlew [task]`
   - On Windows: `gradlew.bat [task]`

4. **Gradle Tasks**:
   View available tasks:
   ```
   ./gradlew tasks
   ```

#### Gradle Version Information

This project uses:
- **Gradle**: 8.10
- **Android Gradle Plugin**: 8.2.0 
- **Kotlin**: 1.9.0
- **Java Compatibility**: Java 17

If you need to downgrade to an older Gradle version:

1. **Update the Gradle Wrapper Version**:
   - Edit `gradle/wrapper/gradle-wrapper.properties`:
     ```properties
     # Change from
     distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
     
     # To an older version (e.g.)
     distributionUrl=https\://services.gradle.org/distributions/gradle-7.3.3-bin.zip
     ```

2. **Update the Android Gradle Plugin** in the project-level `build.gradle`:
   - Also update the app-level build.gradle to use older syntax
   - Restore older compileOptions and kotlinOptions

3. **Compatibility Guidelines**:
   | Gradle Version | Android Gradle Plugin | Java Compatibility |
   |----------------|------------------------|-------------------|
   | 7.3 - 7.5      | 7.2.x - 7.4.x         | Java 8            |
   | 7.5 - 8.0      | 7.4.x - 8.0.x         | Java 11           |
   | 8.0 - 8.5      | 8.0.x - 8.1.x         | Java 11 or 17     |
   | 8.5+           | 8.2.x+                | Java 17           |

#### Troubleshooting Gradle Issues

1. **Gradle Sync Failed**:
   - Check your internet connection (Gradle needs to download dependencies)
   - Look at the error message in Android Studio's "Build" tab
   - Try File > Invalidate Caches / Restart
   - Make sure Java 17 is installed and properly configured

2. **Build Failed**:
   - Check for compilation errors in the code
   - Run `./gradlew clean` to clean the build, then try again
   - Check that your Android SDK is properly installed and the correct version is selected

3. **"GRADLE_USER_HOME is unknown" Error**:
   If you see an error like: `cannot install gradle distribution from https\://services.gradle.org/distributions/gradle-8.10-bin.zip, reason: java.lang.runtimeexception: Base: GRADLE_USER_HOME is unknown`, try these solutions:

   - **Set GRADLE_USER_HOME environment variable manually**:
     - On Linux/Mac:
       ```bash
       export GRADLE_USER_HOME=$HOME/.gradle
       ```
     - On Windows (Command Prompt):
       ```
       set GRADLE_USER_HOME=%USERPROFILE%\.gradle
       ```
     - On Windows (PowerShell):
       ```
       $env:GRADLE_USER_HOME="$env:USERPROFILE\.gradle"
       ```

   - **Verify Java installation**:
     ```bash
     java -version
     ```
     Make sure Java 17 is installed (required for Gradle 8.10)

   - **Create Gradle folder manually**:
     - On Linux/Mac:
       ```bash
       mkdir -p $HOME/.gradle
       ```
     - On Windows:
       ```
       mkdir %USERPROFILE%\.gradle
       ```

   - **Fix Gradle wrapper**:
     If the wrapper is corrupted, regenerate it:
     ```bash
     touch gradle/wrapper/gradle-wrapper.properties
     ```
     And ensure it contains:
     ```
     distributionBase=GRADLE_USER_HOME
     distributionUrl=https\://services.gradle.org/distributions/gradle-8.10-bin.zip
     distributionPath=wrapper/dists
     zipStorePath=wrapper/dists
     zipStoreBase=GRADLE_USER_HOME
     ```

4. **Dependency Resolution Issues**:
   - If a specific dependency is failing to download, check that it exists and is spelled correctly
   - Try adding Google's Maven repository if Google dependencies are failing:
     ```groovy
     repositories {
         google()  // Make sure this is included
         mavenCentral()
     }
     ```

5. **Out of Memory Errors**:
   - Increase Gradle's memory allocation in `gradle.properties`:
     ```
     org.gradle.jvmargs=-Xmx3g -XX:MaxPermSize=512m
     ```

6. **Slow Builds**:
   - Enable Gradle caching and parallel builds in `gradle.properties`:
     ```
     org.gradle.caching=true
     org.gradle.parallel=true
     ```

#### Advanced Gradle Troubleshooting

If you're still experiencing the `GRADLE_USER_HOME is unknown` error or other persistent Gradle issues after trying the solutions above, try these more advanced approaches:

1. **Create Gradle Wrapper Scripts Manually**:
   - Create a new `gradlew` file in the project root:
     ```bash
     # On Linux/Mac
     echo '#!/bin/sh
     exec java -Dorg.gradle.appname=gradlew -classpath `dirname $0`/gradle/wrapper/gradle-wrapper.jar org.gradle.wrapper.GradleWrapperMain "$@"' > gradlew
     chmod +x gradlew
     
     # On Windows, create gradlew.bat:
     echo @rem Gradle startup script for Windows > gradlew.bat
     echo @rem Add the rest of the bat file content... >> gradlew.bat
     ```

2. **Direct Gradle Distribution Download**:
   - Download Gradle 8.10 manually from https://services.gradle.org/distributions/gradle-8.10-bin.zip
   - Unzip it to a directory on your machine
   - Add the bin directory to your PATH
   - Run gradle commands directly instead of using the wrapper

3. **Check System Environment Variables**:
   - Make sure no conflicting environment variables exist:
     ```bash
     # On Linux/Mac
     env | grep GRADLE
     
     # On Windows
     set | findstr GRADLE
     ```
   - Look for any unexpected GRADLE_* variables that might be causing conflicts

4. **Verify File Permissions**:
   - Ensure your user has write permissions to:
     - The project directory
     - Your home directory
     - The .gradle directory

5. **Clean Environment Completely**:
   ```bash
   # Delete all Gradle caches and temp files
   rm -rf ~/.gradle/caches/
   rm -rf ~/.gradle/wrapper/
   rm -rf ~/.gradle/daemon/
   rm -rf <project>/.gradle
   ```

6. **Try a Different Java Version**:
   - Gradle 8.10 requires Java 17
   - Ensure you're using the correct JDK version:
     ```bash
     # Set JAVA_HOME to a Java 17 installation
     export JAVA_HOME=/path/to/java17
     ```

7. **Use Local Properties Approach**:
   - Create a `local.properties` file in the project root:
     ```
     sdk.dir=/path/to/your/Android/sdk
     ```
   - Create a gradle.properties with explicit paths:
     ```
     org.gradle.java.home=/path/to/java
     ```

8. **IDE-Independent Build**:
   - Try building from the command line in a clean terminal session
   - Ensure the terminal has the correct environment variables set

9. **Network/Proxy Issues**:
   - If behind a corporate proxy, set proxy settings:
     ```
     # In gradle.properties
     systemProp.http.proxyHost=proxy.company.com
     systemProp.http.proxyPort=8080
     systemProp.https.proxyHost=proxy.company.com
     systemProp.https.proxyPort=8080
     ```

If all else fails, you might need to:
1. Create a new Android project from scratch in Android Studio
2. Copy your source files over to the new project
3. Configure the new project with the same dependencies

### Key Dependencies
- **Room** - For local database storage and offline mode
- **Nordic BLE Library** - For Bluetooth Low Energy features
- **AndroidX Components** - Core Android architecture components
- **Work Manager** - For background processing
- **PermissionX** - For streamlined permission handling

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
├── build.gradle              # App-specific Gradle configuration
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
build.gradle                 # Project-level Gradle configuration
gradle.properties            # Gradle properties
settings.gradle              # Project settings
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