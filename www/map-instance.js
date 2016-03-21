var exec = require("cordova/exec"),
    channel = require("cordova/channel"),
    channelIds = 0;

var Events = Mixin({
        initEvents: function (prefix) {
            if (!this._channelPrefix) {
                this._channelPrefix = prefix + "." + (channelIds++);
            }
        },

        _prefix: function (type) {
            return this._channelPrefix + "." + type;
        },

        _channel: function (type, sticky) {
            var t = this._prefix(type);
            if (!this._channels) {
                this._channels = {};
            }
            if (sticky !== undefined) {
                this._channels[t] = sticky ?
                    channel.createSticky(t) :
                    channel.create(t);
            }
            return this._channels[t];
        },

        createChannel: function (type) {
            this._channel(type, false);
        },

        createStickyChannel: function (type) {
            this._channel(type, true);
        },

        once: function (type, listener) {
            var onEvent = function (e) {
                    listener(e);
                    this.off(type, onEvent);
                };
            this.on(type, onEvent.bind(this));
        },

        on: function (type, listener) {
            this._channel(type).subscribe(listener);
        },

        off: function (type, listener) {
            this._channel(type).unsubscribe(listener);
        },

        fire: function (type, e) {
            this._channel(type).fire(e);
        }
    });

function assign(target) {
    if (target === undefined || target === null) {
        throw new TypeError('Cannot convert undefined or null to object');
    }

    var output = Object(target);
    for (var index = 1; index < arguments.length; index++) {
        var source = arguments[index];
        if (source !== undefined && source !== null) {
            for (var nextKey in source) {
                if (source.hasOwnProperty(nextKey)) {
                    output[nextKey] = source[nextKey];
                }
            }
        }
    }
    return output;
}

function Mixin(behaviour) {
    return function(target) {
        return assign(target, behaviour);
    };
}

function MapInstance(options) {
    var onLoad = _onLoad.bind(this),
        onError = this._error.bind(this);

    this._error = onError;

    this.initEvents("Mapbox.MapInstance");
    this.createStickyChannel("load");

    exec(onLoad, this._error, "Mapbox", "create", [options]);

    function _onLoad(resp) {
        this._id = resp.id;
        this.loaded = true;

        this.fire("load", {map: this});
    }
}

Events(MapInstance.prototype);

MapInstance.prototype._error = function (err) {
    var error = new Error("Map error (ID: " + this._id + "): " + err);
    console.warn("throwing MapError: ", error);
    throw error;
};

MapInstance.prototype._exec = function (successCallback, errorCallback, method, args) {
    args = [this._id].concat(args || []);
    exec(successCallback, errorCallback, "Mapbox", method, args);
};

MapInstance.prototype._execAfterLoad = function () {
    var args = arguments;
    this.once('load', function (map) {
        this._exec.apply(this, args);
    }.bind(this));
};

MapInstance.prototype.setCenter = function (options, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "setCenter", [options]);
};

MapInstance.prototype.getCenter = function (successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "getCenter");
};

MapInstance.prototype.addMarkers = function (options, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "addMarkers", [options]);
};

MapInstance.prototype.addMarkerCallback = function (callback) {
    this._execAfterLoad(callback, null, "addMarkerCallback");
};

MapInstance.prototype.setCenter = function (center, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "setCenter", [center]);
};

MapInstance.prototype.getCenter = function (successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "getCenter");
};

MapInstance.prototype.getZoomLevel = function (successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "getZoomLevel");
};

MapInstance.prototype.setZoomLevel = function (zoom, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "setZoomLevel", [zoom]);
};

MapInstance.prototype.showUserLocation = function (enabled, successCallback, errorCallback) {
    this._execAfterLoad(successCallback, errorCallback, "showUserLocation", [enabled]);
};

module.exports = MapInstance;
