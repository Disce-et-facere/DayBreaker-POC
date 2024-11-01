# Daybreaker-POC Android App

## Functions (demo)
Current functions allows for:
* Pingin backend to recieve connected devices mac address and alias(if it's stored in DB)
* Set Alias for devices(ex Kitchen) in app and send alias to backend for storage 
* Open Drape action call
* Close Drape action call

![android1](https://github.com/user-attachments/assets/7fa12337-0910-4856-903e-ec3b5698d01f)
![android2](https://github.com/user-attachments/assets/72b0e254-120c-4b22-a6d9-886e98a9bb0a)

## Project Setup Instructions

Follow these steps to set up the app in Android Studio:

### 1. Download the `mobile_app` Folder

To get started, **download only the `mobile_app` folder**. This folder contains all the necessary files to run the app without downloading the entire repository.

1. Open the repository on GitHub.
2. Navigate to the `mobile_app` folder.
3. Click the **Download** button or use the **Download ZIP** option from GitHub's interface.

### 2. Open the Project in Android Studio

1. Launch **Android Studio**.
2. Select **Open an Existing Project**.
3. Navigate to the location of the downloaded `mobile_app` folder, and select it as the project folder.
4. Android Studio will index and sync the project files. You may need to wait a moment while dependencies load.

### 3. Configure Android SDK and Dependencies

Ensure you have the correct SDK version and dependencies installed:

1. **SDK Version**: Open the `build.gradle` file (Project level) to verify the required SDK version.
   - If necessary, adjust your SDK version in **File > Project Structure > Modules > app > Properties** to match the project’s settings.
2. **Dependencies**: Gradle should automatically handle dependency installations. If any errors appear, try:
   - Click **Sync Now** if prompted by Android Studio.
   - Go to **File > Sync Project with Gradle Files**.
   - If issues persist, check `build.gradle` for any specific library versions that may need updates.

### 4. Build and Run the App

1. Connect an Android device via USB or start an Android emulator.
2. Click on the **Run** button in Android Studio (green triangle) to build and install the app on your device/emulator.
3. Verify that the app launches successfully and that all controls (e.g., open/close buttons, scheduling, etc.) are functioning as expected.

### Additional Information

- **Permissions**: Ensure the app has necessary permissions, especially for network access if it connects to external devices.
- **MQTT Broker Setup**: If the app uses MQTT for device control, confirm your local or remote MQTT broker details are configured in the code.
- **Replace the certificates in res/raw to your own certificates
  
### Troubleshooting

If you encounter issues:
- Ensure all dependencies are installed and up-to-date.
- Restart Android Studio and re-sync the project if Gradle sync errors occur.
- For network errors, verify your device/emulator’s network settings and the MQTT broker’s configuration.

---

Feel free to reach out if you encounter any issues or have questions. Happy coding!
