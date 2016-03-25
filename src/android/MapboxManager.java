package com.telerik.plugins.mapbox;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

class MapboxManager {

    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    private String mapboxAccessToken;

    private Float density;

    private CordovaWebView cordovaWebView;

    private OfflineManager offlineManager;

    private HashMap<Integer, OfflineRegion> regions = new HashMap<Integer, OfflineRegion>();

    private HashMap<Long, com.mapbox.mapboxsdk.offline.OfflineRegion> mapboxRegions = new HashMap<Long, com.mapbox.mapboxsdk.offline.OfflineRegion>();

    private int ids = 0;

    public interface OfflineRegionStatusCallback {
        void onComplete(JSONObject progress);
        void onProgress(JSONObject progress);
        void onError(String error);
    }

    public interface LoadOfflineRegionsCallback {
        void onList(JSONArray regions);
        void onError(String error);
    }

    public MapboxManager(String accessToken, Float screenDensity, CordovaWebView webView) {
        this.mapboxAccessToken = accessToken;
        this.density = screenDensity;
        this.cordovaWebView = webView;
        this.offlineManager = OfflineManager.getInstance(webView.getContext());
    }

    public void loadOfflineRegions(final LoadOfflineRegionsCallback callback) {
        this.offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(com.mapbox.mapboxsdk.offline.OfflineRegion[] offlineRegions) {
                try {
                    JSONArray regions = new JSONArray();
                    JSONObject response;
                    for (com.mapbox.mapboxsdk.offline.OfflineRegion offlineRegion : offlineRegions) {
                        OfflineRegion region = createOfflineRegion(offlineRegion);
                        response = new JSONObject();
                        response.put("id", region.getId());
                        regions.put(response);
                    }
                    callback.onList(regions);
                } catch (JSONException e) {
                    String error = "Error loading OfflineRegions: " + e.getMessage();
                    callback.onError(error);
                }
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    public void createOfflineRegion(final JSONObject options, final CallbackContext callback, final OfflineRegionStatusCallback offlineRegionStatusCallback) {
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
                        region.setObserver(offlineRegionStatusCallback);
                        JSONObject resp = new JSONObject();
                        resp.put("id", region.getId());
                        callback.success(resp);
                    } catch (JSONException e) {
                        this.onError(e.getMessage());
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

    private OfflineRegion createOfflineRegion(com.mapbox.mapboxsdk.offline.OfflineRegion offlineRegion) throws JSONException {
        int id = this.ids++;
        OfflineRegion region = new OfflineRegion(id, offlineRegion);
        byte[] encodedMetadata = offlineRegion.getMetadata();
        JSONObject metadata = new JSONObject(encodedMetadata.toString());
        region.setRegionName(metadata.getString(JSON_FIELD_REGION_NAME));
        regions.put(id, region);
        return region;
    }

    public OfflineRegion getOfflineRegion(int id) {
        return regions.get(id);
    }

    public void removeOfflineRegion(int id) {
        regions.remove(id);
    }

    private OfflineRegionDefinition createOfflineRegionDefinition(float retinaFactor, JSONObject options) throws JSONException {
        String styleURL = Mapbox.getStyle(options.getString("style"));
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
}
