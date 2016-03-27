var exec = require("cordova/exec"),
    MapboxPluginAPI = require("./mapbox-plugin-api-mixin"),
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

MapboxPluginAPI('OfflineRegion', OfflineRegion.prototype);
EventsMixin('OfflineRegion', OfflineRegion.prototype);

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

OfflineRegion.prototype.download = function (callback) {
    this._downloading = true;
    return this._execAfterLoad(onSuccess, "downloadOfflineRegion");
    function onSuccess(err) {
        if (err) return (callback || this._err)(err);
        console.log("Mapbox OfflineRegion download started.");
    }
};

OfflineRegion.prototype.pause = function (callback) {
    this._downloading = false;
    return this._execAfterLoad(onSuccess, "pauseOfflineRegion");
    function onSuccess() {
        if (err) return (callback || this._err)(err);
        console.log("Mapbox OfflineRegion download paused.");
    }
};

OfflineRegion.prototype.getStatus = function (callback) {
    return this._execAfterLoad(callback, "offlineRegionStatus");
};

module.exports = {
    createOfflineRegion: function (options) {
        var region = new OfflineRegion();
        region._create(options);
        return region;
    },

    listOfflineRegions: function (callback) {
        callback = callback || function (err, response) {};
        return new Promise(function (resolve, reject) {
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
                    resolve(byName);
                },
                function (errorMessage) {
                    var error = "Error getting offline regions: " + errorMessage;
                    console.error(error);
                    callback(error);
                    reject(error);
                },
                "Mapbox",
                "listOfflineRegions",
                []
            );
        });
    }
};
