# DayBreaker-POC Firmware

## Functions (demo)
Current motor functions allows for:
* Turning the stepper motor 360 degrees clock wise
* Turning the stepper motor 360 degrees counter clock wise

## Demo
https://1drv.ms/v/c/ffe5b772452c545d/EWslNbNru8NPnSGm_VzsCgQBeZ3ohXRZaGwq2AVIbaq3ZA?e=fEFDlm

# ESP32-WROVER-E Project Setup in VS Code with ESP-IDF

This guide provides steps to set up and run the ESP32-WROVER-E project in Visual Studio Code (VS Code) using the ESP-IDF extension. This assumes that the project folder(esp32-wrover-E) has been downloaded and is running in its own esp-idf project.

## Prerequisites

1. **VS Code** - [Download and install VS Code](https://code.visualstudio.com/download).
2. **ESP-IDF Extension for VS Code** - [Installation guide](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/get-started/vscode-setup.html).
3. **ESP-IDF** - ESP-IDF version 5.x or later. You can install ESP-IDF through the VS Code extension or by following [this guide](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/get-started/).
4. **Git** - (Optional) If you need to pull updates to the larger repository.

## Project Setup

### 1. Open the Project in VS Code

1. Open VS Code.
2. Select **File > Open Folder...** and navigate to the folder containing the ESP32 project (e.g., `ESP32-WROVER-E-Project`).
3. Open the folder to load the project into VS Code.

### 2. Configure ESP-IDF in VS Code

The ESP-IDF extension should automatically detect the `sdkconfig` file in the project directory. If not, follow these steps:

1. Open the **ESP-IDF: Configure ESP-IDF** command from the Command Palette (use `Ctrl+Shift+P` or `Cmd+Shift+P` on Mac).
2. Set up the paths to your ESP-IDF, Python, and toolchain if prompted.
3. Select **ESP-IDF: Set Espressif device target** from the Command Palette and choose `esp32` as the target.

### 3. Project Configuration

Open the ESP-IDF configuration menu to customize the project’s settings:

1. Run the following command in the VS Code terminal:

    ```bash
    idf.py menuconfig
    ```

2. **Required Settings:**
   - **Wi-Fi Configuration:** Go to `Component config → Wi-Fi` and verify your Wi-Fi settings.
   - **MQTT Configuration (if using SSL/TLS):** Go to `Component config → MQTT` and adjust for secure connections if required.

3. **Optional Settings:**
   - **Partition Table:** Set to `Single factory app, no OTA` for simpler projects.
   - **Flash Settings:** Ensure `SPI Flash Size` is set to 4MB (or 2MB if using a smaller flash size).

4. Save and exit the configuration.

### 4. Build and Flash the Project

With your ESP32-WROVER-E connected to your computer:

1. Build the project by running the following command in the terminal:

    ```bash
    idf.py build
    ```

2. Flash the project to the ESP32:

    ```bash
    idf.py flash
    ```

3. Monitor the device output:

    ```bash
    idf.py monitor
    ```

Alternatively, use the ESP-IDF extension’s **Build**, **Flash**, and **Monitor** buttons in the VS Code status bar.

### 5. Initializing Wi-Fi and MQTT

- **Wi-Fi:** The project is configured to automatically connect to a Wi-Fi network based on settings in `wifi_manager.c`.
- **MQTT:** The project initializes an MQTT client and connects to the specified broker using settings in `mqtt_manager.c`.

Check `main.c`, `wifi_manager.c`, and `mqtt_manager.c` for detailed implementation.

## Troubleshooting

If you encounter issues:

- Verify that your **ESP-IDF** path and **toolchain** are correctly set in VS Code settings.
- Check the **terminal output** for any errors related to configuration or flashing.
- Refer to the [ESP-IDF troubleshooting guide](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/get-started/troubleshooting.html) for common issues.

## Resources

- [ESP32-WROVER-E Documentation](https://www.espressif.com/en/products/devkits/esp-wrover-kit/overview)
- [ESP-IDF Documentation](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/)
- [VS Code ESP-IDF Extension Guide](https://docs.espressif.com/projects/esp-idf/en/latest/esp32/get-started/vscode-setup.html)

This setup guide should help you start developing on the ESP32-WROVER-E using VS Code and ESP-IDF.
