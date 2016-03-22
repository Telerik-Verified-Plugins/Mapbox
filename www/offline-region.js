var cordova = require("cordova"),
    exec = require("cordova/exec"),
    EventsMixin = require("./events-mixin");

function OfflineRegion(options) {
    var onLoad = _onLoad.bind(this),
        onProgress = _onProgress.bind(this),
        onError = this._error.bind(this),
        onProgressId = this._registerCallback('onProgress', onProgress);

    this._error = onError;
    this._downloaded = false;
    this._downloading = false;

    this.initEvents("Mapbox.MapInstance");
    this.createStickyChannel("load");
    this.createStickyChannel("complete");
    this.createStickyChannel("error");
    this.createChannel("progress");

    if (options.progress) {
        var progress = options.progress;
        delete options.progress;
        this.on("progress", progress);
    }

    exec(onLoad, this._error, "Mapbox", "createOfflineRegion", [options, onProgressId]);

    function _onLoad(resp) {
        this._id = resp.id;
        this.loaded = true;

        this.fire("load", {map: this});
    }

    function _onProgress(progress) {
        this.fire("progress", progress);
    }
}

EventsMixin(OfflineRegion.prototype);

OfflineRegion.prototype._error = function (err) {
    var error = new Error("OfflineRegion error (ID: " + this._id + "): " + err);
    console.warn("throwing OfflineRegionError: ", error);
    throw error;
};

OfflineRegion.prototype._exec = function (successCallback, errorCallback, method, args) {
    args = [this._id].concat(args || []);
    exec(successCallback, errorCallback, "Mapbox", method, args);
};

OfflineRegion.prototype._execAfterLoad = function () {
    var args = arguments;
    this.once('load', function (map) {
        this._exec.apply(this, args);
    }.bind(this));
};

OfflineRegion.prototype._registerCallback = function (name, success, fail) {
    var callbackId = "MapboxOfflineRegion" + name + cordova.callbackId++;

    console.log("_registerCallback(): " + callbackId);

    success = success ||  function () { console.log(callbackId + "() success!", arguments); };
    fail = fail ||  function () { console.log(callbackId + "() fail :(", arguments); };

    cordova.callbacks[callbackId] = {success: success, fail: fail};
    return callbackId;
};

OfflineRegion.prototype.download = function () {
    this._downloading = true;

    this._execAfterLoad(onSuccess, onError, "downloadOfflineRegion");

    function onSuccess(resp) {
        this._downloading = false;
        this._downloaded = true;
        this.fire("complete", resp);
    }

    function onError(error) {
        this._downloading = false;
        this._downloaded = false;
        try {
            this._error(error);
        } catch (e) {
            this.fire("error", e);
        }
    }
};

OfflineRegion.prototype.pause = function () {
    this._execAfterLoad(successCallback, errorCallback, "pauseOfflineRegion");
};

module.exports = OfflineRegion;
