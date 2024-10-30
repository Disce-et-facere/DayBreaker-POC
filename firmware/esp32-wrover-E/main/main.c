#include "wifi_manager.h"
#include "mqtt_manager.h"
#include "esp_log.h"
#include "nvs_flash.h"

static const char *TAG = "MAIN";

void app_main() {
    // Initialize NVS
    esp_err_t ret = nvs_flash_init();
    if (ret == ESP_ERR_NVS_NO_FREE_PAGES || ret == ESP_ERR_NVS_NEW_VERSION_FOUND) {
        ESP_ERROR_CHECK(nvs_flash_erase());
        ret = nvs_flash_init();
    }
    ESP_ERROR_CHECK(ret);

    // Initialize Wi-Fi
    ESP_LOGI(TAG, "Starting Wi-Fi...");
    wifi_init_sta();

    // Initialize MQTT after Wi-Fi is connected
    ESP_LOGI(TAG, "Starting MQTT...");
    mqtt_app_start();
}