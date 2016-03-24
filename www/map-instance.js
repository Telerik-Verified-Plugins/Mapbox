var exec = require("cordova/exec"),
    EventsMixin = require("./events-mixin");

function MapInstance(options) {
    var onLoad = _onLoad.bind(this),
        onError = this._error.bind(this);

    this._error = onError;

    this.initEvents("Mapbox.MapInstance");
    this.createStickyChannel("load");

    exec(onLoad, this._error, "Mapbox", "createMap", [options]);

    function _onLoad(resp) {
        this._id = resp.id;
        this.loaded = true;

        this.fire("load", {map: this});
    }
}

EventsMixin(MapInstance.prototype);

MapInstance.prototype._error = function (err) {
    var error = new Error("Map error (ID: " + this._id + "): " + err);
    console.warn("throwing MapError: ", error);
    throw error;
};

MapInstance.prototype._exec = function (successCallback, errorCallback, method, args) {
    args = [this._id].concat(args || []);
    exec(successCallback, errorCallback, "Mapbox", method, args);
};

MapInstance.prototype._execAfterLoad = function () {
    var args = arguments;
    this.once('load', function (map) {
        this._exec.apply(this, args);
    }.bind(this));
};

MapInstance.prototype.jumpTo = function (options, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "jumpTo", [options]);
};

MapInstance.prototype.setCenter = function (options, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "setCenter", [options]);
};

MapInstance.prototype.getCenter = function (successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "getCenter");
};

MapInstance.prototype.addMarkers = function (options, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "addMarkers", [options]);
};

MapInstance.prototype.addMarkerCallback = function (callback) {
    this._execAfterLoad(callback, null, "addMarkerCallback");
};

MapInstance.prototype.setCenter = function (center, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "setCenter", [center]);
};

MapInstance.prototype.getCenter = function (successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "getCenter");
};

MapInstance.prototype.getZoomLevel = function (successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "getZoomLevel");
};

MapInstance.prototype.setZoomLevel = function (zoom, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "setZoomLevel", [zoom]);
};

MapInstance.prototype.showUserLocation = function (enabled, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "showUserLocation", [enabled]);
};

module.exports = MapInstance;
