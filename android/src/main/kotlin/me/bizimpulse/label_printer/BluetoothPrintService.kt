package me.bizimpulse.label_printer

import android.bluetooth.BluetoothSocket
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import net.posprinter.utils.DataForSendToPrinterTSC
import net.posprinter.utils.PosPrinterDev.*
import java.io.OutputStream
import java.lang.Exception
import java.util.ArrayList

class BluetoothPrintService() {

    lateinit var outputStream: OutputStream

    constructor(socket: BluetoothSocket) : this() {
        this.outputStream = socket.outputStream
    }


    fun print(data: Map<String, Any?>) {
        val thread = Thread {
            try {
                println(data)
                outputStream.write(DataForSendToPrinterTSC.sizeBymm(50.0, 30.0))
                outputStream.write(DataForSendToPrinterTSC.cls())
//                outputStream.write(
//                    DataForSendToPrinterTSC.barCode(
//                        60,
//                        50,
//                        "128",
//                        100,
//                        1,
//                        0,
//                        2,
//                        2,
//                        "abcdef12345"
//                    )
//                )
//                outputStream.write(
//                    DataForSendToPrinterTSC.bitmap(
//                        0,
//                        0,
//                        0,
//                        b,
//                        BitmapToByteData.BmpType.Dithering
//                    )
//                )
                outputStream.write(DataForSendToPrinterTSC.print(1))
                println("Printing is done")
            }catch (e:Exception){
                println(e)
            }
        }
        thread.start()
    }

    private fun _print(list: MutableList<ByteArray>) {
        val thread = Thread {
            try {
                println("Printing")
                outputStream.write(DataForSendToPrinterTSC.sizeBymm(50.0, 30.0))
//                outputStream.write(
//                    DataForSendToPrinterTSC.barCode(
//                        60,
//                        50,
//                        "128",
//                        100,
//                        1,
//                        0,
//                        2,
//                        2,
//                        "abcdef12345"
//                    )
//                )
//                outputStream.write(
//                    DataForSendToPrinterTSC.bitmap(
//                        0,
//                        0,
//                        0,
//                        b,
//                        BitmapToByteData.BmpType.Dithering
//                    )
//                )
                outputStream.write(DataForSendToPrinterTSC.print(1))
                println("Printing is done")
            }catch (e:Exception){
                println(e)
            }
        }
        thread.start()
    }

    private fun _printTest(list: MutableList<ByteArray>) {
        val thread = Thread {
            try {
                println("Printing")
                outputStream.write(DataForSendToPrinterTSC.selfTest())
                outputStream.write(DataForSendToPrinterTSC.print(1))
                println("Printing is done")
            }catch (e:Exception){
                println(e)
            }
        }
        thread.start()
    }

}
