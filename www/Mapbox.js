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

  removeAllMarkers: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "removeAllMarkers", []);
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

  getBounds: function (successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "getBounds", []);
  },

  setBounds: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "setBounds", [options]);
  },

  addPolygon: function (options, successCallback, errorCallback) {
    cordova.exec(successCallback, errorCallback, "Mapbox", "addPolygon", [options]);
  },

  convertCoordinate: function(options, successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertCoordinate", [options]);
  },

  convertPoint: function(options, successCallback, errorCallback){
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertPoint", [options]);
  },

  onRegionWillChange: function(callback){
    cordova.exec(callback, null, "Mapbox", "onRegionWillChange", []);
  },

  onRegionIsChanging: function(callback){
    cordova.exec(callback, null, "Mapbox", "onRegionIsChanging", []);
  },

  onRegionDidChange: function(callback){
    cordova.exec(callback, null, "Mapbox", "onRegionDidChange", []);
  }
};
