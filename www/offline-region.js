var cordova = require("cordova"),
    exec = require("cordova/exec"),
    EventsMixin = require("./events-mixin");

function OfflineRegion(options) {
    var onLoad = _onLoad.bind(this),
        onProgress = _onProgress.bind(this),
        onComplete = _onComplete.bind(this),
        onError = _onError.bind(this),
        onProgressId = this._registerCallback('onProgress', onProgress),
        onCompleteId = this._registerCallback('onComplete', onComplete, onError);

    this._error = this._error.bind(this);
    this._downloaded = false;
    this._downloading = false;

    this.initEvents("Mapbox.MapInstance");
    this.createStickyChannel("load");
    this.createChannel("progress");
    this.createStickyChannel("complete");
    this.createStickyChannel("error");

    exec(onLoad, this._error, "Mapbox", "createOfflineRegion", [options, onProgressId, onCompleteId]);

    function _onLoad(resp) {
        this._id = resp.id;
        this.loaded = true;

        this.fire("load", {map: this});
    }

    function _onProgress(progress) {
        this.fire("progress", progress);
    }

    function _onComplete(resp) {
        this._downloading = false;
        this._downloaded = true;
        this.fire("complete", resp);
    }

    function _onError(error) {
        try {
            this._error(error);
        } catch (e) {
            this.fire("error", e);
        }
    }
}

EventsMixin(OfflineRegion.prototype);

OfflineRegion.prototype._error = function (err) {
    var error = new Error("OfflineRegion error (ID: " + this._id + "): " + err);
    this._downloading = false;
    this._downloaded = false;
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

    success = success ||  function () { console.log(callbackId + "() success!", arguments); };
    fail = fail ||  function () { console.log(callbackId + "() fail :(", arguments); };

    cordova.callbacks[callbackId] = {success: success, fail: fail};
    return callbackId;
};

OfflineRegion.prototype.download = function () {
    this._downloading = true;
    this._execAfterLoad(onSuccess, this._error, "downloadOfflineRegion");
    function onSuccess() {
        console.log("Mapbox OfflineRegion download started.");
    }
};

OfflineRegion.prototype.pause = function () {
    this._downloading = false;
    this._execAfterLoad(onSuccess, this._error, "pauseOfflineRegion");
    function onSuccess() {
        console.log("Mapbox OfflineRegion download paused.");
    }
};

Object.defineProperty(OfflineRegion.prototype, "downloading", {
    get: function () {
        return this._downloading;
    }
});

Object.defineProperty(OfflineRegion.prototype, "downloaded", {
    get: function () {
        return this._downloaded;
    }
});

module.exports = OfflineRegion;
