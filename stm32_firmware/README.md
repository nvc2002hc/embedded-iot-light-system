# STM32 Firmware

Chức năng:
- Đọc cảm biến ánh sáng TSL2561 (I2C)
- Điều khiển LED tự động theo ngưỡng lux
- Gửi dữ liệu qua UART cho HM-10

Hệ thống hoạt động độc lập, không phụ thuộc IoT.

## Test nhanh
- Che cảm biến → LED bật
- Chiếu sáng → LED tắt
- Không cần BLE hay Internet