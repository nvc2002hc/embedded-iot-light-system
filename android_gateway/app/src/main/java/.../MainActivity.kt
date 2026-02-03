package com.example.blegateway

import android.Manifest
import android.bluetooth.*
import android.bluetooth.le.*
import android.content.pm.PackageManager
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import java.nio.charset.Charset
import java.util.*

class MainActivity : AppCompatActivity() {

    /* ================== TAG LOG ================== */
    private val TAG = "BLE_GATEWAY"

    /* ================== BLE UUID HM-10 ================== */
    private val HM10_SERVICE_UUID =
        UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb")
    private val HM10_CHAR_UUID =
        UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb")

    /* ================== BLE ================== */
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var bleScanner: BluetoothLeScanner
    private var bluetoothGatt: BluetoothGatt? = null

    /* ================== FIREBASE ================== */
    private val dbRef =
        FirebaseDatabase.getInstance().getReference("devices/node_01")

    /* ================== LIFECYCLE ================== */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Không cần layout – gateway chạy nền
        setContentView(android.R.layout.simple_list_item_1)

        Log.d(TAG, "ANDROID GATEWAY BAT DAU")

        requestPermissions()
        initFirebase()
        initBLE()
    }

    /* ================== PERMISSION ================== */
    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        ActivityCompat.requestPermissions(this, permissions, 1)
    }

    /* ================== FIREBASE INIT ================== */
    private fun initFirebase() {
        FirebaseApp.initializeApp(this)

        FirebaseAuth.getInstance()
            .signInAnonymously()
            .addOnSuccessListener {
                Log.d(TAG, "Dang nhap Firebase Anonymous OK")
            }
            .addOnFailureListener {
                Log.e(TAG, "Firebase Auth Loi", it)
            }
    }

    /* ================== BLE INIT ================== */
    private fun initBLE() {
        val manager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = manager.adapter
        bleScanner = bluetoothAdapter.bluetoothLeScanner

        startScan()
    }

    /* ================== BLE SCAN ================== */
    private fun startScan() {
        Log.d(TAG, "Dang quet BLE...")

        val filter = ScanFilter.Builder().build()
        val settings = ScanSettings.Builder()
            .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        bleScanner.startScan(listOf(filter), settings, scanCallback)
    }

    /* ================== SCAN CALLBACK ================== */
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device

            // HM-10 thường có tên "HMSoft"
            if (device.name == "HMSoft") {
                Log.d(TAG, "Tim thay HM-10: ${device.address}")

                bleScanner.stopScan(this)
                connectToDevice(device)
            }
        }
    }

    /* ================== CONNECT BLE ================== */
    private fun connectToDevice(device: BluetoothDevice) {
        Log.d(TAG, "Dang ket noi BLE...")
        bluetoothGatt = device.connectGatt(this, false, gattCallback)
    }

    /* ================== GATT CALLBACK ================== */
    private val gattCallback = object : BluetoothGattCallback() {

        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Ket noi BLE thanh cong")
                gatt.discoverServices()
            } else {
                Log.e(TAG, "Mat ket noi BLE")
            }
        }

        override fun onServicesDiscovered(
            gatt: BluetoothGatt,
            status: Int
        ) {
            val service = gatt.getService(HM10_SERVICE_UUID)
            val characteristic = service.getCharacteristic(HM10_CHAR_UUID)

            gatt.setCharacteristicNotification(characteristic, true)

            val descriptor = characteristic.getDescriptor(
                UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
            )
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            gatt.writeDescriptor(descriptor)

            Log.d(TAG, "Dang nhan du lieu tu STM32...")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic
        ) {
            val rawData =
                characteristic.value.toString(Charset.forName("UTF-8"))

            Log.d(TAG, "Nhan BLE: $rawData")
            processData(rawData)
        }
    }

    /* ================== PARSE DATA ================== */
    private fun processData(data: String) {
        // Ví dụ: LUX:123,LED:1
        try {
            val parts = data.trim().split(",")

            val lux =
                parts[0].split(":")[1].toInt()
            val led =
                parts[1].split(":")[1].toInt()

            sendToFirebase(lux, led)

        } catch (e: Exception) {
            Log.e(TAG, "Parse loi", e)
        }
    }

    /* ================== FIREBASE PUSH ================== */
    private fun sendToFirebase(lux: Int, led: Int) {

        val payload = mapOf(
            "lux" to lux,
            "led" to led,
            "timestamp" to ServerValue.TIMESTAMP
        )

        dbRef.setValue(payload)
            .addOnSuccessListener {
                Log.d(TAG, "Day Firebase OK: lux=$lux led=$led")
            }
            .addOnFailureListener {
                Log.e(TAG, "Firebase Loi", it)
            }
    }
}