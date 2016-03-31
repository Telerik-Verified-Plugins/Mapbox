package com.telerik.plugins.mapbox;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONObject;

public class GeoJSONMarkerOptions extends BaseMarkerOptions<GeoJSONMarker, GeoJSONMarkerOptions> {
    private long featureId;
    private JSONObject properties;

    public GeoJSONMarkerOptions properties(JSONObject properties) {
        this.properties = properties;
        return this.getThis();
    }

    public GeoJSONMarkerOptions featureId(long id) {
        this.featureId = id;
        return this.getThis();
    }

    public GeoJSONMarkerOptions() {

    }

    private GeoJSONMarkerOptions(Parcel in) {
        position((LatLng) in.readParcelable(LatLng.class.getClassLoader()));
        snippet(in.readString());
        String iconId = in.readString();
        Bitmap iconBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        Icon icon = IconFactory.recreate(iconId, iconBitmap);
        icon(icon);
        title(in.readString());
    }

    @Override
    public GeoJSONMarkerOptions getThis() {
        return this;
    }

    @Override
    public GeoJSONMarker getMarker() {
        return new GeoJSONMarker(this, this.properties, this.featureId);
    }

    public static final Parcelable.Creator<GeoJSONMarkerOptions> CREATOR = new Parcelable.Creator<GeoJSONMarkerOptions>() {
        public GeoJSONMarkerOptions createFromParcel(Parcel in) {
            return new GeoJSONMarkerOptions(in);
        }

        public GeoJSONMarkerOptions[] newArray(int size) {
            return new GeoJSONMarkerOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(position, flags);
        out.writeString(snippet);
        out.writeString(icon.getId());
        out.writeParcelable(icon.getBitmap(), flags);
        out.writeString(title);
    }
}