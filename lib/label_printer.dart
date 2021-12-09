
import 'dart:async';

import 'package:flutter/services.dart';

class LabelPrinter {
  static const MethodChannel _channel = MethodChannel('label_printer');

  static Future<String?> get platformVersion async {
    final String? version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
