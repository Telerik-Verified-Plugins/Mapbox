package com.telerik.plugins.mapbox;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONException;
import org.json.JSONObject;

public class OfflineRegion {
    protected interface OfflineRegionCreateCallback {
        void onCreate(OfflineRegion region);
        void onError(String error);
    }

    private int id;

    private long mapboxOfflineRegionId;

    private OfflineRegionCreateCallback createCallback;

    private String regionName;

    private com.mapbox.mapboxsdk.offline.OfflineRegion region;

    protected OfflineRegion(int id, com.mapbox.mapboxsdk.offline.OfflineRegion region) {
        this.id = id;
        this.region = region;
    }

    public int getId() {
        return this.id;
    }

    public void setRegionName(String regionName) {
        this.regionName = regionName;
    }

    public void download() {
        this.region.setDownloadState(com.mapbox.mapboxsdk.offline.OfflineRegion.STATE_ACTIVE);
    }

    public void pause() {
        this.region.setDownloadState(com.mapbox.mapboxsdk.offline.OfflineRegion.STATE_INACTIVE);
    }

    public void setObserver(MapboxManager.OfflineRegionStatusCallback statusCallback) {
        this.region.setObserver(new OfflineRegionObserver(statusCallback));
    }

    private class OfflineRegionObserver implements com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionObserver {
        MapboxManager.OfflineRegionStatusCallback statusCallback;

        OfflineRegionObserver(MapboxManager.OfflineRegionStatusCallback callback) {
            this.statusCallback = callback;
        }

        @Override
        public void onStatusChanged(OfflineRegionStatus status) {
            long completedCount = status.getCompletedResourceCount();
            long requiredCount = status.getRequiredResourceCount();
            double percentage = requiredCount >= 0 ? (100.0 * completedCount / requiredCount) : 0.0;
            JSONObject progress = new JSONObject();

            try {
                progress.put("completedCount", completedCount);
                progress.put("completedSize", status.getCompletedResourceSize());
                progress.put("requiredCount", requiredCount);
                progress.put("percentage", percentage);
            } catch (JSONException e) {
                statusCallback.onError(e.getMessage());
                return;
            }

            if (status.isComplete()) {
                statusCallback.onComplete(progress);
            } else {
                statusCallback.onProgress(progress);
            }
        }

        @Override
        public void onError(OfflineRegionError error) {
            String message = "OfflineRegionError: [" + error.getReason() + "] " + error.getMessage();
            statusCallback.onError(message);
            pause();
        }

        @Override
        public void mapboxTileCountLimitExceeded(long limit) {
            statusCallback.onError("Tile limit exceeded (limit: " + limit + ")");
        }
    }
}
