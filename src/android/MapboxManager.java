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
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_REGION_NAME = "name";

    private Float density;

    private OfflineManager offlineManager;

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
        this.density = screenDensity;
        this.offlineManager = OfflineManager.getInstance(webView.getContext());
        this.offlineManager.setAccessToken(accessToken);
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

    public void createOfflineRegion(final JSONObject options, final CallbackContext callback, final OfflineRegionProgressCallback offlineRegionStatusCallback) {
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
                        JSONObject response = region.getMetadata();
                        response.put(JSON_FIELD_ID, region.getId());
                        callback.success(response);
                    } catch (JSONException e) {
                        this.onError(e.getMessage());
                    } catch (UnsupportedEncodingException e) {
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
