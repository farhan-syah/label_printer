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

    labelPrinter.state.listen((state) {
      print('cur device status: $state');
    });
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
          tsc.print(2);
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
