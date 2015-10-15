#import "CDVMapbox.h"

@implementation CDVMapbox

- (void) show:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];

  NSString* mapStyle = [self getMapStyle:[args objectForKey:@"style"]];

  // where shall we show the map overlay?
  NSDictionary *margins = [args objectForKey:@"margins"];
  // note that these will correctly fall back to 0 if not passed in
  int left = [[margins objectForKey:@"left"] intValue];
  int right = [[margins objectForKey:@"right"] intValue];
  int top = [[margins objectForKey:@"top"] intValue];
  int bottom = [[margins objectForKey:@"bottom"] intValue];

  CGRect webviewFrame = self.webView.frame;
  CGRect mapFrame = CGRectMake(left, top, webviewFrame.size.width - left - right, webviewFrame.size.height - top - bottom);
  
  _mapView = [[MGLMapView alloc] initWithFrame:mapFrame styleURL:[NSURL URLWithString:[NSString stringWithFormat:@"asset://styles/%@-v8.json", mapStyle]]];

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
  // keep the callback because there are various events the developer may be interested in
  pluginResult.keepCallback = [NSNumber numberWithBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) hide:(CDVInvokedUrlCommand*)command {
  [_mapView removeFromSuperview];
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

- (void) getCenter:(CDVInvokedUrlCommand*)command {
  CLLocationCoordinate2D ctr = _mapView.centerCoordinate;
  NSDictionary *dic = [NSDictionary dictionaryWithObjectsAndKeys:
                                        [NSNumber numberWithDouble:ctr.latitude], @"lat",
                                        [NSNumber numberWithDouble:ctr.longitude], @"lng",
                                        nil];
  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:dic];
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

// mapping the passed-in style here so we are future-proof
- (NSString*) getMapStyle:(NSString*) input {
  if ([input isEqualToString:@"light"]) {
    return @"light";
  } else if ([input isEqualToString:@"dark"]) {
    return @"dark";
  } else if ([input isEqualToString:@"emerald"]) {
    return @"emerald";
  } else if ([input isEqualToString:@"satellite"]) {
    return @"satellite";
  } else {
    // default
    return @"streets";
  }
}

@end
