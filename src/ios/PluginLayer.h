//
// Created by vikti on 29/03/2016.
//

#import <UIKit/UIKit.h>
@class MapController;

@interface PluginLayer : UIView
@property (nonatomic) MapController *mapCtrl;
@property (nonatomic, strong) UIView* webView;
@property (nonatomic) CGRect mapFrame;
@property (nonatomic) BOOL debuggable;
@property (nonatomic) BOOL clickable;
@property (nonatomic) NSMutableDictionary *HTMLNodes;

- (id)initWithFrame:(CGRect)aRect;
- (void)putHTMLElement:(NSString *)domId size:(NSDictionary *)size;
- (void)removeHTMLElement:(NSString *)domId;
- (void)clearHTMLElement;

@end