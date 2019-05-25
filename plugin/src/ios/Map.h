#import "MapController.h"

@class CDVMapbox;

@interface Map: NSObject;

@property (atomic) int *id;
@property (retain) MGLPointAnnotation *selectedAnnotation;
@property (nonatomic) NSMutableDictionary *margins;

- (id)initWithArgs:(NSDictionary *)args withCDVMapboxPlugin:(CDVMapbox*)cdvMapbox;

- (void) show:(CDVInvokedUrlCommand *)command;
- (void) hide:(CDVInvokedUrlCommand *)command;

- (void) refreshMap:(CDVInvokedUrlCommand *)command;

- (void) getCenterCoordinates:(CDVInvokedUrlCommand *)command;
- (void) setCenterCoordinates:(CDVInvokedUrlCommand*)command;

- (void) setZoomLevel:(CDVInvokedUrlCommand *)command;
- (void) getZoomLevel:(CDVInvokedUrlCommand *)command;

- (void)getBoundsCoordinates:(CDVInvokedUrlCommand *)command;

- (void)setTilt:(CDVInvokedUrlCommand *)command;
- (void) getTilt:(CDVInvokedUrlCommand *)command;

- (void) onRegionWillChange:(CDVInvokedUrlCommand *)command;
- (void) onRegionIsChanging:(CDVInvokedUrlCommand *)command;
- (void) onRegionDidChange:(CDVInvokedUrlCommand *)command;

- (void) animateCamera:(CDVInvokedUrlCommand *)command;

- (void) addPolygon:(CDVInvokedUrlCommand *)command;
- (void) addGeoJSON:(CDVInvokedUrlCommand *)command;
- (void) addMarkers:(CDVInvokedUrlCommand *)command;
- (void) addMarkerCallback:(CDVInvokedUrlCommand *)command;

- (void)convertCoordinates:(CDVInvokedUrlCommand *)command;
- (void) convertPoint:(CDVInvokedUrlCommand *)command;

- (void) destroy;

@end
