import sqlite3
import paho.mqtt.client as mqtt
import time
import ssl
import logging

logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")
logger = logging.getLogger(__name__)

# Path to your database file
db_path = 'DB/database.db'

# Database setup and table creation
def init_db():
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute('''
        CREATE TABLE IF NOT EXISTS devices (
            mac TEXT PRIMARY KEY,         
            alias TEXT,                    
            status TEXT,                  
            position INTEGER,             
            setting INTEGER,              
            time_up TEXT,                 
            time_down TEXT                
        )
    ''')
    conn.commit()
    logger.info("Database and table created successfully.")
    conn.close()

# Insert or update device status in the database
def update_device(mac, alias=None, status=None, position=None, setting=None):
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute('''
        INSERT INTO devices (mac, alias, status, position, setting) 
        VALUES (?, ?, ?, ?, ?) 
        ON CONFLICT(mac) DO UPDATE SET
        alias=IFNULL(?, alias),
        status=COALESCE(?, status),
        position=COALESCE(?, position),
        setting=COALESCE(?, setting)
    ''', (mac, alias, status, position, setting, alias, status, position, setting))
    conn.commit()
    
    # Log current state of the device
    cursor.execute("SELECT * FROM devices WHERE mac = ?", (mac,))
    device_data = cursor.fetchone()
    logger.info(f"Updated device in DB: {device_data}")
    conn.close()

# Retrieve all device MAC addresses and aliases from the database
def get_all_devices():
    conn = sqlite3.connect(db_path)
    cursor = conn.cursor()
    cursor.execute("SELECT mac, alias FROM devices")
    devices = cursor.fetchall()
    logger.info(f"Retrieved devices from DB: {devices}")
    conn.close()
    return devices

# MQTT Callbacks
def on_connect(client, userdata, flags, rc, properties=None):
    logger.info(f"Connected with result code {rc}")
    client.subscribe("msgHandler/android/ping")
    client.subscribe("msgHandler/ping/response")

def on_message(client, userdata, msg):
    topic = msg.topic
    message_content = msg.payload.decode()
    topic_parts = topic.split('/')

    logger.info(f"Received message on topic '{topic}' with content: '{message_content}'")

    if topic == "msgHandler/android/ping" and message_content == "hello":
        client.publish("device/ping", "hello", retain=True)
        logger.info("Sent 'hello' to device/ping")

    elif topic == "msgHandler/ping/response":
        mac = message_content
        update_device(mac=mac, status="online")
        logger.info(f"Stored new device '{mac}' with status 'online' in DB")
        
        for device_mac, alias in get_all_devices():
            client.publish("android/devices", device_mac)
            if alias:
                logger.info(f"Publishing alias '{alias}' for device '{device_mac}' to android/alias/{device_mac}")
                client.publish(f"android/alias/{device_mac}", alias, retain = True)
            # Dynamically subscribe to command and alias topics for each device
            client.subscribe(f"msgHandler/{device_mac}/alias")
            client.subscribe(f"msgHandler/command/{device_mac}")
        logger.info(f"Published MAC addresses to android/devices for '{mac}'")

    elif len(topic_parts) == 3 and topic_parts[2] == "alias":
        mac = topic_parts[1]
        alias = message_content
        update_device(mac=mac, alias=alias)
        logger.info(f"Updated alias for {mac} to {alias}")

    elif len(topic_parts) == 3 and topic_parts[1] == "command":
        mac = topic_parts[2]
        command = message_content.lower()
        position = 100 if command == "up" else 0
        motor_command = "CW" if command == "up" else "CCW"
        
        update_device(mac=mac, position=position)
        client.publish(f"device/{mac}", motor_command)
        logger.info(f"Sent '{motor_command}' command to device/{mac} and updated position to {position}")

# Initialize MQTT Client with mTLS
client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2)
client.on_connect = on_connect
client.on_message = on_message

# Configure TLS/SSL for mTLS
CA_CERT = "/app/certs/ca.crt"
CLIENT_CERT = "/app/certs/client.crt"
CLIENT_KEY = "/app/certs/client.key"
client.tls_set(
    ca_certs=CA_CERT,
    certfile=CLIENT_CERT,
    keyfile=CLIENT_KEY,
    tls_version=ssl.PROTOCOL_TLSv1_2
)
client.tls_insecure_set(False)

# Connect to the broker
BROKER_ADDRESS = "mqtt-broker"
BROKER_PORT = 8883
client.connect(BROKER_ADDRESS, BROKER_PORT, 60)

# Initialize the database and table
init_db()

# Start the loop
client.loop_start()
try:
    while True:
        time.sleep(1)
except KeyboardInterrupt:
    logger.info("Disconnecting...")
    client.loop_stop()
    client.disconnect()
