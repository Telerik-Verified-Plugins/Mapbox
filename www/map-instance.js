var exec = require("cordova/exec"),
    MapboxPluginAPI = require("./mapbox-plugin-api-mixin"),
    EventsMixin = require("./events-mixin");

function MapInstance(options) {
    var onLoad = this._onLoad.bind(this),
        onError = this._error.bind(this);

    this.createStickyChannel("load");

    exec(onLoad, onError, "Mapbox", "createMap", [options]);
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

module.exports = MapInstance;
