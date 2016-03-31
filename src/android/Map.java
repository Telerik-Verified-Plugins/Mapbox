package com.telerik.plugins.mapbox;

import android.location.Location;
import android.support.annotation.Nullable;

import com.mapbox.mapboxsdk.annotations.Marker;
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

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_PROPERTIES = "properties";
    public static final String JSON_FIELD_LNGLAT = "lngLat";

    public static final int EVENT_CAMERACHANGE = 0;
    public static final int EVENT_FLING = 1;
    public static final int EVENT_INFOWINDOWCLICK = 2;
    public static final int EVENT_INFOWINDOWCLOSE = 3;
    public static final int EVENT_INFOWINDOWLONGCLICK = 4;
    public static final int EVENT_MAPCLICK = 5;
    public static final int EVENT_MAPLONGCLICK = 6;
    public static final int EVENT_MARKERCLICK = 7;
    public static final int EVENT_BEARINGTRACKINGMODECHANGE = 8;
    public static final int EVENT_LOCATIONCHANGE = 9;
    public static final int EVENT_LOCATIONTRACKINGMODECHANGE = 10;
    public static final int EVENT_ONSCROLL = 11;

    public interface MapEventListener {
        void onEvent(int code, JSONObject event);
        void onError(String message);
    }

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

    private MapEventListener eventListener;

    private FeatureManager features;

    public Map(long id, MapEventListener eventListener, final MapView mapView) {
        this.id = id;
        this.mapView = mapView;
        this.eventListener = eventListener;
    }

    public long getId() {
        return this.id;
    }

    public MapView getMapView() {
        return this.mapView;
    }

    public JSONArray latLngToJSON(LatLng latLng) throws JSONException {
        return new JSONArray()
                .put(latLng.getLongitude())
                .put(latLng.getLatitude());
    }

    public JSONObject getMarkerJSON(Marker marker) throws JSONException {
        LatLng point = marker.getPosition();
        JSONObject data = new JSONObject()
                .put(JSON_FIELD_LNGLAT, latLngToJSON(point));

        if (marker instanceof GeoJSONMarker) {
            long featureId = ((GeoJSONMarker) marker).getFeatureId();
            data.put(JSON_FIELD_ID, featureId);
            data.put(JSON_FIELD_PROPERTIES, features.getMarker(featureId).getProperties());
        }

        return data;
    }

    public void setMapboxMap(MapboxMap mMap) {
        this.mapboxMap = mMap;

        this.mapboxMap.setOnCameraChangeListener(new MapboxMap.OnCameraChangeListener() {
            @Override
            public void onCameraChange(CameraPosition position) {
                eventListener.onEvent(EVENT_CAMERACHANGE, new JSONObject());
            }
        });

        this.mapboxMap.setOnFlingListener(new MapboxMap.OnFlingListener() {
            @Override
            public void onFling() {
                eventListener.onEvent(EVENT_FLING, new JSONObject());
            }
        });

        this.mapboxMap.setOnInfoWindowClickListener(new MapboxMap.OnInfoWindowClickListener() {
            @Override
            public boolean onInfoWindowClick(Marker marker) {
                try {
                    JSONObject data = getMarkerJSON(marker);
                    eventListener.onEvent(EVENT_INFOWINDOWCLICK, data);
                    return true;
                } catch (JSONException e) {
                    eventListener.onError(e.getMessage());
                    return false;
                }
            }
        });

        this.mapboxMap.setOnInfoWindowCloseListener(new MapboxMap.OnInfoWindowCloseListener() {
            @Override
            public void onInfoWindowClose(Marker marker) {
                try {
                    JSONObject data = getMarkerJSON(marker);
                    eventListener.onEvent(EVENT_INFOWINDOWCLOSE, data);
                } catch (JSONException e) {
                    eventListener.onError(e.getMessage());
                }
            }
        });

        this.mapboxMap.setOnInfoWindowLongClickListener(new MapboxMap.OnInfoWindowLongClickListener() {
            @Override
            public void onInfoWindowLongClick(Marker marker) {
                try {
                    JSONObject data = getMarkerJSON(marker);
                    eventListener.onEvent(EVENT_INFOWINDOWLONGCLICK, data);
                } catch (JSONException e) {
                    eventListener.onError(e.getMessage());
                }
            }
        });

        this.mapboxMap.setOnMapClickListener(new MapboxMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                try {
                    JSONObject data = new JSONObject()
                            .put("lngLat", latLngToJSON(point));
                    eventListener.onEvent(EVENT_MAPCLICK, data);
                } catch (JSONException e) {
                    eventListener.onError(e.getMessage());
                }
            }
        });

        this.mapboxMap.setOnMapLongClickListener(new MapboxMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                try {
                    JSONObject data = new JSONObject()
                            .put("lngLat", latLngToJSON(point));
                    eventListener.onEvent(EVENT_MAPLONGCLICK, data);
                } catch (JSONException e) {
                    eventListener.onError(e.getMessage());
                }
            }
        });

        this.mapboxMap.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                try {
                    JSONObject data = getMarkerJSON(marker);
                    eventListener.onEvent(EVENT_MARKERCLICK, data);
                    return true;
                } catch (JSONException e) {
                    eventListener.onError(e.getMessage());
                    return false;
                }
            }
        });

        this.mapboxMap.setOnMyBearingTrackingModeChangeListener(new MapboxMap.OnMyBearingTrackingModeChangeListener() {
            @Override
            public void onMyBearingTrackingModeChange(int myBearingTrackingMode) {
                eventListener.onEvent(EVENT_BEARINGTRACKINGMODECHANGE, new JSONObject());
            }
        });

        this.mapboxMap.setOnMyLocationChangeListener(new MapboxMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(@Nullable Location location) {
                eventListener.onEvent(EVENT_LOCATIONCHANGE, new JSONObject());
            }
        });

        this.mapboxMap.setOnMyLocationTrackingModeChangeListener(new MapboxMap.OnMyLocationTrackingModeChangeListener() {
            @Override
            public void onMyLocationTrackingModeChange(int myLocationTrackingMode) {
                eventListener.onEvent(EVENT_LOCATIONTRACKINGMODECHANGE, new JSONObject());
            }
        });

        this.mapboxMap.setOnScrollListener(new MapboxMap.OnScrollListener() {
            @Override
            public void onScroll() {
                eventListener.onEvent(EVENT_ONSCROLL, new JSONObject());
            }
        });
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
            JSONObject marker = markers.getJSONObject(i);
            LatLng latLng = new LatLng(marker.getDouble("lat"), marker.getDouble("lng"));
            String title = marker.isNull("title") ? null : marker.getString("title");
            String snippet = marker.isNull("subtitle") ? null : marker.getString("subtitle");
            mapboxMap.addMarker(features.createMarker(latLng, title, snippet));
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