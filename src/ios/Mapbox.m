#import "Mapbox.h"

@implementation Mapbox

// TODO loc services for starter plan:https://www.mapbox.com/guides/first-steps-gl-ios/

// TODO pass accesstoken as preference in plugin.xml, we've now set this .plist var manually: MGLMapboxAccessToken
- (void) show:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];
  // save annotations for later as the app will crash if we add it before the map is loaded
  _queuedAnnotations = [args objectForKey:@"annotations"];

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
  
  _mapView = [[MGLMapView alloc] initWithFrame:mapFrame styleURL:[NSURL URLWithString:[NSString stringWithFormat:@"asset://styles/%@-v7.json", mapStyle]]];

  _mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

  NSNumber *zoom = [args valueForKey:@"zoom"];
  NSDictionary *center = [args objectForKey:@"center"];
  if (center != nil) {
    // TODO can we set zoom independently?
    NSNumber *clat = [center valueForKey:@"lat"];
    NSNumber *clng = [center valueForKey:@"lng"];
    [_mapView setCenterCoordinate:CLLocationCoordinate2DMake(clat.doubleValue, clng.doubleValue)
                        zoomLevel:zoom.intValue
                         animated:NO];
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
  _mapView.scrollEnabled = ![[args objectForKey:@"disableScroll"] boolValue];
  
  // default YES
  _mapView.zoomEnabled = ![[args objectForKey:@"disableZoom"] boolValue];

  [self.webView addSubview:_mapView];

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  // keep the callback because there are various events the developer may be interested in
  pluginResult.keepCallback = [NSNumber numberWithBool:YES];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) hide:(CDVInvokedUrlCommand*)command {
  [_mapView removeFromSuperview];
}

- (void) addAnnotations:(CDVInvokedUrlCommand*)command {
  NSArray *annotations = [command.arguments objectAtIndex:0];
  [self putAnnotationsOnTheMap:annotations];

  CDVPluginResult * pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
  [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
}

- (void) addAnnotationCallback:(CDVInvokedUrlCommand*)command {
  self.annotationCallbackId = command.callbackId;
}

- (void) putAnnotationsOnTheMap:(NSArray *)annotations {
  if (annotations != nil) {
    for (int i = 0; i < annotations.count; i++) {
      NSDictionary* annotation = annotations[i];
      MGLPointAnnotation *point = [[MGLPointAnnotation alloc] init];
      NSNumber *lat = [annotation valueForKey:@"lat"];
      NSNumber *lng = [annotation valueForKey:@"lng"];
      point.coordinate = CLLocationCoordinate2DMake(lat.doubleValue, lng.doubleValue);
      point.title = [annotation valueForKey:@"title"];
      point.subtitle = [annotation valueForKey:@"subtitle"];
      [_mapView addAnnotation:point];
    }
  }
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

// TODO I'd rather use 'mapViewDidFinishLoadingMap' (because this one is invoked more than once) but that one isn't called in Mapbox 0.5.1..
// TODO if you dont scroll this isnt fired... so better to just wrap it in a timeout of the show function
- (void)mapViewDidFinishRenderingMap:(MGLMapView *)mapView fullyRendered:(BOOL)fullyRendered {
  // process any queued annotations
  if (_queuedAnnotations != nil) {
    [self putAnnotationsOnTheMap:_queuedAnnotations];
    _queuedAnnotations = nil;
  }
}

- (void)mapViewDidFinishLoadingMap:(MGLMapView *)mapView {
}

- (void)mapView:(MGLMapView *)mapView didSelectAnnotation:(id <MGLAnnotation>)annotation {
  if (self.annotationCallbackId != nil) {
    NSMutableDictionary* returnInfo = [NSMutableDictionary dictionaryWithCapacity:4];
    [returnInfo setObject:annotation.title forKey:@"title"];
    [returnInfo setObject:annotation.subtitle forKey:@"subtitle"];
    [returnInfo setObject:[NSNumber numberWithDouble:annotation.coordinate.latitude] forKey:@"lat"];
    [returnInfo setObject:[NSNumber numberWithDouble:annotation.coordinate.longitude] forKey:@"lng"];

    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:returnInfo];
    [result setKeepCallbackAsBool:YES];
    [self.commandDelegate sendPluginResult:result callbackId:self.annotationCallbackId];
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
    return @"mapbox-streets";
  }
}

@end
