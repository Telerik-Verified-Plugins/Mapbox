var exec = require("cordova/exec");


function getAbsoluteMargins(mapDiv) {

    var pageRect = getPageRect();

    var rect = mapDiv.getBoundingClientRect();

    return {
        'top': pageRect.top + rect.top,
        'right': (pageRect.left + pageRect.width) - rect.right,
        'bottom': (pageRect.top + pageRect.height) - rect.bottom,
        'left': pageRect.left + rect.left
    };
}


function getDomElementsOverlay(mapDiv) {
    var children = getAllChildren(mapDiv);

    var elements = [];
    var element;

    for (var i = 0; i < children.length; i++) {
        element = getDomElementOverlay(children[i]);

        elements.push(element);
    }
    return elements;
}

function getDomElementOverlay(elem) {
    var elemId = elem.getAttribute("data-pluginDomId");
    if (!elemId) {
        elemId = setRandomId();
        elem.setAttribute("data-pluginDomId", elemId);
    }
    return {
        id: elemId,
        size: getDivRect(elem)
    }
}

function setRandomId() {
    return "pmb" + Math.floor(Math.random() * Date.now());
}

function getAllChildren(root) {
    var list = [];
    var clickable;
    var style, displayCSS, opacityCSS, visibilityCSS;
    var search = function (node) {
        while (node !== null) {
            if (node.nodeType === 1) {
                style = window.getComputedStyle(node);
                visibilityCSS = style.getPropertyValue('visibility');
                displayCSS = style.getPropertyValue('display');
                opacityCSS = style.getPropertyValue('opacity');
                if (displayCSS !== "none" && opacityCSS > 0 && visibilityCSS != "hidden") {
                    clickable = node.getAttribute("data-clickable");
                    if (clickable &&
                        clickable.toLowerCase() === "false" &&
                        node.hasChildNodes()) {
                        Array.prototype.push.apply(list, getAllChildren(node));
                    } else {
                        list.push(node);
                    }
                }
            }
            node = node.nextSibling;
        }
    };
    search(root.firstChild);
    return list;
}


function getDivRect(div) {
    if (!div) {
        return;
    }

    var pageRect = getPageRect();

    var rect = div.getBoundingClientRect();
    return {
        'left': rect.left + pageRect.left,
        'top': rect.top + pageRect.top,
        'width': rect.width,
        'height': rect.height
    };
}

function getPageRect() {
    var doc = document.documentElement;

    var pageWidth = window.innerWidth ||
            document.documentElement.clientWidth ||
            document.body.clientWidth,
        pageHeight = window.innerHeight ||
            document.documentElement.clientHeight ||
            document.body.clientHeight;
    var pageLeft = (window.pageXOffset || doc.scrollLeft) - (doc.clientLeft || 0);
    var pageTop = (window.pageYOffset || doc.scrollTop) - (doc.clientTop || 0);

    return {
        'width': pageWidth,
        'height': pageHeight,
        'left': pageLeft,
        'top': pageTop
    };
}

module.exports = {
     //todo
     /*
     addToggleTrackingCallback: function (callback, errorCallback, id) {
             id = id || 0;
             cordova.exec(callback, errorCallback, "Mapbox", "toggleTracking", [id])
         },

*/

    show: function (options, successCallback, errorCallback, id) {
        id = id || 0;
        if (options.domElement) {
            options.HTMLs = getDomElementsOverlay(options.domElement);
            options.rect = getDivRect(options.domElement);
            delete options.domElement; //Prevent circular reference error
        }
        cordova.exec(successCallback, errorCallback, "Mapbox", "SHOW", [id, options]);
    },

    setDebug: function (debug, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "SET_DEBUG", [id, debug])
    },

    setClickable: function (clickable, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "SET_CLICKABLE", [id, clickable])
    },

    hide: function (id, successCallback, errorCallback) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "HIDE", [id]);
    },

    destroy: function (id, successCallback, errorCallback) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "DESTROY", [id]);
    },

    setContainer: function (container, successCallback, errorCallback, id) {
        id = id || 0;
        container.HTMLs = getDomElementsOverlay(container.domElement);
        container.rect = getDivRect(container.domElement);
        delete container.domElement; //Prevent circular reference error
        cordova.exec(successCallback, errorCallback, "Mapbox", "SET_CONTAINER", [id, container])
    },

    downloadCurrentMap: function (id, statusCallback, errorCallback) {
        id = id || 0;
        cordova.exec(statusCallback, errorCallback, "Mapbox", "DOWNLOAD_CURRENT_MAP", [id]);
    },

    getOfflineRegionsList: function (id, successCallback, errorCallback) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "GET_OFFLINE_REGIONS_LIST", [id]);
    },

    deleteOfflineRegion: function (id, zoneId, successCallback, errorCallback) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "DELETE_OFFLINE_REGION", [id, zoneId]);
    },

    pauseDownload: function (id, successCallback, errorCallback) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "PAUSE_DOWNLOAD", [id]);
    },

    addMarkerCallback: function (id, callback, errorCallback) {
        id = id || 0;
        cordova.exec(callback, errorCallback, "Mapbox", "ADD_MARKER_CALLBACK", [id]);
    },

    //only handle marker for now
    addSource: function (sourceId, source, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "ADD_MARKER", [id, sourceId, source]);
    },

    setMarkerLngLat: function (sourceId, coordinates, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "MARKER__SET_LNG_LAT", [id, sourceId, coordinates])
    },

    setMarkerIcon: function (sourceId, imageProperties, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "MARKER__SET_ICON", [id, sourceId, imageProperties])
    },

    removeSource: function (sourceId, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "REMOVE_MARKER", [id, sourceId])
    },

    flyTo: function (options, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "FLY_TO", [id, options]);
    },

    setCenter: function (center, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "SET_CENTER", [id, center]);
    },

    getCenter: function (successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "GET_CENTER", [id]);
    },

    getNextPositions: function (delta, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "NEXT_MARKERS_POSITIONS_PREDICATE", [id, delta]);
    },

    getMarkersPositions: function (successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "GET_MARKERS_POSITIONS", [id]);
    },

    scrollMap: function (delta, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "SCROLL_MAP", [id, delta]);
    },

    setPitch: function (pitch, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "SET_PITCH", [id, pitch]);
    },

    getPitch: function (successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "GET_PITCH", [id]);
    },

    getZoom: function (successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "GET_ZOOM", [id]);
    },

    setZoom: function (zoom, options, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "SET_ZOOM", [id, zoom, options]);
    },

    zoomTo: function (zoom, options, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "ZOOM_TO", [id, zoom, options]);
    },

    getBounds: function (successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "GET_BOUNDS", [id]);
    },

    getCameraPosition: function (successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "GET_CAMERA_POSITION", [id]);
    },

    convertCoordinates: function (coords, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "CONVERT_COORDINATES", [id, coords]);
    },

    convertPoint: function (point, successCallback, errorCallback, id) {
        id = id || 0;
        cordova.exec(successCallback, errorCallback, "Mapbox", "CONVERT_POINT", [id, point]);
    },

    addOnMapChangeListener: function (listener, callback, id) {
        id = id || 0;
        cordova.exec(callback, null, "Mapbox", "ADD_ON_MAP_CHANGE_LISTENER", [id, listener]);
    },
};