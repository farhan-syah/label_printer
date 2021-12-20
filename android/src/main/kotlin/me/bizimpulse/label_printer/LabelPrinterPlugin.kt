package me.bizimpulse.label_printer


import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.EventChannel.EventSink
import io.flutter.plugin.common.EventChannel.StreamHandler
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*
import kotlin.Exception
import net.posprinter.service.PosprinterService


/** LabelPrinterPlugin */
@RequiresApi(23)
class LabelPrinterPlugin : FlutterPlugin, ActivityAware, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private lateinit var stateChannel: EventChannel
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter

    private var _socket: BluetoothSocket? = null
    private var _device: BluetoothDevice? = null
    private var _isConnecting: Boolean = false
    private var activity: Activity? = null



    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {


        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "label_printer_method")
        channel.setMethodCallHandler(this)

        stateChannel = EventChannel(flutterPluginBinding.binaryMessenger, "label_printer_event")
        stateChannel.setStreamHandler(stateStreamHandler)

        bluetoothManager =
            flutterPluginBinding.applicationContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter


    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        Log.d("onMethodCall", call.method.toString())
        when (call.method) {
            "state" -> {
                state(result)
            }
            "startScan" -> {
                if (activity != null) {
                    if (ContextCompat.checkSelfPermission(
                            activity!!,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            activity!!,
                            arrayOf<String>(Manifest.permission.ACCESS_COARSE_LOCATION),
                            MY_PERMISSIONS_REQUEST_LOCATION
                        )
                    }


                }
                getDevices(result)
            }
            "stopScan" -> {

            }
            "connect" -> {
                connect(result, call.arguments())
            }
            "isConnected" -> {
                isConnected(result, call.arguments())
            }
            "print" -> {
                print(result, call.arguments())
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private val stateStreamHandler: StreamHandler =
        object : StreamHandler {

            private var sink: EventSink? = null

            private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {


                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    Log.d(
                        "label_printer_stream",
                        "stateStreamHandler, current action: $action"
                    )
                    if (BluetoothAdapter.ACTION_STATE_CHANGED == action) {
//                        threadPool = null
                        sink!!.success(intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1))
                    } else if (BluetoothDevice.ACTION_ACL_CONNECTED == action) {
                        sink!!.success(1)
                    } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED == action) {
//                        threadPool = null
                        sink!!.success(0)
                    }
                }
            }

            override fun onListen(o: Any?, eventSink: EventSink) {
                sink = eventSink
                val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
                filter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)
                filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                if (activity != null)
                    activity!!.registerReceiver(broadcastReceiver, filter)
            }

            override fun onCancel(o: Any?) {
                sink = null
                if (activity != null)
                    activity!!.unregisterReceiver(broadcastReceiver)
            }
        }

    private fun state(result: Result) {
        try {
            when (bluetoothAdapter.state) {
                BluetoothAdapter.STATE_OFF -> result.success(BluetoothAdapter.STATE_OFF)
                BluetoothAdapter.STATE_ON -> result.success(BluetoothAdapter.STATE_ON)
                BluetoothAdapter.STATE_TURNING_OFF -> result.success(BluetoothAdapter.STATE_TURNING_OFF)
                BluetoothAdapter.STATE_TURNING_ON -> result.success(BluetoothAdapter.STATE_TURNING_ON)
                else -> result.success(0)
            }
        } catch (e: SecurityException) {
            result.error("invalid_argument", "Argument 'address' not found", null)
        }
    }

    private fun getDevices(result: Result) {
        val devices: MutableList<Map<String, Any>> = ArrayList()
        for (device in bluetoothAdapter.bondedDevices) {
            val ret: MutableMap<String, Any> = HashMap()
            ret["address"] = device.address
            ret["name"] = device.name
            ret["type"] = device.type
            devices.add(ret)
        }
        result.success(devices)
    }

    private fun connect(result: Result, args: Map<String, Any?>) {
        try {

            if(args["address"] == null) throw Exception("Address is null")

            val device = bluetoothAdapter.getRemoteDevice(args["address"] as String)
            if(_device?.address != device.address) _device = device

            if(_socket?.remoteDevice?.address != _device?.address)
            _socket = device!!.createInsecureRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))


            println("isConnecting : $_isConnecting")
            println("isSocketConnected : ${_socket!!.isConnected}")
            if(!_isConnecting && !_socket!!.isConnected) {
                _isConnecting = true
                _socket!!.connect()
                _isConnecting = false
            }

            result.success("Connection Successful")
        }catch (e: Exception){
            _isConnecting = false
            result.error("Connect Error", e.message, null)
        }
    }

    private fun isConnected(result: Result, args: Map<String, Any?>) {
        try {
           result.success(_socket?.isConnected == true)
        }catch (e: Exception){
            result.error("Connect Error", e.message, null)
        }
    }


    private fun print(result: Result, args: Map<String, Any?>) {
        try {
            if(_socket!=null){
                val bluetoothPrintService: BluetoothPrintService = BluetoothPrintService(_socket!!)
                bluetoothPrintService.print(args)
            }

            result.success("Print is Successful")
//            bluetoothPrintService.connectWithBluetooth(_device!!.address)
//            printService.printText("Test");
//            posprinterService.MyBinder().writeDataByYouself();
        }catch (e: Exception){
//            println(e.toString())
            result.error("Printing Error", e.message, null)
        }
    }



    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    companion object {
        private const val MY_PERMISSIONS_REQUEST_LOCATION = 99
        private const val MY_PERMISSIONS_REQUEST_BACKGROUND_LOCATION = 66
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity

    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null;
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity
    }

    override fun onDetachedFromActivity() {
        activity = null
    }
}
