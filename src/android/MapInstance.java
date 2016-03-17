package com.telerik.plugins.mapbox;

import android.content.Context;
import android.webkit.WebView;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;

import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MapInstance {

    public interface MapCreatedCallback {
        void onMapReady(MapInstance map);
    }

    public static MapInstance createMap(MapView mapView, MapCreatedCallback callback) {
        MapInstance map = new MapInstance(mapView, callback);
        maps.put(map.getId(), map);
        return map;
    }

    public static MapInstance getMap(int id) {
        return maps.get(id);
    }

    private static HashMap<Integer, MapInstance> maps = new HashMap<Integer, MapInstance>();

    private static int ids = 0;

    private int id;

    private MapView mapView;

    private MapboxMap mapboxMap;

    private MapCreatedCallback constructorCallback;

    private MapInstance(MapView mapView, MapCreatedCallback callback) {
        this.id = this.ids++;
        this.constructorCallback = callback;
        this.mapView = mapView;

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mMap) {
                mapboxMap = mMap;
                mapboxMap.setMyLocationEnabled(false);
                constructorCallback.onMapReady(MapInstance.this);
            }
        });
    }

    public int getId() {
        return this.id;
    }

    public MapView getMapView() {
        return this.mapView;
    }

    public MapboxMap getMapboxMap() {
        return this.mapboxMap;
    }

    public void configure(JSONObject options) throws JSONException {
        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setCompassEnabled(options.isNull("hideCompass") || !options.getBoolean("hideCompass"));
        uiSettings.setRotateGesturesEnabled(options.isNull("disableRotation") || !options.getBoolean("disableRotation"));
        uiSettings.setScrollGesturesEnabled(options.isNull("disableScroll") || !options.getBoolean("disableScroll"));
        uiSettings.setZoomGesturesEnabled(options.isNull("disableZoom") || !options.getBoolean("disableZoom"));
        uiSettings.setTiltGesturesEnabled(options.isNull("disableTilt") || !options.getBoolean("disableTilt"));
    }

    public void show(CordovaWebView webView, float retinaFactor, JSONObject options) throws JSONException {

    }

    private static String getStyle(final String requested) {
        if ("light".equalsIgnoreCase(requested)) {
            return Style.LIGHT;
        } else if ("dark".equalsIgnoreCase(requested)) {
            return Style.DARK;
        } else if ("emerald".equalsIgnoreCase(requested)) {
            return Style.EMERALD;
        } else if ("satellite".equalsIgnoreCase(requested)) {
            return Style.SATELLITE;
        // TODO not currently supported on Android
        //} else if ("hybrid".equalsIgnoreCase(requested)) {
        //    return Style.HYBRID;
        } else if ("streets".equalsIgnoreCase(requested)) {
            return Style.MAPBOX_STREETS;
        } else {
            return requested;
        }
    }

//    public void show(JSONObject options, CallbackContext callbackContext) {
//
//
//        try {
//
//
//            // placing these offscreen in case the user wants to hide them
//            if (!options.isNull("hideAttribution") && options.getBoolean("hideAttribution")) {
//                mapView.setAttributionMargins(-300, 0, 0, 0);
//            }
//            if (!options.isNull("hideLogo") && options.getBoolean("hideLogo")) {
//                mapView.setLogoMargins(-300, 0, 0, 0);
//            }
//
//            if (showUserLocation) {
//                showUserLocation();
//            }
//
//            Double zoom = options.isNull("zoomLevel") ? 10 : options.getDouble("zoomLevel");
//            float zoomLevel = zoom.floatValue();
//            if (center != null) {
//                final double lat = center.getDouble("lat");
//                final double lng = center.getDouble("lng");
//                mapView.setLatLng(new LatLngZoom(lat, lng, zoomLevel));
//            } else {
//                if (zoomLevel > 18.0) {
//                    zoomLevel = 18.0f;
//                }
//                mapView.setZoom(zoomLevel);
//            }
//
//            if (options.has("markers")) {
//                addMarkers(options.getJSONArray("markers"));
//            }
//        } catch (JSONException e) {
//            callbackContext.error(e.getMessage());
//            return;
//        }
//
//        mapView.setStyleUrl(style);
//    }
}
