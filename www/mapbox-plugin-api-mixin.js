var cordova = require("cordova"),
    exec = require("cordova/exec"),
    Mixin = require('./mixin');

module.exports = function (type, target) {
    var _mapboxType = type;

    return Mixin({
        _registerCallback: function (name, success, fail) {
            var callbackId = ["Mapbox", type, name, cordova.callbackId++].join('.');

            success = success ||  function () { console.log(callbackId + "() success!", arguments); };
            fail = fail ||  function () { console.log(callbackId + "() fail :(", arguments); };

            cordova.callbacks[callbackId] = {success: success, fail: fail};
            return callbackId;
        },

        _error: function (err) {
            var error = new Error("MapboxPlugin error (" + _mapboxType + ":" + this._id + "): " + err);
            console.warn("throwing MapboxPluginError: ", error);
            throw error;
        },

        _execAfterLoad: function () {
            var args = Array.prototype.slice.call(arguments),
                once = this.once.bind(this),
                onLoad = function () {
                    return this._exec.apply(this, args);
                }.bind(this);

            return new Promise(function (resolve, reject) {
                once('load', function (obj) {
                    onLoad().then(resolve, reject);
                });
            });
        },

        _exec: function (callback, method, args) {
            args = [this._id].concat(args || []);
            callback = callback || function (err, response) {};
            return new Promise(function (resolve, reject) {
                exec(
                    function onSuccess(response) {
                        callback(null, response);
                        resolve(response);
                    },
                    function onError(error) {
                        callback(error);
                        reject(error);
                    },
                    "Mapbox", method, args
                );
            });
        }
    })(target);
};
