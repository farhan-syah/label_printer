import 'dart:typed_data';
import 'package:flutter/material.dart';
import 'package:label_printer/label_printer.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  final LabelPrinter labelPrinter = LabelPrinter.instance;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance!.addPostFrameCallback((_) => initBluetooth());
  }

  initBluetooth() {
    labelPrinter.startScan(timeout: const Duration(seconds: 2));
    // labelPrinter.state.listen((state) {
    //   print('cur device status: $state');
    // });
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(
          title: const Text('Plugin example app'),
          actions: [
            InkWell(
              onTap: () {
                labelPrinter.startScan();
              },
              child: const Icon(Icons.refresh),
            )
          ],
        ),
        body: StreamBuilder<List<BluetoothDevice>>(
            stream: labelPrinter.scanResults,
            builder: (context, snapshot) {
              if (snapshot.hasData) {
                return Column(
                  children: [
                    ...List.generate(snapshot.data!.length, (index) {
                      BluetoothDevice device = snapshot.data![index];
                      return BluetoothDeviceContainer(device: device);
                    })
                  ],
                );
              } else {
                return Container();
              }
            }),
      ),
    );
  }
}

class BluetoothDeviceContainer extends StatelessWidget {
  final BluetoothDevice device;
  BluetoothDeviceContainer({Key? key, required this.device}) : super(key: key);
  final LabelPrinter labelPrinter = LabelPrinter.instance;
  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () async {
        await labelPrinter.connect(device);
        bool connected = await labelPrinter.isConnected(device);
        if (connected) {
          TSC tsc = TSC();
          tsc.setSizeInMM(width: 50, height: 30);
          // tsc.addContent(
          //     TSCBarcode(x: 100, y: 50, height: 100, content: '123456789'));

          Uint8List data = Uint8List.fromList([
            137,
            80,
            78,
            71,
            13,
            10,
            26,
            10,
            0,
            0,
            0,
            13,
            73,
            72,
            68,
            82,
            0,
            0,
            0,
            5,
            0,
            0,
            0,
            5,
            8,
            6,
            0,
            0,
            0,
            141,
            111,
            38,
            229,
            0,
            0,
            0,
            28,
            73,
            68,
            65,
            84,
            8,
            215,
            99,
            248,
            255,
            255,
            63,
            195,
            127,
            6,
            32,
            5,
            195,
            32,
            18,
            132,
            208,
            49,
            241,
            130,
            88,
            205,
            4,
            0,
            14,
            245,
            53,
            203,
            209,
            142,
            14,
            31,
            0,
            0,
            0,
            0,
            73,
            69,
            78,
            68,
            174,
            66,
            96,
            130
          ]);
          tsc.addContent(TSCImage(x: 50, y: 50, image: data));
          tsc.addContent(TSCText(x: 50, y: 50, content: 'Test'));
          tsc.print();
          await labelPrinter.printTSC(tsc);
        }
      },
      child: Container(
        padding: const EdgeInsets.all(10),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(device.name.toString()),
            Text(device.address.toString()),
          ],
        ),
      ),
    );
  }
}
