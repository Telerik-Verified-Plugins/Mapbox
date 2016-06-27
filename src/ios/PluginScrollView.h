//
// Created by vikti on 30/03/2016.
//

#import <UIKit/UIKit.h>
#import "PluginDebugLayer.h"

@class MapController;

@interface PluginScrollView : UIScrollView
@property (nonatomic) PluginDebugLayer *debugView;
- (void)attachView:(UIView *)view;
- (void)dettachView;
@end