//
// Created by vikti on 30/03/2016.
//

#import <UIKit/UIKit.h>
#import "MapDebugLayer.h"

@class MapController;

@interface MapScrollLayer : UIScrollView
@property (nonatomic) MapDebugLayer *debugView;
- (void)attachView:(UIView *)view;
- (void)dettachView;
@end