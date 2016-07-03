//
// Created by vikti on 01/04/2016.
//

#import "MapsManager.h"

@implementation MapsManager

static int mapId;
static NSMutableDictionary *maps;
static NSString *accessToken;
static CDVMapbox *cdvMapbox;
static CDVPlugin *cdvPlugin;

- (id)initWithCDVPlugin:(CDVPlugin*)aPlugin withCDVMapboxPlugin:(CDVMapbox*)mapbox withAccessToken:(NSString *)aToken{
    self = [super init];
    if (self) {
        mapId = 0;
        cdvPlugin = aPlugin;
        cdvMapbox = mapbox;
        accessToken = aToken;
    }
    maps = [[NSMutableDictionary alloc] init];
    return self;
}

- (Map*) createMap:(NSDictionary*)args withId:(int)anId {
    // Get an instance of Map Class
    Map *map = [[Map alloc] initWithArgs:args withCDVMapboxPlugin:cdvMapbox];

    // Store it
    maps[[NSString stringWithFormat:@"%d",mapId]] = map;

    if(anId >= 0){
        map.id = (int*)anId;
    } else {
        // Assign property
        map.id = (int *) mapId;
        //map.options:(NSDictionary *)options

        //todo add a embedrect in the mapOverlayLayer for multi maps purpose
        mapId++;
    }

    return map;
}

- (Map *)getMap:(int)mapId {
    Map *map = maps[[NSString stringWithFormat:@"%d",mapId]];
    return map;
}

- (int *)getCcount {
    return (int *) [maps count];
}

- (void)removeMap:(int)id{
    Map* map = maps[[NSString stringWithFormat:@"%d",id]];
    [map destroy];
    [maps removeObjectForKey:[NSString stringWithFormat:@"%d",id]];
}

@end