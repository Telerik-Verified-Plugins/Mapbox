var exec = require("cordova/exec");


function getAbsoluteMargins(mapDiv) {

  var pageRect = getPageRect();

  var rect = mapDiv.getBoundingClientRect();

  return {
    'top': pageRect.top + rect.top,
    'right':  (pageRect.left + pageRect.width) - rect.right,
    'bottom': (pageRect.top + pageRect.height) - rect.bottom,
    'left': pageRect.left + rect.left
  };
}


function getDomElementsOverlay(mapDiv){
  var children = getAllChildren(mapDiv);

  var elements = [];
  var element;

  for (var i = 0; i < children.length; i++) {
    element = getDomElementOverlay(children[i]);

    elements.push(element);
  }
  return elements;
}

function getDomElementOverlay(elem){
  var elemId = elem.getAttribute("__pluginDomId");
  if (!elemId) {
    elemId = setRandomId();
    elem.setAttribute("__pluginDomId", elemId);
  }
  return {
    id: elemId,
    size: getDivRect(elem)
  }
}

function setRandomId(){
  return "pmb" + Math.floor(Math.random() * Date.now());
}

function getAllChildren(root) {
  var list = [];
  var clickable;
  var style, displayCSS, opacityCSS, visibilityCSS;
  var search = function(node) {
    while (node != null) {
      if (node.nodeType == 1) {
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
  show: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    if(options.div){
      options.HTMLs = getDomElementsOverlay(options.div);
      options.rect = getDivRect(options.div);
      delete options.div; //Prevent circular reference error
    }
    cordova.exec(successCallback, errorCallback, "Mapbox", "show", [id, options]);
  },

  setClickable: function (clickable, successCallback, errorCallback, id){
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setClickable", [id, clickable])
  },

  hide: function (id, successCallback, errorCallback) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "hide", [id]);
  },

  refreshMap: function (options, successCallback, errorCallback, id){
    id = id || 0;
    options.HTMLs = getDomElementsOverlay(options.div);
    options.rect = getDivRect(options.div);
    cordova.exec(successCallback, errorCallback, "Mapbox", "refreshMap", [id, options])
  },

  addSource: function (sourceId, source, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "addMarkers", [id, sourceId, source]);
  },

  flyTo: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "flyTo", [id, options]);
  },

  setCenter: function (center, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setCenter", [id, center]);
  },

  getCenter: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getCenter", [id]);
  },

  setPitch: function (pitch, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setPitch", [id, pitch]);
  },

  getPitch: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getPitch", [id]);
  },

  getZoom: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getZoom", [id]);
  },

  setZoom: function (zoom, options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setZoom", [id, zoom, options]);
  },

  zoomTo: function (zoom, options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "zoomTo", [id, zoom, options]);
  },

  getBounds: function(successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getBounds", [id]);
  },

  convertCoordinates: function(coords, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertCoordinates", [id, coords]);
  },

  convertPoint: function(point, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertPoint", [id, point]);
  },

  addOnMapChangeListener: function(listener, callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "addOnMapChangeListener", [id, listener, callback]);
  },
};