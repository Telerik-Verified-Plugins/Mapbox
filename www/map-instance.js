var exec = require("cordova/exec"),
    MapboxPluginAPI = require("./mapbox-plugin-api-mixin"),
    EventsMixin = require("./events-mixin"),
    E = require("./map-events");

function MapInstance(options) {
    var onLoad = this._onLoad.bind(this),
        onEvent = _onEvent.bind(this),
        onError = _onError.bind(this);

    this.createStickyChannel("load");
    this._onEventId = this._registerCallback('onEvent', onEvent, onError);

    exec(onLoad, onError, "Mapbox", "createMap", [options, this._onEventId]);

    function _onEvent(e) {
        var event = E.events[e.code];
        switch (e.code) {
            case E.codes.INFOWINDOWCLICK:
            case E.codes.INFOWINDOWCLOSE:
            case E.codes.INFOWINDOWLONGCLICK:
            case E.codes.MARKERCLICK:
                var marker = new Marker(e.data.id);
                console.debug("Event recieved: " + event.name, marker);
                return this.fire(event.name, marker);
            default:
                console.debug("Event recieved: " + event.name, e.data);
                return this.fire(event.name, e.data);
        }
    }

    function _onError(error) {
        try {
            this.prototype._error.call(this, error);
        } catch (e) {
            this.fire("error", e);
        }
    }
}

MapboxPluginAPI('MapInstance', MapInstance.prototype);
EventsMixin('MapInstance', MapInstance.prototype);

MapInstance.prototype.jumpTo = function (options, callback) {
    return this._execAfterLoad(callback, "jumpTo", [options]);
};

MapInstance.prototype.setCenter = function (options, callback) {
    return this._execAfterLoad(callback, "setCenter", [options]);
};

MapInstance.prototype.getCenter = function (callback) {
    return this._execAfterLoad(callback, "getCenter");
};

MapInstance.prototype.addMarkers = function (options, callback) {
    return this._execAfterLoad(callback, "addMarkers", [options]);
};

MapInstance.prototype.addMarkerCallback = function (markerCallback, callback) {
    return this._execAfterLoad(callback, "addMarkerCallback", [markerCallback]);
};

MapInstance.prototype.setCenter = function (center, callback) {
    return this._execAfterLoad(callback, "setCenter", [center]);
};

MapInstance.prototype.getCenter = function (callback) {
    return this._execAfterLoad(callback, "getCenter");
};

MapInstance.prototype.getZoomLevel = function (callback) {
    return this._execAfterLoad(callback, "getZoomLevel");
};

MapInstance.prototype.setZoomLevel = function (zoom, callback) {
    return this._execAfterLoad(callback, "setZoomLevel", [zoom]);
};

MapInstance.prototype.showUserLocation = function (enabled, callback) {
    return this._execAfterLoad(callback, "showUserLocation", [enabled]);
};

MapInstance.prototype.addSource = function (name, source, callback) {
    return this._execAfterLoad(callback, "addSource", [name, source]);
};

MapInstance.prototype.addLayer = function (layer, callback) {
    return this._execAfterLoad(callback, "addLayer", [layer]);
};

MapInstance.prototype._onLoad = function (resp) {
    this._id = resp.id;
    this.loaded = true;

    this.fire("load", {map: this});
};

function Marker(id) {
    this._id = id;
}

MapboxPluginAPI('Marker', Marker.prototype);
EventsMixin('Marker', Marker.prototype);

module.exports = MapInstance;
