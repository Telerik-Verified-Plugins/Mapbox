package com.telerik.plugins.mapbox;

import android.util.Log;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.HashMap;

class MapboxManager {
    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_REGION_NAME = "name";

    public static String getStyle(final String requested) {
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

    private int ids = 0;

    private String accessToken;

    private Float density;

    private CordovaWebView webView;

    private OfflineManager offlineManager;

    private static HashMap<Long, Map> maps = new HashMap<Long, Map>();

    private HashMap<Long, OfflineRegion> regions = new HashMap<Long, OfflineRegion>();

    public interface OfflineRegionStatusCallback {
        void onStatus(JSONObject status);
        void onError(String error);
    }

    public interface OfflineRegionProgressCallback {
        void onComplete(JSONObject progress);
        void onProgress(JSONObject progress);
        void onError(String error);
    }

    public interface LoadOfflineRegionsCallback {
        void onList(JSONArray regions);
        void onError(String error);
    }

    public MapboxManager(String accessToken, Float screenDensity, CordovaWebView webView) {
        this.accessToken = accessToken;
        this.density = screenDensity;
        this.webView = webView;
        this.offlineManager = OfflineManager.getInstance(webView.getContext());
        this.offlineManager.setAccessToken(accessToken);
    }

    public void createMap(final JSONObject options, final CallbackContext callback) {
        try {
            PositionInfo position = new PositionInfo(options.isNull("margins") ? null : options.getJSONObject("margins"), density);
            MapView mapView = createMapView(Map.createMapboxMapOptions(options), position);

            long id = ids++;
            final Map map = new Map(id, mapView);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(MapboxMap mMap) {
                    try {
                        map.setMapboxMap(mMap);
                        map.setFeatureManager(new FeatureManager(webView.getContext(), mMap));

                        if (options.has("markers")) {
                            map.addMarkers(options.getJSONArray("markers"));
                        }

                        maps.put(map.getId(), map);

                        JSONObject resp = new JSONObject();
                        resp.put("id", map.getId());
                        callback.success(resp);
                    } catch (JSONException e) {
                        removeMap(map.getId());
                        callback.error("Failed to create map: " + e.getMessage());
                    }
                }
            });
        } catch (JSONException e) {
            callback.error("Failed to create map: " + e.getMessage());
        }
    }

    private MapView createMapView(MapboxMapOptions options, PositionInfo position) throws JSONException {
        options.accessToken(accessToken);
        MapView mapView = new MapView(this.webView.getContext(), options);

        // need to do this to register a receiver which onPause later needs
        mapView.onResume();
        mapView.onCreate(null);

        int webViewWidth = webView.getView().getWidth();
        int webViewHeight = webView.getView().getHeight();
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                webViewWidth - position.left - position.right,
                webViewHeight - position.top - position.bottom
        );

        params.setMargins(position.left, position.top, position.right, position.bottom);
        mapView.setLayoutParams(params);

        final FrameLayout layout = (FrameLayout) webView.getView().getParent();
        layout.addView(mapView);

        return mapView;
    }

    public Collection<Map> maps() {
        return maps.values();
    }

    public Map getMap(long id) {
        return maps.get(id);
    }

    public void removeMap(long id) {
        maps.remove(id);
    }

    public void loadOfflineRegions(final LoadOfflineRegionsCallback callback) {
        this.offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(com.mapbox.mapboxsdk.offline.OfflineRegion[] offlineRegions) {
                try {
                    JSONArray responses = new JSONArray();
                    JSONObject response;
                    OfflineRegion region;
                    for (com.mapbox.mapboxsdk.offline.OfflineRegion offlineRegion : offlineRegions) {
                        if (regions.containsKey(offlineRegion.getID())) {
                            region = regions.get(offlineRegion.getID());
                        } else {
                            region = createOfflineRegion(offlineRegion);
                        }
                        response = region.getMetadata();
                        response.put(JSON_FIELD_ID, region.getId());
                        responses.put(response);
                    }
                    callback.onList(responses);
                } catch (JSONException e) {
                    this.onError(e.getMessage());
                } catch (UnsupportedEncodingException e) {
                    this.onError(e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    public void createOfflineRegion(final JSONObject options, final CallbackContext callback) {
        try {
            final String regionName = options.getString("name");

            JSONObject metadata = new JSONObject();
            metadata.put(JSON_FIELD_REGION_NAME, regionName);
            byte[] encodedMetadata =  metadata.toString().getBytes(JSON_CHARSET);
            OfflineRegionDefinition definition = this.createOfflineRegionDefinition(density, options);

            offlineManager.createOfflineRegion(definition, encodedMetadata, new OfflineManager.CreateOfflineRegionCallback() {
                @Override
                public void onCreate(com.mapbox.mapboxsdk.offline.OfflineRegion offlineRegion) {
                    try {
                        OfflineRegion region = createOfflineRegion(offlineRegion);
                        JSONObject response = region.getMetadata();
                        response.put(JSON_FIELD_ID, region.getId());
                        callback.success(response);
                    } catch (JSONException e) {
                        this.onError(e.getMessage());
                        removeOfflineRegion(offlineRegion.getID());
                    } catch (UnsupportedEncodingException e) {
                        this.onError(e.getMessage());
                        removeOfflineRegion(offlineRegion.getID());
                    }
                }

                @Override
                public void onError(String error) {
                    String message = "Failed to create offline region: " + error;
                    callback.error(message);
                }
            });
        } catch (JSONException e) {
            callback.error(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            callback.error(e.getMessage());
        }
    }

    private OfflineRegion createOfflineRegion(com.mapbox.mapboxsdk.offline.OfflineRegion offlineRegion) throws JSONException, UnsupportedEncodingException {
        OfflineRegion region = new OfflineRegion(offlineRegion);
        regions.put(offlineRegion.getID(), region);
        return region;
    }

    public OfflineRegion getOfflineRegion(long id) {
        return regions.get(id);
    }

    public void removeOfflineRegion(long id) {
        regions.remove(id);
    }

    private OfflineRegionDefinition createOfflineRegionDefinition(float retinaFactor, JSONObject options) throws JSONException {
        String styleURL = MapboxManager.getStyle(options.getString("style"));
        double minZoom = options.getDouble("minZoom");
        double maxZoom = options.getDouble("maxZoom");
        JSONObject boundsOptions = options.getJSONObject("bounds");
        double north = boundsOptions.getDouble("north");
        double east = boundsOptions.getDouble("east");
        double south = boundsOptions.getDouble("south");
        double west = boundsOptions.getDouble("west");

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(new LatLng(north, west))
                .include(new LatLng(south, east))
                .build();

        return new OfflineTilePyramidRegionDefinition(styleURL, bounds, minZoom, maxZoom, retinaFactor);
    }

    private class PositionInfo {
        int top = 0;
        int right = 0;
        int bottom = 0;
        int left = 0;

        public PositionInfo(JSONObject margins, float density) throws JSONException {
            this.top = margins == null || margins.isNull("top") ? 0 : (int) (density * margins.getInt("top"));
            this.right = margins == null || margins.isNull("right") ? 0 : (int) (density * margins.getInt("right"));
            this.bottom = margins == null || margins.isNull("bottom") ? 0 : (int) (density * margins.getInt("bottom"));
            this.left = margins == null || margins.isNull("left") ? 0 : (int) (density * margins.getInt("left"));
        }
    }
}
