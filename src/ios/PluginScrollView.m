//
// Created by vikti on 30/03/2016.
//

#import "PluginScrollView.h"
#import "MapController.h"

@implementation PluginScrollView

UIView *myView = nil;

-  (id)initWithFrame:(CGRect)aRect
{
    self = [super initWithFrame:aRect];
    return self;
}

- (void)attachView:(UIView *)view {
    myView = view;
    [self addSubview:view];
}
- (void)dettachView {
    [myView removeFromSuperview];
}
@end