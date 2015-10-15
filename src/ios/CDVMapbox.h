#import <Cordova/CDVPlugin.h>
#import "Mapbox.h"

@interface CDVMapbox : CDVPlugin<MGLMapViewDelegate>

@property (retain) MGLMapView *mapView;
@property (retain) NSString *markerCallbackId;
@property (retain) MGLPointAnnotation *selectedAnnotation;

- (void) show:(CDVInvokedUrlCommand*)command;
- (void) hide:(CDVInvokedUrlCommand*)command;

- (void) addMarkers:(CDVInvokedUrlCommand*)command;
- (void) addMarkerCallback:(CDVInvokedUrlCommand*)command;

- (void) addPolygon:(CDVInvokedUrlCommand*)command;

- (void) addGeoJSON:(CDVInvokedUrlCommand*)command;

- (void) getCenter:(CDVInvokedUrlCommand*)command;
- (void) setCenter:(CDVInvokedUrlCommand*)command;

- (void) getZoomLevel:(CDVInvokedUrlCommand*)command;
- (void) setZoomLevel:(CDVInvokedUrlCommand*)command;

@end
