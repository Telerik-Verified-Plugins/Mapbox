var exec = require("cordova/exec"),
    MapInstance = require("./map-instance"),
    OfflineRegion = require("./offline-region"),
    offlineRegions = [];


function listOfflineRegions(successCallback, errorCallback) {
    exec(
        function (regions) {
            console.log("Offline regions: ", regions);
        },
        function (error) {
            console.error("Error getting offline regions: ", error);
        },
        "Mapbox",
        "listOfflineRegions"
    );
}

module.exports = {
    Map: MapInstance,
    OfflineRegion: OfflineRegion
};
