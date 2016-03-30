var cabo = {
    name: "Cabo San Lucas",
    style: "emerald",
    minZoom: 0,
    maxZoom: 16,
    bounds: {
        north: 22.891,
        east: -109.919,
        south: 22.879,
        west: -109.905
    }
};

Mapbox.listOfflineRegions(function (err, regions) {
    if (err) return onError(err);
    console.log('listOfflineRegions()', regions);

    var region = regions[cabo.name];

    // First load will download region.
    if (!region) {
        region = Mapbox.createOfflineRegion(cabo);

        region.on("error", function (e) {
            console.error("OfflineRegion onError", e);
        });

        region.on("progress", function (progress) {
            console.log("OfflineRegion download onProgress", progress);
        });

        region.on("complete", function (progress) {
            console.log("OfflineRegion download onComplete", progress);
        });

        region.download();
    }
    // Subsequent loads will display offline region download status.
    else {
        region.getStatus(function (err, status) {
            if (err) return onError(err);
            console.log("OfflineRegion getStatus()", status);
        });
    }
});

var map = new Mapbox.Map({
        style: 'emerald',
        zoom: 15,
        center: [-109.912, 22.885],
        showUserLocation: true,
        margins: {
            left: 0,
            right: 0,
            top: 0,
            bottom: 0
        },
        markers: [
            {"title": "Marker 1", "lng": -109.912, "lat": 22.885}
        ],
        // NOTE: the options below are broken...
        hideAttribution: true, // default false
        hideLogo: true, // default false
        hideCompass: false, // default false
        disableRotation: false, // default false
        disableScroll: false, // default false
        disableZoom: false, // default false
        disablePitch: false // default false
    });

map.on('load', function (e) {
    map.addMarkers(
        [
            {"title": "Marker 2", "lng": -109.910, "lat": 22.886},
            {"title": "Marker 3", "lng": -109.913, "lat": 22.883}
        ],
        function () { console.log("Markers added!"); },
        function (e) { console.error("Error adding markers:", e); }
    );

    map.addMarkerCallback(printMarker);

    function printMarker(selectedMarker) {
        alert("Marker selected: " + JSON.stringify(selectedMarker));
        map.addMarkerCallback(printMarker);
    }
});
