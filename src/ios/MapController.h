//
// Created by vikti on 30/03/2016.
//

#import <Cordova/CDV.h>
#import <UIKit/UIKit.h>
#import "MGLMapView.h"
#import "PluginLayout.h"
#import "PluginScrollLayout.h"
#import "MGLPointAnnotation.h"
#import "Mapbox.h"

@class CDVMapbox;

@interface MapController : UIViewController<MGLMapViewDelegate>

@property (nonatomic) MGLMapView *mapView;
@property (nonatomic, strong) UIView* webView;
@property (nonatomic) MapOverlayLayer *pluginLayer;
@property (nonatomic, strong) NSMutableDictionary* overlayManager;
@property (retain) MGLPointAnnotation *selectedAnnotation;
@property (retain) NSString *markerCallbackId;
@property (retain) NSString *regionWillChangeAnimatedCallbackId;
@property (retain) NSString *regionIsChangingCallbackId;
@property (retain) NSString *regionDidChangeAnimatedCallbackId;

- (id) initWithArgs:(NSDictionary *)options withMapFrame:(CGRect)mapFrame withCDVMapboxPlugin:(CDVMapbox *)plugin;
- (CGPoint) convertCoordinate:(CLLocationCoordinate2D)coordinates;
- (CLLocationCoordinate2D) convertPoint:(CGPoint)coordinates;

- (void)putMarkersOnTheMap:(NSArray *)markers;

- (void)setFrame:(CGRect) mapFrame;

- (CGRect)getFrame;
@end