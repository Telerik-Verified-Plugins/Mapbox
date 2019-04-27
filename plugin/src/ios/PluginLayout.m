//
// Created by vikti on 29/03/2016.
//

#import "PluginLayout.h"
#import "MapController.h"

@implementation MapOverlayLayer

-  (id)initWithFrame:(CGRect)aRect
{
    self = [super initWithFrame:aRect];
    self.HTMLNodes = [[NSMutableDictionary alloc] init];
    self.mapFrame = CGRectMake(0.0,0.0,0.0,0.0);
    self.clickable = YES;
    self.debuggable = NO;
    return self;
}
- (void)setHTMLElement:(NSString *)domId size:(NSMutableDictionary *)size {
    self.HTMLNodes[domId] = size;
    [self setNeedsDisplay];
}

- (void)deleteHTMLElement:(NSString *)domId {
    [self.HTMLNodes removeObjectForKey:domId];
    [self setNeedsDisplay];
}

- (void)clearHTMLElement {
    [self.HTMLNodes removeAllObjects];
    [self setNeedsDisplay];
}
// explanation here: https://github.com/mapsplugin/cordova-plugin-googlemaps/issues/844#issuecomment-200467799
- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    if (!self.clickable ||
            self.mapCtrl.mapView == nil ||
            self.mapCtrl.mapView.hidden) {
        return [super hitTest:point withEvent:event];
    }

    float offsetX = self.webView.scrollView.contentOffset.x;// + self.mapCtrl.view.frame.origin.x;
    float offsetY = self.webView.scrollView.contentOffset.y;// + self.mapCtrl.view.frame.origin.y;

    float left = self.mapFrame.origin.x - offsetX;
    float top = self.mapFrame.origin.y - offsetY;
    float width = self.mapFrame.size.width;
    float height = self.mapFrame.size.height;

    BOOL isMapAction = NO;

    isMapAction = point.x >= left && point.x <= (left + width) && point.y >= top && point.y <= (top + height);

    if (isMapAction) {
        NSDictionary *elemSize;
        for (NSString *domId in self.HTMLNodes) {
            elemSize = self.HTMLNodes[domId];
            left = [elemSize[@"left"] floatValue] - offsetX;
            top = [elemSize[@"top"] floatValue] - offsetY;
            width = [elemSize[@"width"] floatValue];
            height = [elemSize[@"height"] floatValue];

            if (point.x >= left && point.x <= (left + width) &&
                    point.y >= top && point.y <= (top + height)) {
                isMapAction = NO;
                break;
            }
        }
    }
    
    if (isMapAction) {
        offsetX = self.webView.frame.origin.x - offsetX;
        offsetY = self.webView.frame.origin.y - offsetY;
        point.x -= offsetX;
        point.y -= offsetY;

        UIView *hitView =[self.mapCtrl.view hitTest:point withEvent:event];
        NSString *hitClass = [NSString stringWithFormat:@"%@", [hitView class]];

        //todo exclude native gui buttons
        /*
        if ([hitClass isEqualToString:@"UIButton"]
        && self.mapCtrl.mapView.showsUserLocation
        && (point.x  + offsetX) >= (left + width - 50)
        && (point.y + offsetY) >= (top + height - 50)) {

            BOOL retValue = [self.mapCtrl didTapMyLocationButtonForMapView:self.mapCtrl.mapView];
            if (retValue) {
                return nil;
            }
        }*/
        return hitView;
    }

    return [super hitTest:point withEvent:event];
}

- (void)drawRect:(CGRect)rect
{
    if (!self.debuggable) {
        return;
    }
    float offsetX = self.webView.scrollView.contentOffset.x;// + self.mapCtrl.view.frame.origin.x;
    float offsetY = self.webView.scrollView.contentOffset.y;// + self.mapCtrl.view.frame.origin.y;

    float left = self.mapFrame.origin.x - offsetX;
    float top = self.mapFrame.origin.y - offsetY;
    float width = self.mapFrame.size.width;
    float height = self.mapFrame.size.height;

    //-----------------------
    // Draw the HTML region
    //-----------------------
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetRGBFillColor(context, 0, 1.0, 0, 0.4);

    CGRect rectangle = CGRectMake(0, 0, rect.size.width, top);
    CGContextFillRect(context, rectangle);

    rectangle.origin.x = 0;
    rectangle.origin.y = top;
    rectangle.size.width = left;
    rectangle.size.height = height;
    CGContextFillRect(context, rectangle);

    rectangle.origin.x = left + width;
    rectangle.origin.y = top;
    rectangle.size.width = self.webView.scrollView.contentSize.width;
    rectangle.size.height = height;
    CGContextFillRect(context, rectangle);

    rectangle.origin.x = 0;
    rectangle.origin.y = top + height;
    rectangle.size.width = self.webView.scrollView.contentSize.width;
    rectangle.size.height = self.webView.scrollView.contentSize.height;
    CGContextFillRect(context, rectangle);
}
@end