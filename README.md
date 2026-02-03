# Embedded & IoT Light Monitoring System

This project implements a complete embedded and IoT system for automatic light control
based on ambient light intensity.

The system is designed with a clear layered architecture, separating local control,
communication, cloud services, and visualization.

---

## System Overview

**Embedded Node**
- MCU: STM32
- Light Sensor: TSL2561 (I2C)
- Actuator: LED
- Communication: UART to BLE (HM-10)

**Gateway**
- Android smartphone
- BLE connection to embedded node
- WiFi connection to Firebase Realtime Database

**Cloud & Visualization**
- Firebase Realtime Database
- Web dashboard (HTML + JavaScript + Chart.js)

---

## Key Features

- Automatic LED control based on ambient light
- Local autonomous operation without Internet
- BLE data transmission to Android gateway
- Real-time cloud synchronization
- Web-based monitoring with real-time chart

---

## System Architecture

STM32 (I2C, GPIO)
→ BLE HM-10 (UART)
→ Android Gateway (BLE → WiFi)
→ Firebase Realtime Database
→ Web Dashboard

---

## Project Structure

embedded-iot-light-system/
├── stm32_firmware/     # STM32 source code
├── android_gateway/    # Android BLE to Firebase gateway
├── web_dashboard/      # Web-based monitoring dashboard
├── docs/               # Report and diagrams
└── README.md

---

## How to Run

1. Flash STM32 firmware from `stm32_firmware`
2. Power the STM32 board and HM-10 module
3. Launch Android gateway app and connect via BLE
4. Verify data updates in Firebase Realtime Database
5. Open `web_dashboard/index.html` to monitor data

---

## Technologies Used

- STM32 HAL
- I2C, UART, BLE
- Android (Kotlin)
- Firebase Realtime Database
- HTML, JavaScript, Chart.js

---

## Author

This project was developed as part of an Embedded Systems & IoT course  
and follows real-world IoT architecture principles.

**Authors:**  
- Nguyen Viet Cong  
- Bui Minh Tri  

University of Engineering and Technology (UET),  
Vietnam National University, Hanoi (VNU)

---

## Interview description

Embedded & IoT Light Monitoring System
Developed an embedded system using STM32 and TSL2561 sensor to automatically control LED lighting based on ambient light.
Implemented BLE communication via HM-10 and an Android gateway to transmit data to Firebase Realtime Database.
Built a web dashboard for real-time monitoring and visualization using JavaScript and Chart.js.
