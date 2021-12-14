package me.bizimpulse.label_printer


import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
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
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.ArrayList
import java.util.HashMap


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

    private var activity: Activity? = null
    private var pendingCall: MethodCall? = null
    private var pendingResult: Result? = null



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
                        pendingCall = call
                        pendingResult = result
                    }

                }
                getDevices(result)
//                startScan(call, result)
            }
            "stopScan" -> {

            }
            else -> {
                result.notImplemented()
            }
        }
    }

    private val stateStreamHandler: StreamHandler =
        object : StreamHandler {

            private var sink: EventSink? = null

            private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {


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
                    activity!!.registerReceiver(mReceiver, filter)
            }

            override fun onCancel(o: Any?) {
                sink = null
                if (activity != null)
                    activity!!.unregisterReceiver(mReceiver)
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
        Log.d("getDevices", devices.toString())
        result.success(devices)
    }

    private fun startScan(call: MethodCall, result: Result) {
        try {
            startScan()
            result.success(null)
        } catch (e: Exception) {
            result.error("startScan", e.message, null)
        }
    }


    @Throws(IllegalStateException::class)
    private fun startScan() {


        val scanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
            ?: throw IllegalStateException("bluetoothAdapter.bluetoothLeScanner is null. Is the Adapter on?")

        // 0:lowPower 1:balanced 2:lowLatency -1:opportunistic
        val settings : ScanSettings =
            ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build()
        Log.d("183", settings.toString())
        val filters = listOf<ScanFilter>()
        scanner.startScan(filters, settings, scanCallback)
    }



    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            Log.d("scanCallback", result.toString())
            super.onScanResult(callbackType, result)
            if(result?.device !=null){
                val device = result.device
                Log.d("scanCallback", device.toString())
                if (device != null && device.name != null) {
                    invokeMethodUIThread("ScanResult", device)
                }
            }
        }

//
    }

    private fun invokeMethodUIThread(name: String, device: BluetoothDevice) {
        val ret: MutableMap<String, Any> = HashMap()
        ret["address"] = device.address
        ret["name"] = device.name
        ret["type"] = device.type
        activity!!.runOnUiThread { channel.invokeMethod(name, ret) }
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
