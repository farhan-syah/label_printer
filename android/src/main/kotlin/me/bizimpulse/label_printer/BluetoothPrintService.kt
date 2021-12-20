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
                outputStream.write(DataForSendToPrinterTSC.sizeBymm(data["width"] as Double, data["height"] as Double))
                outputStream.write(DataForSendToPrinterTSC.cls())

                val content =  data["content"] as List<*>

                content.forEach{
                    val c = it as Map<*, *>
                    if(c["type"]=="barcode") {
                        outputStream.write(
                            DataForSendToPrinterTSC.barCode(
                                c["x"] as Int,
                                c["y"] as Int,
                                c["y"] as String,
                                c["height"] as Int,
                                c["human"] as Int,
                                c["rotation"] as Int,
                                c["narrow"] as Int,
                                c["wide"] as Int,
                                c["content"] as String
                            )
                        )
                    }
                }
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
                outputStream.write(DataForSendToPrinterTSC.print(data["count"] as Int))
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
