function Mapbox() {
}

Mapbox.prototype.test = function (options, successCallback, errorCallback) {
  cordova.exec(successCallback, errorCallback, "Mapbox", "test", [options]);
};

Mapbox.install = function () {
  if (!window.plugins) {
    window.plugins = {};
  }

  window.plugins.mapbox = new Mapbox();
  return window.plugins.mapbox;
};

cordova.addConstructor(Mapbox.install);