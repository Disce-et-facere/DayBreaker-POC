# Use the official Eclipse Mosquitto image
FROM eclipse-mosquitto:latest

# Create a directory for certificates
RUN mkdir -p /mosquitto/certs

# Copy certificates into the container
COPY certs/ca.crt /mosquitto/certs/
COPY certs/mqttbroker.crt /mosquitto/certs/
COPY certs/mqttbroker.key /mosquitto/certs/

# Copy the configuration file into the container
COPY config/mosquitto.conf /mosquitto/config/mosquitto.conf

# Expose port 8883 for MQTT with TLS
EXPOSE 8883
