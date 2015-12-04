var exec = require("cordova/exec");

module.exports = {
  show: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "show", [options]);
  },

  hide: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "hide", [options]);
  },

  addMarkers: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addMarkers", [options]);
  },

  addMarkerCallback: function (callback) {
    cordova.exec(callback, null, "Mapbox", "addMarkerCallback", []);
  },

  addGeoJSON: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addGeoJSON", [options]);
  },

  addSource: function (name, source, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addSource", [name, source]);
  },

  addLayer: function (layer, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addLayer", [layer]);
  },

  setCenter: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "setCenter", [options]);
  },

  getCenter: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "getCenter", []);
  },

  getZoomLevel: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "getZoomLevel", []);
  },

  setZoomLevel: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "setZoomLevel", [options]);
  },

  addPolygon: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addPolygon", [options]);
  }
};
