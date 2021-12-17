package me.bizimpulse.label_printer

import android.bluetooth.BluetoothSocket
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


    fun printText(text: String) {
        println("Printing Text")

        val list: MutableList<ByteArray> = ArrayList()
        //creat a text ,and make it to byte[],
        //creat a text ,and make it to byte[],
        list.add(DataForSendToPrinterPos80.initializePrinter())
        val data1: ByteArray = text.toByteArray()
        list.add(data1)
        //should add the command of print and feed line,because print only when one line is complete, not one line, no print
        list.add(DataForSendToPrinterPos80.printAndFeedLine())
        //cut pager
//        list.add(DataForSendToPrinterPos80.selectCutPagerModerAndCutPager(66, 1))

        _print(list)
    }

    private fun _print(list: MutableList<ByteArray>) {
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
