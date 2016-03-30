package com.telerik.plugins.mapbox;

import android.util.Log;

import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.PluginResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class OfflineRegion {

    public static final String LOG_TAG = "OfflineRegion";

    public static final String JSON_CHARSET = "UTF-8";
    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_PROPERTIES = "properties";

    private JSONObject metadata;

    private com.mapbox.mapboxsdk.offline.OfflineRegion region;

    protected OfflineRegion(com.mapbox.mapboxsdk.offline.OfflineRegion region) throws JSONException, UnsupportedEncodingException {
        this.region = region;
        byte[] encodedMetadata = region.getMetadata();
        JSONObject metadata = new JSONObject(new String(encodedMetadata, JSON_CHARSET))
                .put(JSON_FIELD_ID, this.getId());
        this.metadata = metadata;
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

    public void bindStatusCallbacks(final CallbackContext onProgress, final CallbackContext onComplete) {
        this.region.setObserver(new com.mapbox.mapboxsdk.offline.OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                try {
                    JSONObject progress = statusToJSON(status);
                    PluginResult result = new PluginResult(PluginResult.Status.OK, progress);
                    if (status.isComplete()) {
                        onComplete.sendPluginResult(result);
                    } else {
                        result.setKeepCallback(true);
                        onProgress.sendPluginResult(result);
                    }
                } catch (JSONException e) {
                    this.onError(e.getMessage());
                }
            }

            @Override
            public void onError(OfflineRegionError error) {
                String message = "OfflineRegionError: [" + error.getReason() + "] " + error.getMessage();
                this.onError(message);
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                this.onError("Tile limit exceeded (limit: " + limit + ")");
            }

            private void onError(String error) {
                Log.e(LOG_TAG, error);
                PluginResult result = new PluginResult(PluginResult.Status.ERROR, error);
                result.setKeepCallback(true);
                onComplete.error(error);
                pause();
            }
        });
    }

    private JSONObject statusToJSON(OfflineRegionStatus status) throws JSONException {
        long completedCount = status.getCompletedResourceCount();
        long requiredCount = status.getRequiredResourceCount();
        double percentage = requiredCount >= 0 ? (100.0 * completedCount / requiredCount) : 0.0;
        JSONObject jsonStatus = new JSONObject()
            .put("completed", status.isComplete())
            .put("downloading", status.getDownloadState() == com.mapbox.mapboxsdk.offline.OfflineRegion.STATE_ACTIVE)
            .put("completedCount", completedCount)
            .put("completedSize", status.getCompletedResourceSize())
            .put("requiredCount", requiredCount)
            .put("percentage", percentage);

        return jsonStatus;
    }
}
