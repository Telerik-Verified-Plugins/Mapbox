#import <Cordova/CDVPlugin.h>
#import "MGLMapView.h"
#import "MGLPointAnnotation.h"

@interface Mapbox : CDVPlugin<MGLMapViewDelegate>

@property (retain) MGLMapView *mapView;
@property (retain) NSArray *queuedAnnotations;
@property (retain) NSString *annotationCallbackId;

- (void) show:(CDVInvokedUrlCommand*)command;
- (void) hide:(CDVInvokedUrlCommand*)command;
- (void) addAnnotations:(CDVInvokedUrlCommand*)command;
- (void) addAnnotationCallback:(CDVInvokedUrlCommand*)command;

@end
