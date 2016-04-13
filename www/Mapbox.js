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
  var element, elemId;

  for (var i = 0; i < children.length; i++) {
    element = children[i];
    elemId = element.getAttribute("__pluginDomId");
    if (!elemId) {
      elemId = "pmb" + Math.floor(Math.random() * Date.now()) + i;
      element.setAttribute("__pluginDomId", elemId);
    }
    elements.push({
      id: elemId,
      size: getDivRect(element)
    });
    i++;
  }
  return elements;
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
  for (var i = 0; i < root.childNodes.length; i++) {
    search(root.childNodes[i]);
  }
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
      options.margins = getAbsoluteMargins(options.div);
    }
    cordova.exec(successCallback, errorCallback, "Mapbox", "show", [id, options]);
  },

  hide: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "hide", [id, options]);
  },

  addMarkers: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "addMarkers", [id, options]);
  },    

  addMarkerCallback: function (callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "addMarkerCallback", [id]);
  },

  animateCamera: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "animateCamera", [id, options]);
  },

  addGeoJSON: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "addGeoJSON", [id, options]);
  },

  setCenter: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setCenter", [id, options]);
  },

  getCenter: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getCenter", [id]);
  },

  setTilt: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setTilt", [id, options]);
  },

  getTilt: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getTilt", [id]);
  },

  getZoomLevel: function (successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "getZoomLevel", [id]);
  },

  setZoomLevel: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "setZoomLevel", [id, options]);
  },

  addPolygon: function (options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "addPolygon", [id, options]);
  },

  convertCoordinate: function(options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertCoordinate", [id, options]);
  },

  convertPoint: function(options, successCallback, errorCallback, id) {
    id = id || 0;
    cordova.exec(successCallback, errorCallback, "Mapbox", "convertPoint", [id, options]);
  },

  onRegionWillChange: function(callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "onRegionWillChange", [id]);
  },

  onRegionIsChanging: function(callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "onRegionIsChanging", [id]);
  },

  onRegionDidChange: function(callback, id) {
    id = id || 0;
    cordova.exec(callback, null, "Mapbox", "onRegionDidChange", [id]);
  }

};