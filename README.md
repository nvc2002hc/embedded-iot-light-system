# embedded-iot-light-system

# Embedded & IoT Light Monitoring System

Hệ thống nhúng và IoT điều khiển đèn LED tự động dựa trên ánh sáng môi trường,
sử dụng STM32 + TSL2561 + BLE HM-10 + Android Gateway + Firebase + Web Dashboard.

## Kiến trúc hệ thống
- STM32: đo ánh sáng, điều khiển LED tự động
- HM-10: truyền dữ liệu BLE
- Android: Gateway BLE → Firebase
- Firebase: Cloud Realtime Database
- Web App: Giám sát realtime + biểu đồ

## Thư mục
- stm32_firmware: source code STM32
- android_gateway: ứng dụng Android Gateway
- web_dashboard: giao diện web
- docs: báo cáo và hình ảnh

## Cách chạy hệ thống (Tóm tắt)

1. Nạp firmware STM32 (thư mục stm32_firmware)
2. Cấp nguồn cho STM32 + HM-10
3. Mở Android Gateway → kết nối BLE
4. Kiểm tra dữ liệu trên Firebase
5. Mở web_dashboard/index.html để giám sát