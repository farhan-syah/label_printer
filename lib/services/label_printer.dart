import 'dart:async';

import 'package:flutter/services.dart';
import 'package:label_printer/model/tsc.dart';
import 'package:rxdart/rxdart.dart';

import '../model/bluetooth_device.dart';

class LabelPrinter {
  static const MethodChannel _channel = MethodChannel('label_printer_method');
  static const EventChannel _stateChannel = EventChannel('label_printer_event');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }

  LabelPrinter._() {
    _channel.setMethodCallHandler((MethodCall call) async {
      _methodStreamController.add(call);
    });
  }

  static final LabelPrinter _instance = LabelPrinter._();

  static LabelPrinter get instance => _instance;

  final BehaviorSubject<bool> _isScanning = BehaviorSubject.seeded(false);

  Stream<bool> get isScanning => _isScanning.stream;

  final BehaviorSubject<List<BluetoothDevice>> _scanResults =
      BehaviorSubject.seeded(<BluetoothDevice>[]);
  Stream<List<BluetoothDevice>> get scanResults => _scanResults.stream;

  Stream<MethodCall> get _methodStream => _methodStreamController.stream;
  final StreamController<MethodCall> _methodStreamController =
      StreamController.broadcast();

  List<BluetoothDevice> devices = [];

  Stream<int?> get state async* {
    yield await _channel.invokeMethod('state').then((s) => s);

    yield* _stateChannel.receiveBroadcastStream().map((s) => s);
  }

  Future<void> scan({
    Duration? timeout,
  }) async {
    if (_isScanning.value == true) {
      stopScan();
    }

    _isScanning.add(true);

    _scanResults.add(<BluetoothDevice>[]);

    try {
      final List _result = List.from(await _channel.invokeMethod('startScan'));

      for (var e in _result) {
        if (e['address'] != null) {
          _scanResults.value
              .add(BluetoothDevice.fromJson(Map<String, dynamic>.from(e)));
        }
      }
    } catch (e) {
      _isScanning.add(false);
      rethrow;
    }

    return Future.delayed(timeout ?? const Duration(seconds: 2));
  }

  Future<List<BluetoothDevice>> startScan({
    Duration? timeout,
  }) async {
    await scan(timeout: timeout);
    return _scanResults.value;
  }

  Future stopScan() async {
    await _channel.invokeMethod('stopScan');
    _isScanning.add(false);
  }

  Future<void> connect(BluetoothDevice device) async {
    await _channel.invokeMethod('connect', device.toJson());
  }

  Future<bool> isConnected(BluetoothDevice device) async {
    return await _channel.invokeMethod('isConnected', device.toJson());
  }

  printTSC(TSC content) async {
    print(content.toMap());
    await _channel.invokeMethod('print', content.toMap());
  }
}
