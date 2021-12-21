package me.bizimpulse.label_printer

import android.bluetooth.BluetoothSocket
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import net.posprinter.utils.BitmapToByteData
import net.posprinter.utils.DataForSendToPrinterPos80
import net.posprinter.utils.DataForSendToPrinterTSC
import net.posprinter.utils.PosPrinterDev.*
import java.io.OutputStream
import java.lang.Exception
import java.lang.Iterable
import java.util.ArrayList

class BluetoothPrintService() {

    lateinit var outputStream: OutputStream

    constructor(socket: BluetoothSocket) : this() {
        this.outputStream = socket.outputStream
    }

    fun print(data: Map<String, Any?>) {
        val thread = Thread {
            try {
                outputStream.write(
                    DataForSendToPrinterTSC.sizeBymm(
                        data["width"] as Double,
                        data["height"] as Double
                    )
                )
                outputStream.write(DataForSendToPrinterTSC.cls())

                val content = data["content"] as List<*>

                content.forEach {
                    val c = it as Map<*, *>
                    when (c["type"]) {
                        "barcode" -> {
                            outputStream.write(
                                DataForSendToPrinterTSC.barCode(
                                    c["x"] as Int,
                                    c["y"] as Int,
                                    c["codeType"] as String,
                                    c["height"] as Int,
                                    c["human"] as Int,
                                    c["rotation"] as Int,
                                    c["narrow"] as Int,
                                    c["wide"] as Int,
                                    c["content"] as String
                                )
                            )
                        }
                        "image" -> {
                            val arr = c["bitmap"] as ByteArray
                            val bitmap = BitmapFactory.decodeByteArray(arr, 0, arr.size)
                            outputStream.write(
                                DataForSendToPrinterTSC.bitmap(
                                    c["x"] as Int,
                                    c["y"] as Int,
                                    c["mode"] as Int,
                                    bitmap,
                                    BitmapToByteData.BmpType.Threshold,
                                )
                            )
                        }
                        "text" -> {
                            outputStream.write(
                                DataForSendToPrinterTSC.text(
                                    c["x"] as Int,
                                    c["y"] as Int,
                                    c["fontType"] as String,
                                    c["rotation"] as Int,
                                    c["xMultiplication"] as Int,
                                    c["yMultiplication"] as Int,
                                    c["content"] as String
                                )
                            )
                        }
                    }
                }
                outputStream.write(DataForSendToPrinterTSC.print(data["count"] as Int))
            } catch (e: Exception) {
                println(e)
            }
        }
        thread.start()
    }

}
