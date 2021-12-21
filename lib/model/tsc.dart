import 'dart:typed_data';

import 'package:bitmap/bitmap.dart';
import 'package:flutter/foundation.dart';

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

  addContent(TSCContent content) {
    this.content.add(content);
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

class TSCText extends TSCContent {
  final int x;
  final int y;
  final String fontType;
  final int rotation;
  final int xMultiplication;
  final int yMultiplication;
  final String content;

  TSCText({
    required this.x,
    required this.y,
    required this.content,
    this.fontType = "1",
    this.rotation = 0,
    this.xMultiplication = 2,
    this.yMultiplication = 2,
  }) : super(contentType: ContentType.text);

  @override
  Map<String, dynamic> toMap() {
    return {
      "type": describeEnum(contentType),
      "x": x,
      "y": y,
      "fontType": fontType,
      "rotation": rotation,
      "xMultiplication": xMultiplication,
      "yMultiplication": yMultiplication,
      "content": content,
    };
  }
}

class TSCBarcode extends TSCContent {
  final int x;
  final int y;
  final String codeType;
  final int height;
  final int human;
  final int rotation;
  final int narrow;
  final int wide;
  final String content;

  TSCBarcode({
    required this.x,
    required this.y,
    this.codeType = "128",
    required this.height,
    this.human = 1,
    this.rotation = 0,
    this.narrow = 2,
    this.wide = 2,
    required this.content,
  }) : super(contentType: ContentType.barcode);

  @override
  Map<String, dynamic> toMap() {
    return {
      "type": describeEnum(contentType),
      "x": x,
      "y": y,
      "codeType": codeType,
      "height": height,
      "human": human,
      "rotation": rotation,
      "narrow": narrow,
      "wide": wide,
      "content": content,
    };
  }
}

class TSCImage extends TSCContent {
  final int x;
  final int y;
  final int mode;
  final Uint8List image;

  TSCImage({
    required this.x,
    required this.y,
    this.mode = 0,
    required this.image,
  }) : super(contentType: ContentType.image);

  @override
  Map<String, dynamic> toMap() {
    return {
      "type": describeEnum(contentType),
      "x": x,
      "y": y,
      "mode": mode,
      "bitmap": image,
    };
  }
}

enum ContentType { text, barcode, image }
