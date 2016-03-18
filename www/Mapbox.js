var exec = require("cordova/exec"),
    MapInstance = require("./MapInstance");

module.exports = {
    create: function (options, successCallback, errorCallback) {
        console.log('Mapbox.js create()');
        cordova.exec(function(resp) {
            console.log('Mapbox.js create()', resp);
            var map = new MapInstance(resp.id);
            successCallback(map);
        }, errorCallback, "Mapbox", "create", [options]);
    }

  // hide: function (options, successCallback, errorCallback) {
  //   cordova.exec(successCallback, errorCallback, "Mapbox", "hide", [options]);
  // },

  // animateCamera: function (options, successCallback, errorCallback) {
  //   cordova.exec(successCallback, errorCallback, "Mapbox", "animateCamera", [options]);
  // },

  // addGeoJSON: function (options, successCallback, errorCallback) {
  //   cordova.exec(successCallback, errorCallback, "Mapbox", "addGeoJSON", [options]);
  // },

  // setTilt: function (options, successCallback, errorCallback) {
  //   cordova.exec(successCallback, errorCallback, "Mapbox", "setTilt", [options]);
  // },

  // getTilt: function (successCallback, errorCallback) {
  //   cordova.exec(successCallback, errorCallback, "Mapbox", "getTilt", []);
  // },

<<<<<<< 881cb4ce1ff2ab3dcdbb4afb9e31b6c4dbeb58be
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
