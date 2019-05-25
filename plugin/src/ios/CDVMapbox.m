#import "CDVMapbox.h"

@interface CDVMapbox(){
@private
  MapsManager *_mapsManager;
}
@end

@implementation CDVMapbox

- (void)pluginInitialize {

#if CORDOVA_VERSION_MIN_REQUIRED >= __CORDOVA_4_0_0
  self.webView.backgroundColor = [UIColor clearColor];
  self.webView.opaque = NO;
  [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(pageDidLoad) name:CDVPageDidLoadNotification object:nil];
#endif

  /* Init the plugin layer responsible to capture touch events.
   * It permits to have Dom Elements on top of the map.
   * If a touch event occurs in one of the embed rectangles and outside of a inner html element,
   * the plugin layer considers that is a map action (drag, pan, etc.).
   * If not, the user surely want to access the UIWebView.
  */
  self.mapOverlayLayer = [[MapOverlayLayer alloc] initWithFrame:self.webView.frame];
  self.mapOverlayLayer.webView = self.webView;
  self.mapOverlayLayer.backgroundColor = [UIColor whiteColor];
  self.mapOverlayLayer.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

  /* Init a scroll view which on are attached the maps. This enables the map views to track the UIWebView.
   * This scroll view is synchronised with the web view UIScrollView thanks to the UIScrollViewDelegate functions
   */
  self.pluginScrollView = [[PluginScrollLayout alloc] initWithFrame:self.webView.frame];
  self.pluginScrollView.debugView.pluginLayer = self.mapOverlayLayer; //todo make a global var to active debug mode
  self.pluginScrollView.debugView.webView = (UIWebView *) self.webView;
  self.webView.scrollView.delegate = self;
  self.pluginScrollView.autoresizingMask = UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight;

  [self.pluginScrollView setContentSize:CGSizeMake(320, 960)];

  [self.mapOverlayLayer addSubview:self.pluginScrollView];

  /* Get all the current sub views of this view controller and pass it under the Plugin Layer.
   * Then, all user touch will be first intercepted by the plugin layer
   * which will decide whether or not it is a map action.
   */
  NSArray *subViews = self.viewController.view.subviews;
  UIView *view;
  for (int i = 0; i < [subViews count]; i++) {
    view = subViews[(NSUInteger) i];
    [view removeFromSuperview];
    [self.mapOverlayLayer addSubview:view];
  }

  [self.viewController.view addSubview:self.mapOverlayLayer];



  /* Create a MapsManager to handle multiple maps.
   * Each map has an ID.
   * At each creation a mapFrame is added to the MapOverlayLayer and the PluginScrollLayout
   */
  _mapsManager = [[MapsManager alloc] initWithCDVPlugin:self withCDVMapboxPlugin:self withAccessToken:[MGLAccountManager accessToken]];
}

/**
 * Make the web view background transparent to see the map through. Override CSS effect.
 */
-(void)pageDidLoad {
  self.webView.backgroundColor = [UIColor clearColor];
  self.webView.opaque = NO;
}

/**
 * Synchronise the pluginScrollView delegate to the main web view scroll view.
 * So the maps follow when the user pan the web page.
 */
- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
  CGPoint offset = self.pluginScrollView.contentOffset;
  offset.x = self.webView.scrollView.contentOffset.x;
  offset.y = self.webView.scrollView.contentOffset.y;
  [self.pluginScrollView setContentOffset:offset];
  [self.pluginScrollView.debugView setNeedsDisplay];
}

/**
 * Resize the plugin scroll view when the webview changes. (eg. Portrait <-> landscape)
 */
-(void)viewDidLayoutSubviews {
  [self.pluginScrollView setContentSize: self.webView.scrollView.contentSize];
  [self.pluginScrollView flashScrollIndicators];
}

// todo handle show specific map id
- (void) show:(CDVInvokedUrlCommand *)command {

  int id = [command.arguments[0] intValue];
  NSMutableDictionary *args = command.arguments[1];

  // create map if id does not exist
  if(![_mapsManager getCcount] || [_mapsManager getMap:id == nil]){

    Map *map = [self createMap:args withId:id];

    args[@"id"] = @((NSInteger) map.id);
  }
  //todo remvove as create remove do the job
  // or execute the original command
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap show:aCommand];
  }];
}

- (void) hide:(CDVInvokedUrlCommand *)command{
  int id = [command.arguments[0] intValue];
  [_mapsManager removeMap:id];
}

- (void) refreshMap:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap refreshMap:aCommand];
  }];
}

//todo refactor when implement multi maps
- (void) setClickable:(CDVInvokedUrlCommand *)command{
  self.mapOverlayLayer.clickable = [command.arguments[1][@"clickable"] boolValue];
}

- (void) onRegionWillChange:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap onRegionWillChange:aCommand];
  }];
}
- (void) onRegionIsChanging:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap onRegionIsChanging:aCommand];
  }];
}
- (void) onRegionDidChange:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap onRegionDidChange:aCommand];
  }];
}
- (void) setCenter:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap setCenterCoordinates:aCommand];
  }];
}
- (void) getCenter:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap getCenterCoordinates:aCommand];
  }];
}
- (void) getZoomLevel:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap getZoomLevel:aCommand];
  }];
}
- (void) getBoundsCoordinates:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap getBoundsCoordinates:aCommand];
  }];
}
- (void) setZoomLevel:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap setZoomLevel:aCommand];
  }];
}
- (void) getTilt:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap getTilt:aCommand];
  }];
}
- (void) setTilt:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap setTilt:aCommand];
  }];
}
- (void) animateCamera:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap animateCamera:aCommand];
  }];
}
- (void) addPolygon:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap addPolygon:aCommand];
  }];
}
- (void) addGeoJSON:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap addGeoJSON:aCommand];
  }];
}
- (void) addMarkers:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap addMarkers:aCommand];
  }];
}
- (void) addMarkerCallback:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap addMarkerCallback:aCommand];
  }];
}
- (void) convertCoordinates:(CDVInvokedUrlCommand *)command{
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand){
      [aMap convertCoordinates:aCommand];
  }];
}
- (void) convertPoint:(CDVInvokedUrlCommand *)command {
  [self exec:command withMethod:^(Map *aMap, CDVInvokedUrlCommand *aCommand) {
      [aMap convertPoint:aCommand];
  }];
}

- (void)exec:(CDVInvokedUrlCommand *)command withMethod:(void (^)(Map*, CDVInvokedUrlCommand*))execute_map_method {
  Map *map = [_mapsManager getMap:[command.arguments[0] intValue]];
  execute_map_method(map, command);
}

- (Map*) createMap:(NSDictionary*)args withId:(int)anId{
//  [self.commandDelegate runInBackground:^{

  Map* map = [_mapsManager createMap:args withId:anId||nil];

  //CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsInt: (int) map.id];
  // keep the callback because there are various events the developer may be interested in
  //pluginResult.keepCallback = @YES;
  return map;
  //}];
}

- (void) onPause:(Boolean *)multitasking{

}

- (void) onResume:(Boolean *)multitasking{

}

- (void) onDestroy{

}

- (float) getRetinaFactor{
  return 1.0;
}

@end
