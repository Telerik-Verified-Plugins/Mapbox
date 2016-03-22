package com.telerik.plugins.mapbox;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

public class OfflineRegion {
    // JSON encoding/decoding
    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    private static HashMap<Integer, OfflineRegion> regions = new HashMap<Integer, OfflineRegion>();

    private static HashMap<Long, com.mapbox.mapboxsdk.offline.OfflineRegion> mapboxRegions = new HashMap<Long, com.mapbox.mapboxsdk.offline.OfflineRegion>();

    private static int ids = 0;

    public interface OfflineRegionCreatedCallback {
        void onCreate(OfflineRegion region);
        void onProgress(JSONObject progress);
        void onError(String error);
    }

    public static void create(OfflineManager offlineManager, float retinaFactor, JSONObject options, OfflineRegionCreatedCallback callback) {
        OfflineRegion region = new OfflineRegion(offlineManager, retinaFactor, options, callback);
        regions.put(region.getId(), region);
    }

    public static OfflineRegion getOfflineRegion(int id) {
        return regions.get(id);
    }

    public static void removeOfflineRegion(int id) {
        regions.remove(id);
    }

    private int id;

    private long mapboxOfflineRegionId;

    private OfflineRegionCreatedCallback constructorCallback;

    private String regionName;

    private OfflineRegion(final OfflineManager offlineManager, final float retinaFactor, final JSONObject options, final OfflineRegionCreatedCallback callback) {
        this.id = this.ids++;
        this.constructorCallback = callback;

        try {
            this.regionName = options.getString("name");

            OfflineRegionDefinition definition = this.createOfflineRegionDefinition(retinaFactor, options);
            byte[] encodedMetadata =  this.getMetadata().toString().getBytes(JSON_CHARSET);

            offlineManager.createOfflineRegion(definition, encodedMetadata, new OfflineManager.CreateOfflineRegionCallback() {
                @Override
                public void onCreate(com.mapbox.mapboxsdk.offline.OfflineRegion offlineRegion) {
                    offlineRegion.setObserver(new OfflineRegionObserver(constructorCallback));
                    mapboxOfflineRegionId = offlineRegion.getID();
                    mapboxRegions.put(mapboxOfflineRegionId, offlineRegion);
                    constructorCallback.onCreate(OfflineRegion.this);
                }

                @Override
                public void onError(String error) {
                    constructorCallback.onError(error);
                    OfflineRegion.removeOfflineRegion(getId());
                }
            });
        } catch (JSONException e) {
            constructorCallback.onError(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            constructorCallback.onError(e.getMessage());
        } finally {
            OfflineRegion.removeOfflineRegion(getId());
        }
    }

    public int getId() {
        return this.id;
    }

    public JSONObject getMetadata() throws JSONException {
        JSONObject metadata = new JSONObject();
        metadata.put(JSON_FIELD_REGION_NAME, this.regionName);
        return metadata;
    }

    public void download() {
        mapboxRegions.get(this.mapboxOfflineRegionId).setDownloadState(com.mapbox.mapboxsdk.offline.OfflineRegion.STATE_ACTIVE);
    }

    public void pause() {
        mapboxRegions.get(this.mapboxOfflineRegionId).setDownloadState(com.mapbox.mapboxsdk.offline.OfflineRegion.STATE_INACTIVE);
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

    private class OfflineRegionObserver implements com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionObserver {
        OfflineRegionCreatedCallback constructorCallback;

        OfflineRegionObserver(OfflineRegionCreatedCallback callback) {
            this.constructorCallback = callback;
        }

        @Override
        public void onStatusChanged(OfflineRegionStatus status) {
            try {
                JSONObject progress = new JSONObject();
                progress.put("completedCount", status.getCompletedResourceCount());
                progress.put("completedSize", status.getCompletedResourceSize());
                progress.put("requiredCount", status.getRequiredResourceCount());
                constructorCallback.onProgress(progress);
            } catch (JSONException e) {
                constructorCallback.onError(e.getMessage());
            }
        }

        @Override
        public void onError(OfflineRegionError error) {
            constructorCallback.onError(error.getMessage());
        }

        @Override
        public void mapboxTileCountLimitExceeded(long limit) {
            constructorCallback.onError("Tile limit exceeded (limit: " + limit + ")");
        }
    }

}
