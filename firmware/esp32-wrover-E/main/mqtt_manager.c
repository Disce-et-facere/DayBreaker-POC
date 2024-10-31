#include "mqtt_manager.h"
#include "mqtt_client.h"
#include "motor_control.h"
#include "esp_log.h"
#include "esp_err.h"
#include "esp_system.h"
#include "esp_efuse.h"
#include "esp_mac.h"
#include <string.h>

extern const uint8_t client_cert_pem_start[] asm("_binary_client_crt_start");
extern const uint8_t client_cert_pem_end[] asm("_binary_client_crt_end");
extern const uint8_t client_key_pem_start[] asm("_binary_client_key_start");
extern const uint8_t client_key_pem_end[] asm("_binary_client_key_end");
extern const uint8_t ca_cert_pem_start[] asm("_binary_ca_crt_start");
extern const uint8_t ca_cert_pem_end[] asm("_binary_ca_crt_end");

#define MQTT_BROKER_URI "mqtts://your_ip:8883"

static uint8_t base_mac[6];
static char device_command[32]; 
static const char *ping_request = "device/ping";
static const char *return_ping = "msgHandler/ping/response";
static const char *TAG = "MQTT";

static void log_error_if_nonzero(const char *message, int error_code)
{
    if (error_code != 0) {
        ESP_LOGE(TAG, "Last error %s: 0x%x", message, error_code);
    }
}

static void mqtt_event_handler(void *handler_args, esp_event_base_t base, int32_t event_id, void *event_data)
{
    ESP_LOGD(TAG, "Event dispatched from event loop base=%s, event_id=%" PRIi32, base, event_id);
    esp_mqtt_event_handle_t event = event_data;
    esp_mqtt_client_handle_t client = event->client;
    int msg_id;
    switch ((esp_mqtt_event_id_t)event_id) {
    case MQTT_EVENT_CONNECTED:
        ESP_LOGI(TAG, "MQTT_EVENT_CONNECTED");
        msg_id = esp_mqtt_client_subscribe(client, ping_request, 0);
        ESP_LOGI(TAG, "sent subscribe successful, msg_id=%d", msg_id);

        msg_id = esp_mqtt_client_subscribe(client, device_command, 0);
        ESP_LOGI(TAG, "sent subscribe successful, msg_id=%d", msg_id);

        msg_id = esp_mqtt_client_subscribe(client, return_ping, 1);
        ESP_LOGI(TAG, "sent subscribe successful, msg_id=%d", msg_id);

        break;
    case MQTT_EVENT_DISCONNECTED:
        ESP_LOGI(TAG, "MQTT_EVENT_DISCONNECTED");
        break;

    case MQTT_EVENT_SUBSCRIBED:
        ESP_LOGI(TAG, "MQTT_EVENT_SUBSCRIBED, msg_id=%d", event->msg_id);
        //msg_id = esp_mqtt_client_publish(client, "test/topic", "data", 0, 0, 0);
        //ESP_LOGI(TAG, "sent publish successful, msg_id=%d", msg_id);
        break;
    case MQTT_EVENT_UNSUBSCRIBED:
        ESP_LOGI(TAG, "MQTT_EVENT_UNSUBSCRIBED, msg_id=%d", event->msg_id);
        break;
    case MQTT_EVENT_PUBLISHED:
        ESP_LOGI(TAG, "MQTT_EVENT_PUBLISHED, msg_id=%d", event->msg_id);
        break;
    case MQTT_EVENT_DATA:
        ESP_LOGI(TAG, "MQTT_EVENT_DATA");

        if (strncmp(event->topic, ping_request, event->topic_len) == 0) {
            ESP_LOGI(TAG, "Received ping request on %s", ping_request);

            if (strncmp(event->data, "hello", event->data_len) == 0) {
                char mac_response[18];
                snprintf(mac_response, sizeof(mac_response), "%02X%02X%02X%02X%02X%02X",
                        base_mac[0], base_mac[1], base_mac[2],
                        base_mac[3], base_mac[4], base_mac[5]);

                // Return mac addr when pinged
                int msg_id = esp_mqtt_client_publish(client, return_ping, mac_response, 0, 0, 0);
                ESP_LOGI(TAG, "Sent MAC address to topic %s, msg_id=%d", return_ping, msg_id);
            }
        } else if (strncmp(event->topic, device_command, event->topic_len) == 0) {
            ESP_LOGI(TAG, "Message received on device_command: %s", device_command);

            // Check if the message is "CW" or "CCW" and call the appropriate motor function
            if (event->data_len == 2 && strncmp(event->data, "CW", 2) == 0) {
                ESP_LOGI(TAG, "Starting clockwise rotation");
                rotate_360_clockwise();
            } else if (event->data_len == 3 && strncmp(event->data, "CCW", 3) == 0) {
                ESP_LOGI(TAG, "Starting counterclockwise rotation");
                rotate_360_counterclockwise();
            }
        }

        printf("TOPIC=%.*s\r\n", event->topic_len, event->topic);
        printf("DATA=%.*s\r\n", event->data_len, event->data);
        break;
    case MQTT_EVENT_ERROR:
        ESP_LOGI(TAG, "MQTT_EVENT_ERROR");
        if (event->error_handle->error_type == MQTT_ERROR_TYPE_TCP_TRANSPORT) {
            log_error_if_nonzero("reported from esp-tls", event->error_handle->esp_tls_last_esp_err);
            log_error_if_nonzero("reported from tls stack", event->error_handle->esp_tls_stack_err);
            log_error_if_nonzero("captured as transport's socket errno",  event->error_handle->esp_transport_sock_errno);
            ESP_LOGI(TAG, "Last errno string (%s)", strerror(event->error_handle->esp_transport_sock_errno));

        }
        break;
    default:
        ESP_LOGI(TAG, "Other event id:%d", event->event_id);
        break;
    }
}

void mqtt_app_start(void) {

    motor_init();

    esp_efuse_mac_get_default(base_mac);
    snprintf(device_command, sizeof(device_command), "device/%02X%02X%02X%02X%02X%02X",
             base_mac[0], base_mac[1], base_mac[2], base_mac[3], base_mac[4], base_mac[5]);

    const esp_mqtt_client_config_t mqtt_cfg = {
        .broker.address.uri = MQTT_BROKER_URI,
        .broker.verification.certificate = (const char *)ca_cert_pem_start,
        .credentials = {
        .authentication = {
            .certificate = (const char *)client_cert_pem_start,
            .key = (const char *)client_key_pem_start,
        },
        }
    };

    ESP_LOGI(TAG, "[APP] Free memory: %" PRIu32 " bytes", esp_get_free_heap_size());
    esp_mqtt_client_handle_t client = esp_mqtt_client_init(&mqtt_cfg);
    /* The last argument may be used to pass data to the event handler, in this example mqtt_event_handler */
    esp_mqtt_client_register_event(client, ESP_EVENT_ANY_ID, mqtt_event_handler, NULL);
    esp_mqtt_client_start(client);
}