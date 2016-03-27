package com.telerik.plugins.mapbox;

import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Map {
    public static MapboxMapOptions createMapboxMapOptions(JSONObject options) throws JSONException {
        MapboxMapOptions opts = new MapboxMapOptions();
        opts.styleUrl(MapboxManager.getStyle(options.getString("style")));
        opts.attributionEnabled(options.isNull("hideAttribution") || !options.getBoolean("hideAttribution"));
        opts.logoEnabled(options.isNull("hideLogo") || options.getBoolean("hideLogo"));
        opts.locationEnabled(!options.isNull("showUserLocation") && options.getBoolean("showUserLocation"));
        opts.camera(Map.getCameraPostion(options, null));
        opts.compassEnabled(options.isNull("hideCompass") || !options.getBoolean("hideCompass"));
        opts.rotateGesturesEnabled(options.isNull("disableRotation") || !options.getBoolean("disableRotation"));
        opts.scrollGesturesEnabled(options.isNull("disableScroll") || !options.getBoolean("disableScroll"));
        opts.zoomGesturesEnabled(options.isNull("disableZoom") || !options.getBoolean("disableZoom"));
        opts.tiltGesturesEnabled(options.isNull("disableTilt") || !options.getBoolean("disableTilt"));
        return opts;
    }

    public static CameraPosition getCameraPostion(JSONObject options, @Nullable CameraPosition start) throws JSONException {
        CameraPosition.Builder builder = new CameraPosition.Builder(start);

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

        return builder.build();
    }

    private long id;

    private MapView mapView;

    private MapboxMap mapboxMap;

    private FeatureManager features;

    public Map(long id, final MapView mapView) {
        this.id = id;
        this.mapView = mapView;
    }

    public long getId() {
        return this.id;
    }

    public MapView getMapView() {
        return this.mapView;
    }

    public void setMapboxMap(MapboxMap mMap) {
        this.mapboxMap = mMap;
    }

    public void setFeatureManager(FeatureManager featureManager) {
        this.features = featureManager;
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
        CameraPosition position = Map.getCameraPostion(options, mapboxMap.getCameraPosition());
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

    public void addSource(String name, JSONObject source) throws UnsupportedTypeException, JSONException {
        final String sourceType = source.getString("type");

        if (sourceType.equals("geojson") && source.has("data")) {
            final JSONObject data = source.getJSONObject("data");
            features.addGeoJSONSource(name, data);
        } else {
            throw new UnsupportedTypeException("source:" + sourceType);
        }
    }

    public void addLayer(JSONObject layer) throws UnknownSourceException, UnsupportedTypeException, JSONException {
        final String layerType = layer.getString("type");
        final String source = layer.getString("source");
        final String id = layer.getString("id");

        if (features.hasSource(source)) {
            if (layerType.equals("fill")) {
                features.addFillLayer(id, source, layer);
            } else if (layerType.equals("line")) {
                features.addLineLayer(id, source, layer);
            } else if (layerType.equals("symbol")) {
                features.addMarkerLayer(id, source, layer);
            } else {
                throw new UnsupportedTypeException("layer:" + layerType);
            }
        } else {
            throw new UnknownSourceException(source);
        }
    }
}

class UnsupportedTypeException extends Exception {
    String type;

    public UnsupportedTypeException(String type) {
        this.type = type;
    }

    @Override
    public String getMessage() {
        return "Unsupported type: " + this.type;
    }
}

class UnknownSourceException extends Exception {
    String source;

    public UnknownSourceException(String source) {
        this.source = source;
    }

    @Override
    public String getMessage() {
        return "Unknown source: " + this.source;
    }
}