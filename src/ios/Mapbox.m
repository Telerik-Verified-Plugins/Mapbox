#import "MGLMapView.h"
#import "Mapbox.h"

@implementation Mapbox

- (void) test:(CDVInvokedUrlCommand*)command {

  MGLMapView *mapView = [[MGLMapView alloc] initWithFrame:CGRectMake(0, 0, 400, 400)
                                              accessToken:@"<access token string>"];

  [mapView setCenterCoordinate:CLLocationCoordinate2DMake(28.369334, -80.743779)
                     zoomLevel:13
                      animated:NO];

  [mapView useBundledStyleNamed:@"outdoors"];

  [self.view addSubview:mapView];


  NSMutableDictionary *args = [command.arguments objectAtIndex:0];
  NSString *cards = [args objectForKey:@"cards"];
  if (cards == nil) {
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"cards is required"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    return;
  } else {
  }

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

@end