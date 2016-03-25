var exec = require("cordova/exec"),
    MapInstance = require("./map-instance"),
    offline = require("./offline-region");

module.exports = {
    Map: MapInstance,
    createOfflineRegion: offline.createOfflineRegion,
    listOfflineRegions: offline.listOfflineRegions
};
