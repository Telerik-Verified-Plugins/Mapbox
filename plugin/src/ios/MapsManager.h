//
// Created by vikti on 02/04/2016.
//

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>
#import "Map.h"


@interface MapsManager: NSObject

- (id)initWithCDVPlugin:(CDVPlugin*)aPlugin withCDVMapboxPlugin:(CDVMapbox*)mapbox withAccessToken:(NSString *)aToken;
- (Map*)createMap:(NSDictionary *)args withId:(int)anId;
- (Map*)getMap:(int)id;
- (int*)getCcount;
- (void)removeMap:(int)id;

@end