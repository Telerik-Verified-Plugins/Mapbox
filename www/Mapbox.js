function Mapbox() {
}

Mapbox.prototype.show = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "Mapbox", "show", [options]);
};

Mapbox.prototype.hide = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "Mapbox", "hide", [options]);
};

Mapbox.prototype.addAnnotations = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "Mapbox", "addAnnotations", [options]);
};

Mapbox.prototype.addGeoJson = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "Mapbox", "addGeoJson", [options]);
};

Mapbox.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.mapbox = new Mapbox();
  return window.plugins.mapbox;
};

cordova.addConstructor(Mapbox.install);