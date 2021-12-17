package me.bizimpulse.label_printer

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import net.posprinter.utils.PosPrinterDev.PortType
import java.io.OutputStream


class iPosPrinterDev {
   val portInfo: PortInfo

    constructor(address: String) {
        this.portInfo = BluetoohPort(address = address)
    }


}


open class PortInfo() {
    lateinit var portType: PortType
    val isOpened: Boolean = false
}

class BluetoohPort() : PortInfo() {
    lateinit var address: String
    lateinit var adapter: BluetoothAdapter
    lateinit var socket: BluetoothSocket
    lateinit var outputStream: OutputStream

    constructor(address: String): this(){
        if(BluetoothAdapter.checkBluetoothAddress(address)) {
            this.portType = PortType.Bluetooth
            this.address = address
            this.adapter = BluetoothAdapter.getDefaultAdapter()
//            this.socket = adapter.
//            this.outputStream = this.adapter
        }else throw Exception("Invalid Address")
    }
}