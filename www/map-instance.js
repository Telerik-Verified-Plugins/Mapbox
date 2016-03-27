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

MapInstance.prototype.jumpTo = function (options, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "jumpTo",
        [options]
    );
};

MapInstance.prototype.setCenter = function (options, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "setCenter",
        [options]
    );
};

MapInstance.prototype.getCenter = function (callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "getCenter"
    );
};

MapInstance.prototype.addMarkers = function (options, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "addMarkers",
        [options]
    );
};

MapInstance.prototype.addMarkerCallback = function (callback) {
    this._execAfterLoad(callback, null, "addMarkerCallback");
};

MapInstance.prototype.setCenter = function (center, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "setCenter",
        [center]
    );
};

MapInstance.prototype.getCenter = function (callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "getCenter"
    );
};

MapInstance.prototype.getZoomLevel = function (callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "getZoomLevel"
    );
};

MapInstance.prototype.setZoomLevel = function (zoom, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "setZoomLevel",
        [zoom]
    );
};

MapInstance.prototype.showUserLocation = function (enabled, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "showUserLocation",
        [enabled]
    );
};

MapInstance.prototype.addSource = function (name, source, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "addSource",
        [name, source]
    );
};

MapInstance.prototype.addLayer = function (layer, callback) {
    var result = wrapCallback(callback);
    this._execAfterLoad(
        result.success,
        result.error,
        "addLayer",
        [layer]
    );
};

function wrapCallback(callback) {
    return {
        success: function (response) { callback(null, response); },
        error: function (err) { callback(err); }
    };
}

module.exports = MapInstance;
