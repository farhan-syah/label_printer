class TSC {
  TSC();

  double? width;
  double? height;

  double? gapWidth;
  double? gapHeight;

  int printCount = 0;

  List<TSCContent> content = <TSCContent>[];

  setSizeInMM({required double width, required double height}) {
    this.width = width;
    this.height = height;
  }

  setGapInMM({required double width, required double height}) {
    gapWidth = width;
    gapHeight = height;
  }

  addContent({required double width, required double height}) {
    gapWidth = width;
    gapHeight = height;
  }

  print([int count = 1]) {
    printCount += count;
  }

  Map<String, dynamic> toMap() {
    assert(width != null);
    assert(height != null);
    assert(printCount != 0);
    return {
      "width": width,
      "height": height,
      "gapWidth": gapWidth,
      "gapHeight": gapHeight,
      "content": content.map((c) => c.toMap()).toList(),
      "count": printCount,
    };
  }
}

class TSCContent {
  final ContentType contentType;

  TSCContent({required this.contentType});

  toMap() {}
}

class TSCBarcode extends TSCContent {
  final int x;
  final int y;
  final String codeType;
  final int heigth;
  final int human;
  final int rotation;
  final int narrow;
  final int wide;
  final String content;

  TSCBarcode({
    required this.x,
    required this.y,
    this.codeType = "128",
    required this.heigth,
    this.human = 1,
    this.rotation = 0,
    this.narrow = 2,
    this.wide = 2,
    required this.content,
  }) : super(contentType: ContentType.barcode);

  @override
  Map<String, dynamic> toMap() {
    return {
      "type": contentType,
      "x": x,
      "y": y,
      "codeType": codeType,
      "height": heigth,
      "human": human,
      "rotation": rotation,
      "narrow": narrow,
      "wide": wide,
      "content": content,
    };
  }
}

enum ContentType { barcode, print }
