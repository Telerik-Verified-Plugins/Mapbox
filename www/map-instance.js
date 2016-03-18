var exec = require("cordova/exec");

function MapInstance(id) {
    this._id = id;
}

MapInstance.prototype._exec = function (successCallback, errorCallback, method, args) {
    args = [this._id].concat(args || []);
    exec(successCallback, errorCallback, "Mapbox", method, args);
};

MapInstance.prototype.setCenter = function (options, successCallback, errorCallback) {
    this._exec(successCallback, errorCallback, "setCenter", [options]);
};

MapInstance.prototype.getCenter = function (successCallback, errorCallback) {
    this._exec(successCallback, errorCallback, "getCenter");
};

MapInstance.prototype.addMarkers = function (options, successCallback, errorCallback) {
    this._exec(successCallback, errorCallback, "addMarkers", [options]);
};

MapInstance.prototype.addMarkerCallback = function (callback) {
    this._exec(callback, null, "addMarkerCallback");
};

MapInstance.prototype.setCenter = function (center, successCallback, errorCallback) {
    this._exec(successCallback, errorCallback, "setCenter", [center]);
};

MapInstance.prototype.getCenter = function (successCallback, errorCallback) {
    this._exec(successCallback, errorCallback, "getCenter");
};

MapInstance.prototype.getZoomLevel = function (successCallback, errorCallback) {
    this._exec(successCallback, errorCallback, "getZoomLevel");
};

MapInstance.prototype.setZoomLevel = function (zoom, successCallback, errorCallback) {
    this._exec(successCallback, errorCallback, "setZoomLevel", [zoom]);
};

module.exports = MapInstance;
