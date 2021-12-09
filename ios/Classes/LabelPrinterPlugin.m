#import "LabelPrinterPlugin.h"
#if __has_include(<label_printer/label_printer-Swift.h>)
#import <label_printer/label_printer-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "label_printer-Swift.h"
#endif

@implementation LabelPrinterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftLabelPrinterPlugin registerWithRegistrar:registrar];
}
@end
