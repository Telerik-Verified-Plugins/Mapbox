#import <Cordova/CDVPlugin.h>
#import "Mapbox.h"
#import "Map.h"
#import "PluginLayout.h"
#import "PluginScrollLayout.h"
#import "MapsManager.h"
#import "MGLAccountManager.h"

@interface CDVMapbox : CDVPlugin<UIScrollViewDelegate>;

@property (nonatomic) MapOverlayLayer *mapOverlayLayer;
@property (nonatomic) PluginScrollLayout *pluginScrollView;

- (void) onPause:(Boolean *)multitasking;
- (void) onResume:(Boolean *)multitasking;
- (void) onDestroy;

- (void) getCenter:(CDVInvokedUrlCommand *)command;
- (void) setCenter:(CDVInvokedUrlCommand*)command;

- (void) setZoomLevel:(CDVInvokedUrlCommand *)command;
- (void) getZoomLevel:(CDVInvokedUrlCommand *)command;

- (void) setTilt:(CDVInvokedUrlCommand *)command;
- (void) getTilt:(CDVInvokedUrlCommand *)command;

- (void) refreshMap:(CDVInvokedUrlCommand *)command;

- (void) setClickable:(CDVInvokedUrlCommand *)command;

- (void) onRegionWillChange:(CDVInvokedUrlCommand *)command;
- (void) onRegionIsChanging:(CDVInvokedUrlCommand *)command;
- (void) onRegionDidChange:(CDVInvokedUrlCommand *)command;

- (void) getBoundsCoordinates:(CDVInvokedUrlCommand *)command;

- (void) animateCamera:(CDVInvokedUrlCommand *)command;

- (void) addPolygon:(CDVInvokedUrlCommand *)command;
- (void) addGeoJSON:(CDVInvokedUrlCommand *)command;
- (void) addMarkers:(CDVInvokedUrlCommand *)command;
- (void) addMarkerCallback:(CDVInvokedUrlCommand *)command;

- (void) convertCoordinate:(CDVInvokedUrlCommand *)command;
- (void) convertPoint:(CDVInvokedUrlCommand *)command;

@end
