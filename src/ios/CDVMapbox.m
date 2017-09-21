#import "CDVMapbox.h"

@implementation CDVMapbox

- (void) show:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];

  NSURL* mapStyle = [self getMapStyle:[args objectForKey:@"style"]];

  // where shall we show the map overlay?
  NSDictionary *margins = [args objectForKey:@"margins"];
  // note that these will correctly fall back to 0 if not passed in
  int left = [[margins objectForKey:@"left"] intValue];
  int right = [[margins objectForKey:@"right"] intValue];
  int top = [[margins objectForKey:@"top"] intValue];
  int bottom = [[margins objectForKey:@"bottom"] intValue];

  CGRect webviewFrame = self.webView.frame;

  CGRect mapFrame = CGRectMake(left, top, webviewFrame.size.width - left - right, webviewFrame.size.height - top - bottom);

  _mapView = [[MGLMapView alloc] initWithFrame:mapFrame
                                      styleURL:mapStyle];

  _mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

  NSNumber *zoomLevel = [args valueForKey:@"zoomLevel"];
  if (zoomLevel == nil) {
    // we need a default
    zoomLevel = [NSNumber numberWithDouble:10.0];
  }
  NSDictionary *center = [args objectForKey:@"center"];
  if (center != nil) {
    NSNumber *clat = [center valueForKey:@"lat"];
    NSNumber *clng = [center valueForKey:@"lng"];
    [_mapView setCenterCoordinate:CLLocationCoordinate2DMake(clat.doubleValue, clng.doubleValue)
                        zoomLevel:zoomLevel.doubleValue
                         animated:NO];
  } else {
    [_mapView setZoomLevel:zoomLevel.doubleValue];
  }


  _mapView.delegate = self;

  // default NO, note that this requires adding `NSLocationWhenInUseUsageDescription` or `NSLocationAlwaysUsageDescription` to the plist
  _mapView.showsUserLocation = [[args objectForKey:@"showUserLocation"] boolValue];

  // default NO
  _mapView.attributionButton.hidden = [[args objectForKey:@"hideAttribution"] boolValue];

  // default NO - required for the 'starter' plan
  _mapView.logoView.hidden = [[args objectForKey:@"hideLogo"] boolValue];

  // default NO
  _mapView.compassView.hidden = [[args objectForKey:@"hideCompass"] boolValue];

  // default YES
  _mapView.rotateEnabled = ![[args objectForKey:@"disableRotation"] boolValue];

  // default YES
  _mapView.pitchEnabled = ![[args objectForKey:@"disablePitch"] boolValue];

  // default YES
  _mapView.allowsTilting = ![[args objectForKey:@"disableTilt"] boolValue];

  // default YES
  _mapView.scrollEnabled = ![[args objectForKey:@"disableScroll"] boolValue];

  // default YES
  _mapView.zoomEnabled = ![[args objectForKey:@"disableZoom"] boolValue];

  [self.webView addSubview:_mapView];

  // render markers async as the app will crash if we add it before the map is loaded.. and the delegate events are not sufficiently helpful
  NSArray* markers = [args objectForKey:@"markers"];
  if (markers != nil) {
    // Draw the markers after the map has initialized
    [self performSelector:@selector(putMarkersOnTheMap:) withObject:markers afterDelay:1.0];
  }

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) hide:(CDVInvokedUrlCommand*)command {
  [_mapView removeFromSuperview];

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) getCenter:(CDVInvokedUrlCommand*)command {
    CLLocationCoordinate2D ctr = _mapView.centerCoordinate;
    NSDictionary *dic = [NSDictionary dictionaryWithObjectsAndKeys:
                         [NSNumber numberWithDouble:ctr.latitude], @"lat",
                         [NSNumber numberWithDouble:ctr.longitude], @"lng",
                         nil];
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) setCenter:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];
  NSNumber *clat = [args valueForKey:@"lat"];
  NSNumber *clng = [args valueForKey:@"lng"];
  BOOL animated = [[args objectForKey:@"animated"] boolValue];
  [_mapView setCenterCoordinate:CLLocationCoordinate2DMake(clat.doubleValue, clng.doubleValue) animated:animated];

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) setTilt:(CDVInvokedUrlCommand*)command {
  // TODO tilt/pitch seems not to be implemented in Mapbox iOS SDK (yet)
  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not implemented for iOS (yet)"];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) getTilt:(CDVInvokedUrlCommand*)command {
  // TODO seems not to be implemented in Mapbox iOS SDK (yet)
  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not implemented for iOS (yet)"];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) setZoomLevel:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];
  NSNumber *level = [args objectForKey:@"level"];
  BOOL animated = [[args objectForKey:@"animated"] boolValue];
  double zoom = level.doubleValue;
  if (zoom >= 0 && zoom <= 20) {
    [_mapView setZoomLevel:zoom animated:animated];
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  } else {
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"invalid zoomlevel, use any double value from 0 to 20 (like 8.3)"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }
}

- (void) getZoomLevel:(CDVInvokedUrlCommand*)command {
  double zoom = _mapView.zoomLevel;
  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDouble:zoom];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) getBounds:(CDVInvokedUrlCommand*)command {
    MGLCoordinateBounds bounds = _mapView.visibleCoordinateBounds;
    NSDictionary *dic = [NSDictionary dictionaryWithObjectsAndKeys:
                         [NSNumber numberWithDouble:bounds.sw.latitude], @"sw_lat",
                         [NSNumber numberWithDouble:bounds.sw.longitude], @"sw_lng",
                         [NSNumber numberWithDouble:bounds.ne.latitude], @"ne_lat",
                         [NSNumber numberWithDouble:bounds.ne.longitude], @"ne_lng",
                         nil];
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) setBounds:(CDVInvokedUrlCommand*)command {
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"not implemented for iOS (yet)"];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)animateCamera:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];

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

  NSDictionary *target = [args objectForKey:@"target"];
  if (target != nil) {
    NSNumber *clat = [target valueForKey:@"lat"];
    NSNumber *clng = [target valueForKey:@"lng"];
    cam.centerCoordinate = CLLocationCoordinate2DMake(clat.doubleValue, clng.doubleValue);
  }

  [_mapView setCamera:cam withDuration:durInt animationTimingFunction:[CAMediaTimingFunction functionWithName:kCAMediaTimingFunctionEaseInEaseOut]];

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void)addPolygon:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];
  NSArray* points = [args objectForKey:@"points"];
  if (points != nil) {
    [self.commandDelegate runInBackground:^{
        CLLocationCoordinate2D *coordinates = malloc(points.count * sizeof(CLLocationCoordinate2D));
        for (int i=0; i<points.count; i++) {
          NSDictionary* point = points[i];
          NSNumber *lat = [point valueForKey:@"lat"];
          NSNumber *lng = [point valueForKey:@"lng"];
          coordinates[i] = CLLocationCoordinate2DMake(lat.doubleValue, lng.doubleValue);
        }
        NSUInteger numberOfCoordinates = points.count; // sizeof(coordinates) / sizeof(CLLocationCoordinate2D);
        MGLPolygon *shape = [MGLPolygon polygonWithCoordinates:coordinates count:numberOfCoordinates];
        [_mapView addAnnotation:shape];
        CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
    }];
  } else {
    CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }
}

- (void) addGeoJSON:(CDVInvokedUrlCommand*)command {
//  NSString *url = [command.arguments objectAtIndex:0];
// TODO not implemented yet, see https://www.mapbox.com/ios-sdk/examples/line-geojson/
  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) addMarkers:(CDVInvokedUrlCommand*)command {
  NSArray *markers = [command.arguments objectAtIndex:0];
  if (markers != nil) {
    [self putMarkersOnTheMap:markers];
  }

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) removeAllMarkers:(CDVInvokedUrlCommand*)command {
  NSArray *annotations = _mapView.annotations;
  if (annotations) {
    [_mapView removeAnnotations:annotations];
  }

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) addMarkerCallback:(CDVInvokedUrlCommand*)command {
  self.markerCallbackId = command.callbackId;
}

- (void) putMarkersOnTheMap:(NSArray *)markers {
  [self.commandDelegate runInBackground:^{
      for (int i = 0; i < markers.count; i++) {
        NSDictionary* marker = markers[i];
        MGLPointAnnotation *point = [[MGLPointAnnotation alloc] init];
        NSNumber *lat = [marker valueForKey:@"lat"];
        NSNumber *lng = [marker valueForKey:@"lng"];
        point.coordinate = CLLocationCoordinate2DMake(lat.doubleValue, lng.doubleValue);
        point.title = [marker valueForKey:@"title"];
        point.subtitle = [marker valueForKey:@"subtitle"];
        [_mapView addAnnotation:point];
      }
  }];
}

- (void) convertCoordinate:(CDVInvokedUrlCommand *)command {
  NSDictionary *args = command.arguments[0];

  double lat = [[args valueForKey:@"lat"]doubleValue];
  double lng = [[args valueForKey:@"lng"]doubleValue];

  if ((fabs(lat) > 90)||(fabs(lng) > 180)){
    CDVPluginResult * pluginResult = [CDVPluginResult
            resultWithStatus:CDVCommandStatus_ERROR
             messageAsString:@"Incorrect Leaflet.LatLng value."];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }

  CGPoint screenPoint = [_mapView  convertCoordinate:CLLocationCoordinate2DMake(lat, lng)
                                       toPointToView:_mapView];

  NSDictionary *point = @{@"x" : @(screenPoint.x), @"y" : @(screenPoint.y)};

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:point];

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) convertPoint:(CDVInvokedUrlCommand *)command {
  NSDictionary *args = command.arguments[0];

  float x = [[args valueForKey:@"x"] floatValue];
  float y = [[args valueForKey:@"y"] floatValue];

  if ((x < 0 || y < 0)){
    CDVPluginResult * pluginResult = [CDVPluginResult
            resultWithStatus:CDVCommandStatus_ERROR
             messageAsString:@"Incorrect Leaflet.Point point coordinates."];
    [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
  }

  CLLocationCoordinate2D location = [_mapView convertPoint:CGPointMake(x, y)
                                      toCoordinateFromView:_mapView];

  NSDictionary *coordinates = @{@"lat" : @(location.latitude), @"lng" : @(location.longitude)};

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:coordinates];

  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) onRegionWillChange:(CDVInvokedUrlCommand*)command {
  self.regionWillChangeAnimatedCallbackId = command.callbackId;
}

- (void) onRegionIsChanging:(CDVInvokedUrlCommand*)command {
  self.regionIsChangingCallbackId = command.callbackId;
}

- (void) onRegionDidChange:(CDVInvokedUrlCommand*)command {
  self.regionDidChangeAnimatedCallbackId = command.callbackId;
}
#pragma mark - MGLMapViewDelegate

// this method is invoked every time an annotation is clicked
- (BOOL)mapView:(MGLMapView *)mapView annotationCanShowCallout:(id <MGLAnnotation>)annotation {
  return YES;
}

//- (MGLAnnotationImage *)mapView:(MGLMapView *)mapView imageForAnnotation:(id <MGLAnnotation>)annotation {
// TODO should be able to use an img from www/
//  MGLAnnotationImage *annotationImage = [mapView dequeueReusableAnnotationImageWithIdentifier:@"pisa"];

//  if (!annotationImage) {
// Leaning Tower of Pisa by Stefan Spieler from the Noun Project
//    UIImage *image = [UIImage imageNamed:@"pisa"];
//    annotationImage = [MGLAnnotationImage annotationImageWithImage:image reuseIdentifier:@"pisa"];
//  }

//  return annotationImage;
//}

- (nullable UIView *)mapView:(MGLMapView *)mapView rightCalloutAccessoryViewForAnnotation:(id <MGLAnnotation>)annotation {
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
    [returnInfo setObject:self.selectedAnnotation.title forKey:@"title"];
    if (self.selectedAnnotation.subtitle != nil) {
      [returnInfo setObject:self.selectedAnnotation.subtitle forKey:@"subtitle"];
    }
    [returnInfo setObject:[NSNumber numberWithDouble:self.selectedAnnotation.coordinate.latitude] forKey:@"lat"];
    [returnInfo setObject:[NSNumber numberWithDouble:self.selectedAnnotation.coordinate.longitude] forKey:@"lng"];

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.markerCallbackId];
  }
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
  } else if ( input != nil ) {
    NSURL *url = [NSURL URLWithString:input];
    return url;
  } else {
    return [MGLStyle streetsStyleURL];
  }
}

- (NSMutableDictionary*) getResultOnMapChange{

  NSMutableDictionary* returnInfo = [NSMutableDictionary dictionary];
  MGLMapCamera* camera = _mapView.camera;

  returnInfo[@"lat"] = @(_mapView.centerCoordinate.latitude);
  returnInfo[@"lng"] = @(_mapView.centerCoordinate.longitude);
  returnInfo[@"camAltitude"] = @(_mapView.camera.altitude);
  returnInfo[@"camPitch"] = @(_mapView.camera.pitch);
  returnInfo[@"camHeading"] = @(_mapView.camera.heading);

  return returnInfo;
}


- (void)mapView:(nonnull MGLMapView *)mapView regionWillChangeAnimated:(BOOL)animated {
  if (self.regionWillChangeAnimatedCallbackId != nil) {

    NSMutableDictionary* returnInfo = [self getResultOnMapChange];

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.regionWillChangeAnimatedCallbackId];
  }
};

- (void)mapViewRegionIsChanging:(nonnull MGLMapView *)mapView{
  if (self.regionIsChangingCallbackId != nil) {

    NSMutableDictionary* returnInfo = [self getResultOnMapChange];

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.regionIsChangingCallbackId];
  }
};

- (void)mapView:(nonnull MGLMapView *)mapView regionDidChangeAnimated:(BOOL)animated{
  if (self.regionDidChangeAnimatedCallbackId != nil) {

    NSMutableDictionary* returnInfo = [self getResultOnMapChange];

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.regionDidChangeAnimatedCallbackId];
  }
};

@end
