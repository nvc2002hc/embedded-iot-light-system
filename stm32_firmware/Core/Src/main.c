/* =========================================================
 *  AUTO LIGHT CONTROL SYSTEM
 *  MCU    : STM32F103
 *  SENSOR : TSL2561 (I2C)
 *  OUTPUT : LED (PC13)
 *  COMM   : UART1 -> HM-10 BLE
 *
 *  CORE IDEA:
 *  - STM32 xử lý hoàn toàn AUTO tại local
 *  - BLE chỉ để truyền dữ liệu
 * =========================================================
 */

#include "stm32f1xx_hal.h"
#include <string.h>
#include <stdio.h>

/* ================= HANDLE ================= */
I2C_HandleTypeDef hi2c1;
UART_HandleTypeDef huart1;

/* ================= SYSTEM VAR ================= */
uint16_t lux_value = 0;
uint8_t led_state = 0;
char uart_tx[64];

/* ================= TSL2561 ================= */
#define TSL2561_ADDR      (0x39 << 1)
#define TSL2561_CMD       0x80
#define TSL2561_CONTROL   0x00
#define TSL2561_DATA0LOW  0x0C

/* ================= PROTOTYPE ================= */
void SystemClock_Config(void);
static void MX_GPIO_Init(void);
static void MX_I2C1_Init(void);
static void MX_USART1_UART_Init(void);

void TSL2561_Init(void);
uint16_t TSL2561_Read(void);
void Auto_LED(uint16_t lux);
void Send_UART(uint16_t lux, uint8_t led);

/* ================= MAIN ================= */
int main(void)
{
    HAL_Init();
    SystemClock_Config();

    MX_GPIO_Init();
    MX_I2C1_Init();
    MX_USART1_UART_Init();

    TSL2561_Init();

    while (1)
    {
        lux_value = TSL2561_Read();
        Auto_LED(lux_value);
        Send_UART(lux_value, led_state);
        HAL_Delay(1000);
    }
}

/* ================= TSL2561 INIT ================= */
void TSL2561_Init(void)
{
    uint8_t cmd[2];
    cmd[0] = TSL2561_CMD | TSL2561_CONTROL;
    cmd[1] = 0x03; // Power ON
    HAL_I2C_Master_Transmit(&hi2c1, TSL2561_ADDR, cmd, 2, 100);
}

/* ================= READ LIGHT ================= */
uint16_t TSL2561_Read(void)
{
    uint8_t reg = TSL2561_CMD | TSL2561_DATA0LOW;
    uint8_t data[2];

    HAL_I2C_Master_Transmit(&hi2c1, TSL2561_ADDR, &reg, 1, 100);
    HAL_I2C_Master_Receive(&hi2c1, TSL2561_ADDR, data, 2, 100);

    return (data[1] << 8) | data[0];
}

/* ================= AUTO CONTROL ================= */
void Auto_LED(uint16_t lux)
{
    if (lux < 200)
    {
        HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_RESET);
        led_state = 1;
    }
    else
    {
        HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_SET);
        led_state = 0;
    }
}

/* ================= UART SEND ================= */
void Send_UART(uint16_t lux, uint8_t led)
{
    sprintf(uart_tx, "LUX:%d,LED:%d\r\n", lux, led);
    HAL_UART_Transmit(&huart1, (uint8_t*)uart_tx, strlen(uart_tx), 100);
}

/* ================= GPIO ================= */
static void MX_GPIO_Init(void)
{
    GPIO_InitTypeDef GPIO_InitStruct = {0};
    __HAL_RCC_GPIOC_CLK_ENABLE();

    GPIO_InitStruct.Pin = GPIO_PIN_13;
    GPIO_InitStruct.Mode = GPIO_MODE_OUTPUT_PP;
    GPIO_InitStruct.Speed = GPIO_SPEED_FREQ_LOW;
    HAL_GPIO_Init(GPIOC, &GPIO_InitStruct);

    HAL_GPIO_WritePin(GPIOC, GPIO_PIN_13, GPIO_PIN_SET);
}

/* ================= I2C ================= */
static void MX_I2C1_Init(void)
{
    hi2c1.Instance = I2C1;
    hi2c1.Init.ClockSpeed = 100000;
    hi2c1.Init.AddressingMode = I2C_ADDRESSINGMODE_7BIT;
    hi2c1.Init.DutyCycle = I2C_DUTYCYCLE_2;
    HAL_I2C_Init(&hi2c1);
}

/* ================= UART ================= */
static void MX_USART1_UART_Init(void)
{
    huart1.Instance = USART1;
    huart1.Init.BaudRate = 9600;
    huart1.Init.WordLength = UART_WORDLENGTH_8B;
    huart1.Init.StopBits = UART_STOPBITS_1;
    huart1.Init.Parity = UART_PARITY_NONE;
    huart1.Init.Mode = UART_MODE_TX_RX;
    HAL_UART_Init(&huart1);
}