//
// Created by vikti on 29/03/2016.
//

#import <UIKit/UIKit.h>
@class MapController;

@interface MapOverlayLayer : UIView
@property (nonatomic) MapController *mapCtrl;
@property (nonatomic, strong) UIView* webView;
@property (nonatomic) CGRect mapFrame;
@property (nonatomic) NSMutableDictionary *mapsFrames;
@property (nonatomic) BOOL debuggable;
@property (nonatomic) BOOL clickable;
@property (nonatomic) NSMutableDictionary *HTMLNodes;

- (id)initWithFrame:(CGRect)aRect;
- (void)setHTMLElement:(NSString *)domId size:(NSMutableDictionary *)size;
- (void)deleteHTMLElement:(NSString *)domId;
- (void)clearHTMLElement;

@end