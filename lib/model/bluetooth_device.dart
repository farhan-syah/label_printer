class BluetoothDevice {
  final String address;
  final String name;
  BluetoothDevice({required this.name, required this.address});

  factory BluetoothDevice.fromJson(Map<String, dynamic> json) {
    return BluetoothDevice(name: json['name'], address: json['address']);
  }
  Map<String, dynamic> toJson() {
    return {
      'name': name,
      'address': address,
    };
  }
}
