import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:label_printer/label_printer.dart';

void main() {
  const MethodChannel channel = MethodChannel('label_printer');

  TestWidgetsFlutterBinding.ensureInitialized();

  setUp(() {
    channel.setMockMethodCallHandler((MethodCall methodCall) async {
      return '42';
    });
  });

  tearDown(() {
    channel.setMockMethodCallHandler(null);
  });

  test('getPlatformVersion', () async {
    expect(await LabelPrinter.platformVersion, '42');
  });
}
