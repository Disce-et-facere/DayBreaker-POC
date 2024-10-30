#include "driver/gpio.h"
#include "freertos/FreeRTOS.h"
#include "freertos/task.h"
#include "esp_mac.h"
#include "esp_log.h"
#include "esp_err.h"
#include "esp_system.h"

#define PIN_IN1 GPIO_NUM_21  // Define pins for motor control
#define PIN_IN2 GPIO_NUM_22
#define PIN_IN3 GPIO_NUM_23
#define PIN_IN4 GPIO_NUM_19

#define DELAY_MS 10           // Delay between steps, adjust for speed

static const char *TAG = "MOTOR";

// Step sequence for 28BYJ-48 stepper motor (4-phase)
static const int step_sequence[8][4] = {
    {1, 0, 0, 0},
    {1, 1, 0, 0},
    {0, 1, 0, 0},
    {0, 1, 1, 0},
    {0, 0, 1, 0},
    {0, 0, 1, 1},
    {0, 0, 0, 1},
    {1, 0, 0, 1}
};

void test_motor_pins() {
    gpio_set_level(PIN_IN1, 1);
    vTaskDelay(pdMS_TO_TICKS(500));
    gpio_set_level(PIN_IN1, 0);

    gpio_set_level(PIN_IN2, 1);
    vTaskDelay(pdMS_TO_TICKS(500));
    gpio_set_level(PIN_IN2, 0);

    gpio_set_level(PIN_IN3, 1);
    vTaskDelay(pdMS_TO_TICKS(500));
    gpio_set_level(PIN_IN3, 0);

    gpio_set_level(PIN_IN4, 1);
    vTaskDelay(pdMS_TO_TICKS(500));
    gpio_set_level(PIN_IN4, 0);
}

// Initialize GPIOs
void motor_init() {
    gpio_set_direction(PIN_IN1, GPIO_MODE_OUTPUT);
    gpio_set_direction(PIN_IN2, GPIO_MODE_OUTPUT);
    gpio_set_direction(PIN_IN3, GPIO_MODE_OUTPUT);
    gpio_set_direction(PIN_IN4, GPIO_MODE_OUTPUT);
}

// Function to rotate motor by 360 degrees (clockwise)
void rotate_360_clockwise() {
    ESP_LOGI(TAG, "Starting clockwise rotation"); // Add log here
    int steps_per_revolution = 512;  // 512 steps for one 360 degree rotation

    for (int i = 0; i < steps_per_revolution; i++) {
        for (int j = 0; j < 8; j++) {
            gpio_set_level(PIN_IN1, step_sequence[j][0]);
            gpio_set_level(PIN_IN2, step_sequence[j][1]);
            gpio_set_level(PIN_IN3, step_sequence[j][2]);
            gpio_set_level(PIN_IN4, step_sequence[j][3]);
            vTaskDelay(pdMS_TO_TICKS(DELAY_MS));
        }
    }
}

// Function to rotate motor by 360 degrees (counterclockwise)
void rotate_360_counterclockwise() {
    ESP_LOGI(TAG, "Starting counterclockwise rotation");
    int steps_per_revolution = 512;  // 512 steps for one 360 degree rotation

    for (int i = 0; i < steps_per_revolution; i++) {
        for (int j = 7; j >= 0; j--) {
            gpio_set_level(PIN_IN1, step_sequence[j][0]);
            gpio_set_level(PIN_IN2, step_sequence[j][1]);
            gpio_set_level(PIN_IN3, step_sequence[j][2]);
            gpio_set_level(PIN_IN4, step_sequence[j][3]);
            vTaskDelay(pdMS_TO_TICKS(DELAY_MS));
        }
    }
}
