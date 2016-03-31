var codes = {
        CAMERACHANGE: 0,
        FLING: 1,
        INFOWINDOWCLICK: 2,
        INFOWINDOWCLOSE: 3,
        INFOWINDOWLONGCLICK: 4,
        MAPCLICK: 5,
        MAPLONGCLICK: 6,
        MARKERCLICK: 7,
        BEARINGTRACKINGMODECHANGE: 8,
        LOCATIONCHANGE: 9,
        LOCATIONTRACKINGMODECHANGE: 10,
        ONSCROLL: 11,
    },
    events = {};

events[codes.CAMERACHANGE] = {name: "camerachange"};
events[codes.FLING] = {name: "fling"};
events[codes.INFOWINDOWCLICK] = {name: "infowindowclick"};
events[codes.INFOWINDOWCLOSE] = {name: "infowindowclose"};
events[codes.INFOWINDOWLONGCLICK] = {name: "infowindowlongclick"};
events[codes.MAPCLICK] = {name: "mapclick"};
events[codes.MAPLONGCLICK] = {name: "maplongclick"};
events[codes.MARKERCLICK] = {name: "markerclick"};
events[codes.BEARINGTRACKINGMODECHANGE] = {name: "bearingtrackingmodechange"};
events[codes.LOCATIONCHANGE] = {name: "locationchange"};
events[codes.LOCATIONTRACKINGMODECHANGE] = {name: "locationtrackingmodechange"};
events[codes.ONSCROLL] = {name: "onscroll"};

module.exports = {
    events: events,
    codes: codes
};
