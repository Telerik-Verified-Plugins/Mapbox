var exec = require("cordova/exec"),
    MapInstance = require("./MapInstance");

module.exports = {
    show: function (options, successCallback, errorCallback) {
        console.log('Mapbox.js show()');
        cordova.exec(function(resp) {
            console.log('Mapbox.js show()', resp);
            var map = new MapInstance(resp.id);
            successCallback(map);
        }, errorCallback, "Mapbox", "show", [options]);
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

  animateCamera: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "animateCamera", [options]);
  },

  addGeoJSON: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addGeoJSON", [options]);
  },

  setCenter: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "setCenter", [options]);
  },

  getCenter: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "getCenter", []);
  },

  setTilt: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "setTilt", [options]);
  },

  getTilt: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "getTilt", []);
  },

  getZoomLevel: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "getZoomLevel", []);
  },

  setZoomLevel: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "setZoomLevel", [options]);
  },

  addPolygon: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addPolygon", [options]);
  },

  convertCoordinate: function(options, successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertCoordinate", [options]);
  },

  convertPoint: function(options, successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertPoint", [options]);
  }
};
