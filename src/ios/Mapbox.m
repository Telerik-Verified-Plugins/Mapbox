#import "Mapbox.h"

@implementation Mapbox

/* JS API, also compatible with Android and {N} implementations

 {
   position: {
     // this should make the map overlay the entire screen except for 40px at the top
     'left': 0,
     'right': 0,
     'top': 40,
     'bottom': 0
   },
   style: 'emerald',
   annotations: [
     {
       'lat': 4.33434,
       'lng': 7.23232,
       'title': 'hi',
       'subtitle': 'lo',
       'image': 'www/img/annotations/hi.jpg' // TODO support this on a rainy day
     },
     {
       ..
     }
   ]
 }
 
*/

// TODO loc services for starter plan:https://www.mapbox.com/guides/first-steps-gl-ios/

// TODO pass accesstoken as preference in plugin.xml, we've now set this .plist var manually: MGLMapboxAccessToken
- (void) show:(CDVInvokedUrlCommand*)command {
  NSDictionary *args = [command.arguments objectAtIndex:0];
  // save annotations for later as the app will crash if we add it before the map is loaded
  _queuedAnnotations = [args objectForKey:@"annotations"];
//  [self bundleImage:[dic valueForKey:@"image"] withCallbackId:command.callbackId];

  // TODO pass in position in JS API
  CGRect webviewFrame = self.webView.frame;
  int left = 0;
  int right = 0;
  int top = 40;
  int bottom = 40;
  
  //  x, y, width, height
  CGRect mapFrame = CGRectMake(left, top, webviewFrame.size.width - left - right, webviewFrame.size.height - top - bottom);
  
  // TODO pass in, default don't set as it falls back to mapbox-streets
  // dark
  // emerald // quite pretty
  // light
  // mapbox-streets // default
  // satellite
  //[_mapView setStyleURL:[NSURL URLWithString:@"asset://styles/mapbox-streets-v7.json"]];
  _mapView = [[MGLMapView alloc] initWithFrame:mapFrame styleURL:[NSURL URLWithString:@"asset://styles/mapbox-streets-v7.json"]];

  _mapView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

  // TODO pass in lat, lng, zoom
  [_mapView setCenterCoordinate:CLLocationCoordinate2DMake(52.3702160, 4.8951680)
                     zoomLevel:13
                      animated:NO];

  // note that this requires adding `NSLocationWhenInUseUsageDescription` or `NSLocationAlwaysUsageDescription` to the plist
  _mapView.showsUserLocation = YES;

  _mapView.delegate = self;
  
  // TODO pass in, default NO
  _mapView.attributionButton.hidden = YES;

  // TODO pass in, default NO - required for the 'starter' plan
  _mapView.logoView.hidden = YES;

  // TODO pass in, default NO
  _mapView.compassView.hidden = YES;
  
  // TODO pass in, default YES
  _mapView.rotateEnabled = YES;

  // TODO pass in, default YES
  _mapView.scrollEnabled = YES;
  
  // TODO pass in, default YES
  _mapView.zoomEnabled = YES;

  
  [self.webView addSubview:_mapView];

//  NSMutableDictionary *args = [command.arguments objectAtIndex:0];

  
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

// TODO make sure this is invoked at all
- (void)mapViewDidFinishLoadingMap:(MGLMapView *)mapView {
  // process any queued annotations
  if (_queuedAnnotations != nil) {
    [self putAnnotationsOnTheMap:_queuedAnnotations];
    _queuedAnnotations = nil;
  }
}

- (void)mapView:(MGLMapView *)mapView didSelectAnnotation:(id <MGLAnnotation>)annotation {
  NSString *title = annotation.title;
  NSString *subtitle = annotation.subtitle;
  CLLocationCoordinate2D coord = annotation.coordinate;
  // TODO trigger the kept callback
}

@end
