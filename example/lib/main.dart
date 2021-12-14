import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
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

      //   switch (state) {
      //     case BluetoothManager.CONNECTED:
      //       setState(() {
      //         _connected = true;
      //         tips = 'connect success';
      //       });
      //       break;
      //     case BluetoothManager.DISCONNECTED:
      //       setState(() {
      //         _connected = false;
      //         tips = 'disconnect success';
      //       });
      //       break;
      //     default:
      //       break;
      //   // }
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
                    ...List.generate(
                        snapshot.data!.length,
                        (index) =>
                            Text(snapshot.data![index].address.toString()))
                  ],
                );
              } else
                return Container();
            }),
      ),
    );
  }
}
