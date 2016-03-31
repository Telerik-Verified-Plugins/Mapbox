package com.telerik.plugins.mapbox;

import com.mapbox.mapboxsdk.annotations.Marker;

import org.json.JSONObject;

public class GeoJSONMarker extends Marker {
    private JSONObject properties;

    private long featureId;

    public GeoJSONMarker(GeoJSONMarkerOptions options, JSONObject properties, long featureId) {
        super(options);
        this.properties = properties;
        this.featureId = featureId;
    }

    public JSONObject getProperties() {
        return this.properties != null ? this.properties : new JSONObject();
    }

    public long getFeatureId() {
        return this.featureId;
    }
}