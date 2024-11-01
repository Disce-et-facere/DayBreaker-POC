# Daybreaker-POC Backend

This backend is part of the **Daybreaker-POC App**, responsible for managing MQTT messages and handling secure communication between devices and the app. Follow these instructions to set up and run the backend.

## Project Setup Instructions

### 1. Download the Project

You have two options for downloading:

1. **Download the Entire Repository**:
   - Clone the repository:
     ```bash
     git clone https://github.com/Disce-et-facere/DayBreaker-POC.git
     ```
   - Navigate to the `backend` directory:
     ```bash
     cd backend
     ```

2. **Download Only the `backend` Folder**:
   - Open the repository in GitHub.
   - Navigate to the `backend` folder.
   - Click the **Download** button or use the **Download ZIP** option to download just the `backend` folder.

### 2. Replace Certificates

To ensure secure MQTT communication, replace the default certificates with your own:

1. Place your own certificates in the following directories:
   - **MQTT Broker**: Replace certificates in `mqtt-broker/certs`
   - **Message Handler**: Replace certificates in `msg-handler/certs`

2. Ensure the certificate filenames match those referenced in the `docker-compose.yml` file.

### 3. Start the Backend

1. In the terminal, navigate to the `backend` folder if you're not already there.
2. Run the following command to start the backend services:
   ```bash
   docker-compose up
