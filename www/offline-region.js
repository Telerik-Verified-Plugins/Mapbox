var exec = require("cordova/exec"),
    MapboxPluginAPI = require("./mapbox-plugin-api-mixin"),
    EventsMixin = require("./events-mixin");

var OFFLINE_REGIONS = {};

function OfflineRegion(options) {
    this._init();
    this._properties = options.properties || {};

    var args = [options, this._onProgressId, this._onCompleteId];
    exec(this._onCreate, this._error, "Mapbox", "createOfflineRegion", args);
}

MapboxPluginAPI('OfflineRegion', OfflineRegion.prototype);
EventsMixin('OfflineRegion', OfflineRegion.prototype);

Object.defineProperty(OfflineRegion.prototype, "downloading", {
    get: function () {
        return this._downloading;
    }
});

Object.defineProperty(OfflineRegion.prototype, "loaded", {
    get: function () {
        return this._loaded;
    }
});

Object.defineProperty(OfflineRegion.prototype, "properties", {
    get: function () {
        return this._properties;
    }
});

OfflineRegion.prototype._init = function () {
    var onProgress = _onProgress.bind(this),
        onComplete = _onComplete.bind(this),
        onError = _onError.bind(this);

    this._onProgressId = this._registerCallback('onProgress', onProgress);
    this._onCompleteId = this._registerCallback('onComplete', onComplete, onError);
    this._error = onError;
    this._onCreate = this._onCreate.bind(this);

    this._downloading = false;

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
            this.prototype._error.call(this, error);
        } catch (e) {
            this.fire("error", e);
        }
    }
};

OfflineRegion.prototype._onCreate = function onCreate(response) {
    this._id = response.id;
    this._properties = response.properties;
    this._loaded = true;
    this.fire("load", this);

    OFFLINE_REGIONS[this._id] = this;
};

OfflineRegion.prototype.download = function (callback) {
    this._downloading = true;
    return this._execAfterLoad(onSuccess, "downloadOfflineRegion");
    function onSuccess(err) {
        if (err) return (callback || this._error)(err);
        console.debug("Mapbox OfflineRegion download started.");
    }
};

OfflineRegion.prototype.pause = function (callback) {
    this._downloading = false;
    return this._execAfterLoad(onSuccess, "pauseOfflineRegion");
    function onSuccess(err) {
        if (err) return (callback || this._error)(err);
        console.debug("Mapbox OfflineRegion download paused.");
    }
};

OfflineRegion.prototype.getStatus = function (callback) {
    return this._execAfterLoad(callback, "offlineRegionStatus");
};

function listOfflineRegions() {
    return new Promise(function (resolve, reject) {
        exec(resolve, reject, "Mapbox", "listOfflineRegions", []);
    });
}

function createRegionFromResponse(response) {
    return new Promise(function (resolve, reject) {
        var region = OFFLINE_REGIONS[response.id] || new OfflineRegion(response);
        region.once('load', resolve);
    });
}

module.exports = {
    createOfflineRegion: function (options) {
        return new OfflineRegion(options);
    },

    listOfflineRegions: function (callback) {
        callback = callback || function (err, response) {};

        return listOfflineRegions()
            .then(function (responses) {
                return Promise.all(responses.map(createRegionFromResponse));
            })
            .then(function (regions) {
                callback(null, regions);
                return regions;
            })
            .catch(function (errorMessage) {
                var error = "Error getting offline regions: " + errorMessage;
                console.error(error);
                callback(error);
            });
    }
};
