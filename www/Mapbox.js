var exec = require("cordova/exec");
  
module.exports = {
  show: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "show", [id, options]);
  },

  hide: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "hide", [id, options]);
  },

  addMarkers: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "addMarkers", [id, options]);
  },    

  addMarkerCallback: function (callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "addMarkerCallback", [id]);
  },

  animateCamera: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "animateCamera", [id, options]);
  },

  addGeoJSON: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "addGeoJSON", [id, options]);
  },

  setCenter: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setCenter", [id, options]);
  },

  getCenter: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getCenter", [id]);
  },

  setTilt: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setTilt", [id, options]);
  },

  getTilt: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getTilt", [id]);
  },

  getZoomLevel: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getZoomLevel", [id]);
  },

  setZoomLevel: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setZoomLevel", [id, options]);
  },

  addPolygon: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "addPolygon", [id, options]);
  },

  convertCoordinate: function(options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertCoordinate", [id, options]);
  },

  convertPoint: function(options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertPoint", [id, options]);
  },

  onRegionWillChange: function(callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "onRegionWillChange", [id]);
  },

  onRegionIsChanging: function(callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "onRegionIsChanging", [id]);
  },

  onRegionDidChange: function(callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "onRegionDidChange", [id]);
  }

};