class BluetoothDevice {
  BluetoothDevice();

  String? address;

  factory BluetoothDevice.fromJson(Map<String, dynamic> json) {
    return BluetoothDevice();
  }
  Map<String, dynamic> toJson() {
    return {};
  }
}
