class BluetoothDevice {
  String? address;
  BluetoothDevice({this.address});

  factory BluetoothDevice.fromJson(Map<String, dynamic> json) {
    return BluetoothDevice(address: json['address'] as String);
  }
  Map<String, dynamic> toJson() {
    return {};
  }
}
