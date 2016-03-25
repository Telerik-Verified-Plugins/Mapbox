package com.telerik.plugins.mapbox;

import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class OfflineRegion {

    public static final String JSON_CHARSET = "UTF-8";

    private JSONObject metadata;

    private com.mapbox.mapboxsdk.offline.OfflineRegion region;

    protected OfflineRegion(com.mapbox.mapboxsdk.offline.OfflineRegion region) throws JSONException, UnsupportedEncodingException {
        this.region = region;
        byte[] encodedMetadata = region.getMetadata();
        this.metadata = new JSONObject(new String(encodedMetadata, JSON_CHARSET));
    }

    public Long getId() {
        return this.region.getID();
    }

    public JSONObject getMetadata() {
        return this.metadata;
    }

    public void getStatus(final MapboxManager.OfflineRegionStatusCallback statusCallback) {
        this.region.getStatus(new com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionStatusCallback() {
            @Override
            public void onStatus(OfflineRegionStatus status) {
                try {
                    statusCallback.onStatus(statusToJSON(status));
                } catch (JSONException e) {
                    this.onError(e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                statusCallback.onError(error);
            }
        });
    }

    public void download() {
        this.region.setDownloadState(com.mapbox.mapboxsdk.offline.OfflineRegion.STATE_ACTIVE);
    }

    public void pause() {
        this.region.setDownloadState(com.mapbox.mapboxsdk.offline.OfflineRegion.STATE_INACTIVE);
    }

    public void setObserver(MapboxManager.OfflineRegionProgressCallback statusCallback) {
        this.region.setObserver(new OfflineRegionObserver(statusCallback));
    }

    private JSONObject statusToJSON(OfflineRegionStatus status) throws JSONException {
        long completedCount = status.getCompletedResourceCount();
        long requiredCount = status.getRequiredResourceCount();
        double percentage = requiredCount >= 0 ? (100.0 * completedCount / requiredCount) : 0.0;
        JSONObject jsonStatus = new JSONObject()
            .put("completedCount", completedCount)
            .put("completedSize", status.getCompletedResourceSize())
            .put("requiredCount", requiredCount)
            .put("percentage", percentage);

        return jsonStatus;
    }

    private class OfflineRegionObserver implements com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionObserver {
        private MapboxManager.OfflineRegionProgressCallback progressCallback;

        OfflineRegionObserver(MapboxManager.OfflineRegionProgressCallback callback) {
            this.progressCallback = callback;
        }

        @Override
        public void onStatusChanged(OfflineRegionStatus status) {
            try {
                JSONObject progress = statusToJSON(status);
                if (!status.isComplete()) {
                    progressCallback.onProgress(progress);
                } else {
                    progressCallback.onComplete(progress);
                }
            } catch (JSONException e) {
                progressCallback.onError(e.getMessage());
            }
        }

        @Override
        public void onError(OfflineRegionError error) {
            String message = "OfflineRegionError: [" + error.getReason() + "] " + error.getMessage();
            progressCallback.onError(message);
            pause();
        }

        @Override
        public void mapboxTileCountLimitExceeded(long limit) {
            progressCallback.onError("Tile limit exceeded (limit: " + limit + ")");
        }
    }
}
