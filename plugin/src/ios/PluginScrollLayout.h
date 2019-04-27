//
// Created by vikti on 30/03/2016.
//

#import <UIKit/UIKit.h>
#import "MapOverlayDebugLayer.h"

@class MapController;

@interface PluginScrollLayout : UIScrollView
@property (nonatomic) MapOverlayDebugLayer *debugView;
- (void)attachView:(UIView *)view;
- (void)dettachView;
@end