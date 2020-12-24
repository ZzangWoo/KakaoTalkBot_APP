package com.example.kakaotalknotification.Arduino

import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import java.io.IOException
import java.util.*

class ControlArduino {
    companion object {
        lateinit var bluetoothAdapter: BluetoothAdapter
        lateinit var pairedDevices: Set<BluetoothDevice>
        val REQUEST_ENABLE_BLUETOOTH = 1

        val EXTRA_ADDRESS: String = "Device_address"

        var deviceUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
        var bluetoothSocket: BluetoothSocket? = null

        lateinit var deviceAddress: String

        fun connect() { pairedDeviceList() }
        fun on() { sendCommand("N") }
        fun off() { sendCommand("F") }
        fun refresh() { refreshBluetooth() }

        private fun refreshBluetooth() {
            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket!!.close()
                    bluetoothSocket = null

                    pairedDeviceList()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        private fun sendCommand(input: String) {
            if (bluetoothSocket != null) {
                try {
                    bluetoothSocket!!.outputStream.write((input.toByteArray()))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        private fun pairedDeviceList() {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            pairedDevices = bluetoothAdapter.bondedDevices
            val list : ArrayList<BluetoothDevice> = ArrayList()

            if (!pairedDevices.isEmpty()) {
                for (device: BluetoothDevice in pairedDevices) {
                    Log.i("device", "Name : " + device.name)
                    Log.i("device", "Mac : " + device.address)

                    deviceAddress = device.address
                }
            } else {
//                toast("No paired devices found")
            }

            ConnectToDevice().execute()
        }
    }

    private class ConnectToDevice () : AsyncTask<Void, Void, String>() {
        private var connectSuccess: Boolean = true

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void?): String? {
            try {
                if (bluetoothSocket == null) {
                    val device: BluetoothDevice = bluetoothAdapter.getRemoteDevice(deviceAddress)
                    bluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(deviceUUID)
                    bluetoothSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
                e.printStackTrace()
            }
            return null
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)

            if (!connectSuccess) {
                Log.i("data", "couldn't connect")
            } else {
                Log.i("data", "connected")
            }
        }
    }
}