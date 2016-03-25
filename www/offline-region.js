var cordova = require("cordova"),
    exec = require("cordova/exec"),
    EventsMixin = require("./events-mixin");

var OFFLINE_REGIONS = {};

function OfflineRegion() {
    var onProgress = _onProgress.bind(this),
        onComplete = _onComplete.bind(this),
        onError = _onError.bind(this);

    this._onProgressId = this._registerCallback('onProgress', onProgress);
    this._onCompleteId = this._registerCallback('onComplete', onComplete, onError);

    this._error = this._error.bind(this);
    this._create = this._create.bind(this);
    this._instance = this._instance.bind(this);

    this._downloading = false;

    this.initEvents("Mapbox.MapInstance");
    this.createStickyChannel("load");
    this.createChannel("progress");
    this.createStickyChannel("complete");
    this.createStickyChannel("error");

    function _onProgress(progress) {
        this.fire("progress", progress);
    }

    function _onComplete(resp) {
        this._downloading = false;
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

OfflineRegion.prototype._create = function (options) {
    var args = [options, this._onProgressId, this._onCompleteId];
    exec(this._instance, this._error, "Mapbox", "createOfflineRegion", args);
};

OfflineRegion.prototype._instance = function (response) {
    this._id = response.id;
    this._name = response.name;
    this.loaded = true;
    this.fire("load", {offlineRegion: this});
    OFFLINE_REGIONS[this._id] = this;
};

OfflineRegion.prototype._error = function (err) {
    var error = new Error("OfflineRegion error (ID: " + this._id + "): " + err);
    this._downloading = false;
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

OfflineRegion.prototype.getStatus = function (callback) {
    this._execAfterLoad(onSuccess, onError, "offlineRegionStatus");
    function onSuccess(status) {
        callback(null, status);
    }
    function onError(error) {
        callback(error);
    }
};

Object.defineProperty(OfflineRegion.prototype, "downloading", {
    get: function () {
        return this._downloading;
    }
});

Object.defineProperty(OfflineRegion.prototype, "name", {
    get: function () {
        return this._name;
    }
});

module.exports = {
    createOfflineRegion: function (options) {
        var region = new OfflineRegion();
        region._create(options);
        return region;
    },

    listOfflineRegions: function (callback) {
        exec(
            function (responses) {
                console.log("Offline regions: ", responses);
                var regions = responses.map(function (response) {
                        var region = OFFLINE_REGIONS[response.id];
                        if (!region) {
                            region = new OfflineRegion();
                            region._instance(response);
                        }
                        return region;
                    }),
                    byName = regions.reduce(function (regionsByName, region) {
                        regionsByName[region.name] = region;
                        return regionsByName;
                    }, {});
                callback(null, byName);
            },
            function (errorMessage) {
                var error = "Error getting offline regions: " + errorMessage;
                console.error(error);
                callback(error);
            },
            "Mapbox",
            "listOfflineRegions",
            []
        );
    }
};
