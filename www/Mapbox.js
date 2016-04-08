var exec = require("cordova/exec");

module.exports = {
  show: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "show", [options]);
  },

  hide: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "hide", [options]);
  },

  addMarkers: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "addMarkers", [options]);
  },

  addMarkerCallback: function (callback) {
    var options = {ids : [0]};
    cordova.exec(callback, null, "Mapbox", "addMarkerCallback", [options]);
  },

  animateCamera: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "animateCamera", [options]);
  },

  addGeoJSON: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "addGeoJSON", [options]);
  },

  setCenter: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "setCenter", [options]);
  },

  getCenter: function (successCallback, errorCallback) {
    var options = {ids : [0]};
    cordova.exec(successCallback, errorCallback, "Mapbox", "getCenter", [options]);
  },

  setTilt: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "setTilt", [options]);
  },

  getTilt: function (successCallback, errorCallback) {
    var options = {ids : [0]};
    cordova.exec(successCallback, errorCallback, "Mapbox", "getTilt", [options]);
  },

  getZoomLevel: function (successCallback, errorCallback) {
    var options = {ids : [0]};
    cordova.exec(successCallback, errorCallback, "Mapbox", "getZoomLevel", [options]);
  },

  setZoomLevel: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "setZoomLevel", [options]);
  },

  addPolygon: function (options, successCallback, errorCallback) {
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "addPolygon", [options]);
  },

  convertCoordinate: function(options, successCallback, errorCallback){
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertCoordinate", [options]);
  },

  convertPoint: function(options, successCallback, errorCallback){
    options.ids = [0];
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertPoint", [options]);
  },

  onRegionWillChange: function(callback){
    var options = {ids : [0]};
    cordova.exec(callback, null, "Mapbox", "onRegionWillChange", [options]);
  },

  onRegionIsChanging: function(callback){
    var options = {ids : [0]};
    cordova.exec(callback, null, "Mapbox", "onRegionIsChanging", [options]);
  },

  onRegionDidChange: function(callback){
    var options = {ids : [0]};
    cordova.exec(callback, null, "Mapbox", "onRegionDidChange", [options]);
  }

};