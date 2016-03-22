package com.telerik.plugins.mapbox;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class MapInstance {

    private final static String LOG_TAG = "MapInstance";

    public interface MapCreatedCallback {
        void onMapReady(MapInstance map);
    }

    public static MapInstance createMap(MapView mapView, JSONObject options, MapCreatedCallback callback) {
        MapInstance map = new MapInstance(mapView, options, callback);
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

    private MapInstance(final MapView mapView, final JSONObject options, final MapCreatedCallback callback) {
        this.id = this.ids++;
        this.constructorCallback = callback;
        this.mapView = mapView;

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mMap) {
                mapboxMap = mMap;
                applyOptions(options);
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

    public JSONArray getCenter() throws JSONException {
        CameraPosition cameraPosition = mapboxMap.getCameraPosition();
        double lat = cameraPosition.target.getLatitude();
        double lng = cameraPosition.target.getLongitude();
        double alt = cameraPosition.target.getAltitude();
        return new JSONArray().put(lat).put(lng).put(alt);
    }

    public void setCenter(JSONArray coords) throws JSONException {
        double lng = coords.getDouble(0);
        double lat = coords.getDouble(1);
        //double alt = coords.getDouble(2);

        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lat, lng))
                        .build()
        ));
    }

    public double getZoom() {
        CameraPosition cameraPosition = mapboxMap.getCameraPosition();
        return cameraPosition.zoom;
    }

    public void setZoom(double zoom) {
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .zoom(zoom)
                        .build()
        ));
    }

    public void jumpTo(JSONObject options) throws JSONException {
        CameraPosition current = mapboxMap.getCameraPosition();
        CameraPosition.Builder builder = new CameraPosition.Builder(current);

        if (!options.isNull("zoom")) {
            builder.zoom(options.getDouble("zoom"));
        }

        if (!options.isNull("center")) {
            JSONArray center = options.getJSONArray("center");
            double lng = center.getDouble(0);
            double lat = center.getDouble(1);
            builder.target(new LatLng(lat, lng));
        }

        // TODO: Bearing

        // TODO: Pitch

        CameraPosition position = builder.build();
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(position));
    }

    public void addMarkers(JSONArray markers) throws JSONException {
        for (int i = 0; i < markers.length(); i++) {
            final JSONObject marker = markers.getJSONObject(i);
            final MarkerOptions mo = new MarkerOptions();

            mo.title(marker.isNull("title") ? null : marker.getString("title"));
            mo.snippet(marker.isNull("subtitle") ? null : marker.getString("subtitle"));
            mo.position(new LatLng(marker.getDouble("lat"), marker.getDouble("lng")));

            mapboxMap.addMarker(mo);
        }
    }

    public void addMarkerListener(MapboxMap.OnInfoWindowClickListener listener) {
        mapboxMap.setOnInfoWindowClickListener(listener);
    }

    public void showUserLocation(boolean enabled) {
        mapboxMap.setMyLocationEnabled(enabled);
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

    private void applyOptions(JSONObject options) {
        try {
            if (options.has("style")) {
                this.mapView.setStyleUrl(this.getStyle(options.optString("style")));
            }

            if (!options.isNull("showUserLocation")) {
                this.showUserLocation(options.getBoolean("showUserLocation"));
            }

            UiSettings uiSettings = mapboxMap.getUiSettings();
            uiSettings.setCompassEnabled(options.isNull("hideCompass") || !options.getBoolean("hideCompass"));
            uiSettings.setRotateGesturesEnabled(options.isNull("disableRotation") || !options.getBoolean("disableRotation"));
            uiSettings.setScrollGesturesEnabled(options.isNull("disableScroll") || !options.getBoolean("disableScroll"));
            uiSettings.setZoomGesturesEnabled(options.isNull("disableZoom") || !options.getBoolean("disableZoom"));
            uiSettings.setTiltGesturesEnabled(options.isNull("disableTilt") || !options.getBoolean("disableTilt"));

            if (!options.isNull("hideAttribution") && options.getBoolean("hideAttribution")) {
                uiSettings.setAttributionMargins(-300, 0, 0, 0);
            }

            if (!options.isNull("hideLogo") && options.getBoolean("hideLogo")) {
                uiSettings.setLogoMargins(-300, 0, 0, 0);
            }

            if (options.has("markers")) {
                this.addMarkers(options.getJSONArray("markers"));
            }

            this.jumpTo(options);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }
    }
}