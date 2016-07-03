//
// Created by vikti on 30/03/2016.
//

#import "PluginScrollLayout.h"
#import "MapController.h"
#import "MapOverlayDebugLayer.h"

@implementation PluginScrollLayout

UIView *myView = nil;

-  (id)initWithFrame:(CGRect)aRect {
    self = [super initWithFrame:aRect];
    self.debugView = [[MapOverlayDebugLayer alloc] initWithFrame:aRect];
    return self;
}

- (void)attachView:(UIView *)view {
    myView = view;
    [self addSubview:view];
    [self addSubview:self.debugView];
}
- (void)dettachView {
    [myView removeFromSuperview];
    [self.debugView removeFromSuperview];
}
@end