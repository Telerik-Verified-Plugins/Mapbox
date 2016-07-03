//
// Created by vikti on 31/03/2016.
//

/**
 * This class represent a single Map. It is instantiated by the MapsManager.
 * First it creates a controller (which instantiate the MGLMapView). Then, it receives all the CDVMapbox commands
 * and transmit them to the controller. It does not act directly on the map view.
 */


#import "Map.h"
#import "CDVMapbox.h"

@interface Map () {
@private
    CGRect _mapFrame;
    MapController *_mapCtrl;
    CDVMapbox *_cdvMapbox;
}
@end


@implementation Map


- (id)initWithArgs:(NSDictionary *)args withCDVMapboxPlugin:(CDVMapbox*)cdvMapbox{

    self = [self init];

    _cdvMapbox = cdvMapbox;
    _mapFrame = [self getFrameWithDictionary:args[@"margins"]];

    // Create a controller (which instantiate the MGLMapbox view)
    _mapCtrl = [[MapController alloc] initWithArgs:args
                                      withMapFrame:_mapFrame
                               withCDVMapboxPlugin:_cdvMapbox];

    _cdvMapbox.mapOverlayLayer.mapCtrl = _mapCtrl;
    [self updatePluginLayerLayout:args[@"HTMLs"]];
    [_cdvMapbox.pluginScrollView attachView:_mapCtrl.view];

    return self;
}

- (void) updatePluginLayerLayout:(NSArray*)nodes{

    _cdvMapbox.mapOverlayLayer.mapFrame = _mapFrame;
    _cdvMapbox.pluginScrollView.debugView.mapFrame = _mapFrame;

    if ([nodes count]) {
        [_cdvMapbox.mapOverlayLayer clearHTMLElement];
        NSMutableString *id;
        NSMutableDictionary *size, *elem;
        for (elem in nodes) {
            size = elem[@"size"];
            id = elem[@"id"];
            [_cdvMapbox.mapOverlayLayer setHTMLElement:id size:size];
        }
    }

    [_cdvMapbox.pluginScrollView.debugView setNeedsDisplay];

}

- (void)refreshMap:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[1];

    if (args[@"margins"] == nil) {
        CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"invalid mapFrame"];
        [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        return;
    }

    _mapFrame = [self getFrameWithDictionary:args[@"margins"]];

    // update the touchable zone in the plugin layer
    [self updatePluginLayerLayout:args[@"HTMLs"]];

    // resize the map view
    [_mapCtrl setFrame:_mapFrame];

    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (int *)getId {
    return _id;
}

/**
 * Set the touchable frame of the plugin layer and the controller map view from the input margins
 */
- (CGRect)getFrameWithDictionary:(NSDictionary *)margins
{
    int left = [margins[@"left"] intValue];
    int right = [margins[@"right"] intValue];
    int top = [margins[@"top"] intValue];
    int bottom = [margins[@"bottom"] intValue];

    CGRect webviewFrame = _cdvMapbox.webView.frame;

    return CGRectMake(
            left,
            top,
            webviewFrame.size.width - left - right,
            webviewFrame.size.height - top - bottom
    );
}

- (void) setZoomLevel:(CDVInvokedUrlCommand*)command {
    NSDictionary *args = command.arguments[1];
    NSNumber *level = args[@"level"];
    BOOL animated = [args[@"animated"] boolValue];
    double zoom = level.doubleValue;
    if (zoom >= 0 && zoom <= 20) {
        [_mapCtrl.mapView setZoomLevel:zoom animated:animated];
        CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    } else {
        CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"invalid zoomlevel, use any double value from 0 to 20 (like 8.3)"];
        [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) getZoomLevel:(CDVInvokedUrlCommand*)command {
    double zoom = _mapCtrl.mapView.zoomLevel;
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:zoom];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)show:(CDVInvokedUrlCommand *)command {
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
   // pluginResult.keepCallback = @YES;
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)hide:(CDVInvokedUrlCommand *)command {

}

- (void)getCenterCoordinates:(CDVInvokedUrlCommand*)command {
    CLLocationCoordinate2D ctr = _mapCtrl.mapView.centerCoordinate;
    NSDictionary *dic = @{@"lat" : @(ctr.latitude), @"lng" : @(ctr.longitude)};
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)setCenterCoordinates:(CDVInvokedUrlCommand*)command {
    NSDictionary *args = command.arguments[1];
    NSNumber *clat = args[@"lat"];
    NSNumber *clng = args[@"lng"];
    BOOL animated = [args[@"animated"] boolValue];

    [_mapCtrl.mapView setCenterCoordinate:CLLocationCoordinate2DMake(clat.doubleValue, clng.doubleValue) animated:animated];

    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) getBoundsCoordinates:(CDVInvokedUrlCommand *)command {
    MGLCoordinateBounds bounds = [_mapCtrl.mapView  convertRect:[_mapCtrl getFrame] toCoordinateBoundsFromView:_mapCtrl.mapView];

    NSMutableDictionary *returnInfo = [NSMutableDictionary dictionaryWithCapacity:2];

    returnInfo[@"sw"] = [[NSMutableArray alloc] initWithCapacity:2];
    returnInfo[@"sw"][0] = @(bounds.sw.longitude);
    returnInfo[@"sw"][1] = @(bounds.sw.latitude);

    returnInfo[@"ne"] = [[NSMutableArray alloc] initWithCapacity:2];
    returnInfo[@"ne"][0] = @(bounds.ne.longitude);
    returnInfo[@"ne"][1] = @(bounds.ne.latitude);

    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
    [pluginResult setKeepCallbackAsBool:YES];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) setTilt:(CDVInvokedUrlCommand*)command {
    // TODO tilt/pitch seems not to be implemented in Mapbox iOS SDK (yet)
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not implemented for iOS (yet)"];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) getTilt:(CDVInvokedUrlCommand*)command {
    // TODO seems not to be implemented in Mapbox iOS SDK (yet)
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not implemented for iOS (yet)"];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)onRegionWillChange:(CDVInvokedUrlCommand *)command {
    _mapCtrl.regionWillChangeAnimatedCallbackId = command.callbackId;
}

- (void)onRegionIsChanging:(CDVInvokedUrlCommand *)command {
    _mapCtrl.regionIsChangingCallbackId = command.callbackId;
}

- (void)onRegionDidChange:(CDVInvokedUrlCommand *)command {
    _mapCtrl.regionDidChangeAnimatedCallbackId = command.callbackId;
}

- (void)animateCamera:(CDVInvokedUrlCommand*)command {
    NSDictionary *args = command.arguments[1];

    MGLMapCamera * cam = [MGLMapCamera camera];

    NSNumber *altitude = [args valueForKey:@"altitude"];
    if (altitude != nil) {
        cam.altitude = [altitude doubleValue];
    }

    NSNumber *tilt = [args valueForKey:@"tilt"];
    if (tilt != nil) {
        cam.pitch = [tilt floatValue];
    }

    NSNumber *bearing = [args valueForKey:@"bearing"];
    if (bearing != nil) {
        cam.heading = [bearing floatValue];
    }

    NSTimeInterval durInt = 15; // default 15
    NSNumber *duration = [args valueForKey:@"duration"];
    if (duration != nil) {
        durInt = [duration intValue];
    }

    NSDictionary *target = args[@"target"];
    if (target != nil) {
        NSNumber *clat = [target valueForKey:@"lat"];
        NSNumber *clng = [target valueForKey:@"lng"];
        cam.centerCoordinate = CLLocationCoordinate2DMake(clat.doubleValue, clng.doubleValue);
    }

    [_mapCtrl.mapView setCamera:cam withDuration:durInt animationTimingFunction:[CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut]];

    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void)addPolygon:(CDVInvokedUrlCommand*)command {
    NSDictionary *args = command.arguments[1];
    NSArray* points = args[@"points"];
    if (points != nil) {
        [_cdvMapbox.commandDelegate runInBackground:^{
            CLLocationCoordinate2D *coordinates = malloc(points.count * sizeof(CLLocationCoordinate2D));
            for (int i=0; i<points.count; i++) {
                NSDictionary* point = points[i];
                NSNumber *lat = [point valueForKey:@"lat"];
                NSNumber *lng = [point valueForKey:@"lng"];
                coordinates[i] = CLLocationCoordinate2DMake(lat.doubleValue, lng.doubleValue);
            }
            NSUInteger numberOfCoordinates = points.count; // sizeof(coordinates) / sizeof(CLLocationCoordinate2D);
            MGLPolygon *shape = [MGLPolygon polygonWithCoordinates:coordinates count:numberOfCoordinates];
            [_mapCtrl.mapView addAnnotation:shape];
            CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
            [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
        }];
    } else {
        CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
        [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }
}

- (void) addGeoJSON:(CDVInvokedUrlCommand*)command {
//  NSString *url = [command.arguments objectAtIndex:0];
// TODO not implemented yet, see https://www.mapbox.com/ios-sdk/examples/line-geojson/
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) addMarkers:(CDVInvokedUrlCommand*)command {
    NSArray *markers = command.arguments[1];
    if (markers != nil) [_mapCtrl putMarkersOnTheMap:markers];
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@"zefzefz"];
    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}


- (void) addMarkerCallback:(CDVInvokedUrlCommand*)command {
    _mapCtrl.markerCallbackId = command.callbackId;
}


- (void)convertCoordinates:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[1];

    double lat = [[args valueForKey:@"lat"]doubleValue];
    double lng = [[args valueForKey:@"lng"]doubleValue];

    if ((fabs(lat) > 90)||(fabs(lng) > 180)){
        CDVPluginResult * pluginResult = [CDVPluginResult
                resultWithStatus:CDVCommandStatus_ERROR
                 messageAsString:@"Incorrect Leaflet.LatLng value."];
        [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }

    CGPoint screenPoint = [_mapCtrl convertCoordinate:CLLocationCoordinate2DMake(lat, lng)];

    NSDictionary *point = @{@"x" : @(screenPoint.x), @"y" : @(screenPoint.y)};

    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:point];

    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) convertPoint:(CDVInvokedUrlCommand *)command {
    NSDictionary *args = command.arguments[1];

    float x = [[args valueForKey:@"x"] floatValue];
    float y = [[args valueForKey:@"y"] floatValue];

    if ((x < 0 || y < 0)){
        CDVPluginResult * pluginResult = [CDVPluginResult
                resultWithStatus:CDVCommandStatus_ERROR
                 messageAsString:@"Incorrect Leaflet.Point point coordinates."];
        [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }

    CLLocationCoordinate2D location = [_mapCtrl convertPoint:CGPointMake(x, y)];

    NSDictionary *coordinates = @{@"lat" : @(location.latitude), @"lng" : @(location.longitude)};

    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:coordinates];

    [_cdvMapbox.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)destroy {
    [_mapCtrl.overlayManager removeAllObjects];
    [_cdvMapbox.pluginScrollView dettachView];
    [_mapCtrl.view removeFromSuperview];
    _cdvMapbox.mapOverlayLayer.mapFrame = CGRectMake(0.0,0.0,0.0,0.0);
    _cdvMapbox.mapOverlayLayer.mapCtrl = nil;
    _mapCtrl = nil;
}


@end