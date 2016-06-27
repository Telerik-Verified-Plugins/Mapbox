//
// Created by vikti on 30/03/2016.
//

#import "PluginScrollView.h"
#import "MapController.h"
#import "PluginDebugLayer.h"

@implementation PluginScrollView

UIView *myView = nil;

-  (id)initWithFrame:(CGRect)aRect {
    self = [super initWithFrame:aRect];
    self.debugView = [[PluginDebugLayer alloc] initWithFrame:aRect];
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