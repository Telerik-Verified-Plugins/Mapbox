#import "MapOverlayDebugLayer.h"
#import "PluginLayout.h"

@implementation MapOverlayDebugLayer


-  (id)initWithFrame:(CGRect)aRect
{
    self = [super initWithFrame:aRect];
    self.opaque = NO;
    self.debuggable = NO;
    self.clickable = YES;
    return self;
}

- (void)drawRect:(CGRect)rect
{
    if (self.debuggable == NO) {
        return;
    }
    CGContextRef context = UIGraphicsGetCurrentContext();

    if (self.clickable == NO) {
        CGContextSetRGBFillColor(context, 0.0, 1.0, 0, 0.4);
        CGContextFillRect(context, rect);
        return;
    }
    float offsetX = self.webView.scrollView.contentOffset.x;// + self.mapCtrl.view.frame.origin.x;
    float offsetY = self.webView.scrollView.contentOffset.y;// + self.mapCtrl.view.frame.origin.y;

    float left = self.pluginLayer.mapFrame.origin.x - offsetX;
    float top = self.pluginLayer.mapFrame.origin.y - offsetY;
    float width = self.pluginLayer.mapFrame.size.width;
    float height = self.pluginLayer.mapFrame.size.height;

    CGRect rectangle = CGRectMake(0, 0, 0, 0);

    //---------------------------------
    // Draw the HTML elements region
    //---------------------------------
    CGContextSetRGBFillColor(context, 1.0, 0, 0, 0.4);
    NSDictionary *elemSize;
    for (NSString *domId in self.pluginLayer.HTMLNodes) {
        elemSize = self.pluginLayer.HTMLNodes[domId];
        left = [elemSize[@"left"] floatValue] - self.offsetX;
        top = [elemSize[@"top"] floatValue] - self.offsetY;
        width = [elemSize[@"width"] floatValue];
        height = [elemSize[@"height"] floatValue];

        rectangle.origin.x = left;
        rectangle.origin.y = top;
        rectangle.size.width = width;
        rectangle.size.height = height;
        CGContextFillRect(context, rectangle);

    }

}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    [self setNeedsDisplay];
    return [super hitTest:point withEvent:event];
}

@end