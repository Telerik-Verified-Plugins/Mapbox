//
// Created by vikti on 30/03/
// Can hold several maps. Take a command from CDVMapbox and manipulate a map.
// The advantage to have all map in the same controller is it knows all the maps states. So advanced features
// could be doable such syncing twos maps together.
//

#import "MapController.h"
#import "MGLPointAnnotation.h"
#import "CDVMapbox.h"

@interface MapController()
{
@private
    NSDictionary* _initArgs;
    CDVMapbox *_cdvMapbox;
    CGRect _mapFrame;
}
@end

@implementation MapController

- (id)initWithArgs:(NSDictionary *)args withMapFrame:(CGRect)mapFrame withCDVMapboxPlugin:(CDVMapbox*) plugin {
    self = [super init];
    _initArgs = [[NSDictionary alloc] initWithDictionary:args];
    _cdvMapbox = plugin;
    _mapFrame = mapFrame;
    self.webView = _cdvMapbox.webView;

    return self;
}

- (void)setFrame:(CGRect)mapFrame {
    self.mapView.frame = _mapFrame = mapFrame;
}

- (CGRect)getFrame{
    return _mapFrame;
}

- (void)viewDidLoad{

    // Create an instance of Map Class
    self.mapView = [[MGLMapView alloc] initWithFrame:_mapFrame
                                            styleURL:[self getMapStyle:_initArgs[@"style"]]];

    self.mapView.delegate = self;

    self.mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

    NSNumber *zoomLevel = _initArgs[@"zoomLevel"];
    if (zoomLevel == nil) {
        // we need a default
        zoomLevel = @10.0;
    }
    NSDictionary *center = _initArgs[@"center"];
    if (center != nil) {
        NSNumber *clat = [center valueForKey:@"lat"];
        NSNumber *clng = [center valueForKey:@"lng"];
        [self.mapView setCenterCoordinate:CLLocationCoordinate2DMake(clat.doubleValue, clng.doubleValue)
                           zoomLevel:zoomLevel.doubleValue
                            animated:NO];
    } else {
        [self.mapView setZoomLevel:zoomLevel.doubleValue];
    }


    // default NO, note that this requires adding `NSLocationWhenInUseUsageDescription` or `NSLocationAlwaysUsageDescription` to the plist
    self.mapView.showsUserLocation = [_initArgs[@"showUserLocation"] boolValue];

    // default NO
    self.mapView.attributionButton.hidden = [_initArgs[@"hideAttribution"] boolValue];

    // default NO - required for the 'starter' plan
    self.mapView.logoView.hidden = [_initArgs[@"hideLogo"] boolValue];

    // default NO
    self.mapView.compassView.hidden = [_initArgs[@"hideCompass"] boolValue];
    // sibling the plugin layer and the webview. Each times an event occurs, [mapOverlayLayer hitTest] is called.
    // It the touch occurs in the map area (and don't touch an overlay DOM element) the event is passed to the map.

    // default YES
    self.mapView.rotateEnabled = ![_initArgs[@"disableRotation"] boolValue];

    // default YES
    self.mapView.pitchEnabled = ![_initArgs[@"disablePitch"] boolValue];

    // default YES
    //self.mapView.allowsTilting = ![_initArgs[@"disableTilt"] boolValue];

    // default YES
    self.mapView.scrollEnabled = ![_initArgs[@"disableScroll"] boolValue];

    // default YES
    self.mapView.zoomEnabled = ![_initArgs[@"disableZoom"] boolValue];

    if (_initArgs[@"markers"]) [self putMarkersOnTheMap:_initArgs[@"markers"]];

    _initArgs = nil; //will not be used again

    [self.view addSubview: self.mapView];
}

- (NSURL*) getMapStyle:(NSString*) input {
    if ([input isEqualToString:@"light"]) {
        return [MGLStyle lightStyleURL];
    } else if ([input isEqualToString:@"dark"]) {
        return [MGLStyle darkStyleURL];
    } else if ([input isEqualToString:@"emerald"]) {
        return [MGLStyle emeraldStyleURL];
    } else if ([input isEqualToString:@"satellite"]) {
        return [MGLStyle satelliteStyleURL];
    } else if ([input isEqualToString:@"hybrid"]) {
        return [MGLStyle hybridStyleURL];
    } else {
        // default (TODO allow an arbitrary url (see Android))
        return [MGLStyle streetsStyleURL];
    }
}

- (CGPoint)convertCoordinate:(CLLocationCoordinate2D)coordinates {
    CGPoint screenPoint = [self.mapView  convertCoordinate:coordinates
                                             toPointToView:self.mapView];

    return screenPoint;
}

- (CLLocationCoordinate2D)convertPoint:(CGPoint)point {
    CLLocationCoordinate2D coordinates = [self.mapView convertPoint:point
                                               toCoordinateFromView:self.mapView];

    return coordinates;
}

- (void)putMarkersOnTheMap:(NSArray *)markers {
    [_cdvMapbox.commandDelegate runInBackground:^{
        for (int i = 0; i < markers.count; i++) {
            NSDictionary* marker = markers[i];
            MGLPointAnnotation *point = [[MGLPointAnnotation alloc] init];
            NSNumber *lat = [marker valueForKey:@"lat"];
            NSNumber *lng = [marker valueForKey:@"lng"];
            point.coordinate = CLLocationCoordinate2DMake(lat.doubleValue, lng.doubleValue);
            point.title = [marker valueForKey:@"title"];
            point.subtitle = [marker valueForKey:@"subtitle"];
            [self.mapView addAnnotation:point];
        }
    }];
}


// this method is invoked every time an annotation is clicked
- (BOOL)mapView:(MGLMapView *)mapView annotationCanShowCallout:(id <MGLAnnotation>)annotation {
    return YES;
}

- (nullable UIView *)mapView:(MGLMapView *)mapView rightCalloutAccessoryViewForAnnotation:(MGLPointAnnotation *)annotation {
    if (self.markerCallbackId != nil) {
        self.selectedAnnotation = annotation;
        UIButton *butt = [UIButton buttonWithType:UIButtonTypeDetailDisclosure];
        [butt addTarget:self action:@selector(annotationInfoButtonTouched:) forControlEvents:UIControlEventTouchDown];
        return butt;
    } else {
        return nil;
    }
}

- (void) annotationInfoButtonTouched:(UIButton *)sender {
    if (self.markerCallbackId != nil && self.selectedAnnotation != nil) {
        NSMutableDictionary* returnInfo = [NSMutableDictionary dictionaryWithCapacity:4];
        returnInfo[@"title"] = self.selectedAnnotation.title;
        if (self.selectedAnnotation.subtitle != nil) {
            returnInfo[@"subtitle"] = self.selectedAnnotation.subtitle;
        }
        returnInfo[@"lat"] = @(self.selectedAnnotation.coordinate.latitude);
        returnInfo[@"lng"] = @(self.selectedAnnotation.coordinate.longitude);

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
        [result setKeepCallbackAsBool:YES];
        [_cdvMapbox.commandDelegate sendPluginResult:result callbackId:self.markerCallbackId];
    }
}

- (NSMutableDictionary*) getResultOnMapChange{

    NSMutableDictionary* returnInfo = [NSMutableDictionary dictionary];
    MGLMapCamera* camera = self.mapView.camera;

    returnInfo[@"lat"] = @(self.mapView.centerCoordinate.latitude);
    returnInfo[@"lng"] = @(self.mapView.centerCoordinate.longitude);
    returnInfo[@"camAltitude"] = @(self.mapView.camera.altitude);
    returnInfo[@"camPitch"] = @(self.mapView.camera.pitch);
    returnInfo[@"camHeading"] = @(self.mapView.camera.heading);

    return returnInfo;
}

- (void)mapView:(nonnull MGLMapView *)mapView regionWillChangeAnimated:(BOOL)animated {
    if (self.regionWillChangeAnimatedCallbackId != nil) {

        NSMutableDictionary* returnInfo = [self getResultOnMapChange];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
        [result setKeepCallbackAsBool:YES];
        [_cdvMapbox.commandDelegate sendPluginResult:result callbackId:self.regionWillChangeAnimatedCallbackId];
    }
};

- (void)mapViewRegionIsChanging:(nonnull MGLMapView *)mapView{
    if (self.regionIsChangingCallbackId != nil) {

        NSMutableDictionary* returnInfo = [self getResultOnMapChange];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
        [result setKeepCallbackAsBool:YES];
        [_cdvMapbox.commandDelegate sendPluginResult:result callbackId:self.regionIsChangingCallbackId];
    }
};

- (void)mapView:(nonnull MGLMapView *)mapView regionDidChangeAnimated:(BOOL)animated{
    if (self.regionDidChangeAnimatedCallbackId != nil) {

        NSMutableDictionary* returnInfo = [self getResultOnMapChange];

        CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
        [result setKeepCallbackAsBool:YES];
        [_cdvMapbox.commandDelegate sendPluginResult:result callbackId:self.regionDidChangeAnimatedCallbackId];
    }
};

@end