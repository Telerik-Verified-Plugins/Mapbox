package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.cordova.hellocordova.R;

/**
 * Created by vikti on 25/06/2016.
 * This file handles the concrete action with MapBox API.
 * //todo decouple the file from MapBox to easily switch to other APIs. Need interface.
 */
public class MapController {
    public FrameLayout.LayoutParams mapFrame;

    private static float retinaFactor;

    private MapView _mapView;
    private MapboxMapOptions _initOptions;
    private MapboxMap _mapboxMap;
    private UiSettings _uiSettings;
    private CameraPosition _cameraPosition;
    private Activity _activity;

    public View getMapView(){
        return (View) _mapView;
    }

    public MapController(final JSONObject options, Activity activity, final CallbackContext callbackContext){

        try{
            _initOptions = _createMapboxMapOptions(options);
        } catch (JSONException e){
            e.printStackTrace();
            return;
        }

        _activity = activity;

        _mapView = new MapView(_activity, _initOptions);
        _mapView.setLayoutParams(
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

        // need to do this to register a receiver which onPause later needs
        _mapView.onResume();
        _mapView.onCreate(null);

        _mapView.getMapAsync(new OnMapReadyCallback() {

            public void onMapReady(MapboxMap map) {

                _mapboxMap = map;
                /*
                _uiSettings = _mapboxMap.getUiSettings();
                _cameraPosition = _mapboxMap.getCameraPosition();
                final JSONObject _initArgs;
                final JSONObject margins;
                final int left;
                final int right;
                final int top;
                final int bottom;
                final JSONObject center;

                try {
                    _initArgs = args.getJSONObject(1);
                    margins = _initArgs.isNull("margins") ? null : _initArgs.getJSONObject("margins");
                    center = _initArgs.isNull("center") ? null : _initArgs.getJSONObject("center");
                    left = applyRetinaFactor(margins == null || margins.isNull("left") ? 0 : margins.getInt("left"));
                    right = applyRetinaFactor(margins == null || margins.isNull("right") ? 0 : margins.getInt("right"));
                    top = applyRetinaFactor(margins == null || margins.isNull("top") ? 0 : margins.getInt("top"));
                    bottom = applyRetinaFactor(margins == null || margins.isNull("bottom") ? 0 : margins.getInt("bottom"));

                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                    return;
                }
                /*
                final String style = getStyle(_initArgs.optString("style"));
                _mapView.setStyleUrl(style);

                _showUserLocation = !_initArgs.isNull("showUserLocation") && _initArgs.getBoolean("showUserLocation");


                _uiSettings.setCompassEnabled(_initArgs.isNull("hideCompass") || !_initArgs.getBoolean("hideCompass"));
                _uiSettings.setRotateGesturesEnabled(_initArgs.isNull("disableRotation") || !_initArgs.getBoolean("disableRotation"));
                _uiSettings.setScrollGesturesEnabled(_initArgs.isNull("disableScroll") || !_initArgs.getBoolean("disableScroll"));
                _uiSettings.setZoomGesturesEnabled(_initArgs.isNull("disableZoom") || !_initArgs.getBoolean("disableZoom"));
                _uiSettings.setTiltGesturesEnabled(_initArgs.isNull("disableTilt") || !_initArgs.getBoolean("disableTilt"));

                // placing these offscreen in case the user wants to hide them
                if (!_initArgs.isNull("hideAttribution") && _initArgs.getBoolean("hideAttribution")) {
                    _uiSettings.setAttributionMargins(-300, 0, 0, 0);
                }
                if (!_initArgs.isNull("hideLogo") && _initArgs.getBoolean("hideLogo")) {
                    _uiSettings.setLogoMargins(-300, 0, 0, 0);
                }

                if (_showUserLocation) {
                    _showUserLocation();
                }

                double zoom = _initArgs.isNull("zoomLevel") ? 10 : _initArgs.getDouble("zoomLevel");
                float zoomLevel = (float) zoom;
                if (center != null) {
                    final double lat = center.getDouble("lat");
                    final double lng = center.getDouble("lng");
                    setCenter(new LatLng(lat, lng));
                } else {
                    if (zoomLevel > 18.0) {
                        zoomLevel = 18.0f;
                    }
                    setZoom(zoomLevel);
                }

                if (_initArgs.has("markers")) {
                    addMarkers(_initArgs.getJSONArray("markers"));
                }
*/
                // position the _mapView overlay
               // _mapView.setLayoutParams(mapFrame);
            }
        });

    }

    public LatLng getCenter(){
        CameraPosition cameraPosition = _mapboxMap.getCameraPosition();
        double lat = cameraPosition.target.getLatitude();
        double lng = cameraPosition.target.getLongitude();
        return new LatLng(lat, lng);
    }

    public void setCenter(double... coords){
        CameraPosition cameraPosition = _mapboxMap.getCameraPosition();
        double lat = coords.length > 0 ? coords[0] : cameraPosition.target.getLatitude();
        double lng = coords.length > 1 ? coords[1] : cameraPosition.target.getLongitude();
        double alt = coords.length > 2 ? coords[2] : cameraPosition.target.getAltitude(); //todo alt or zoom ????

        _mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                    .target(new LatLng(lat, lng, alt))
                    .build()
        ));
    }

    public double getTilt(){
        return _mapboxMap.getCameraPosition().tilt;
    }

    public void setTilt(double titl){
        _mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .tilt(titl)
                        .build()
        ));
    }

    public void flyTo(JSONObject position) throws JSONException{
        CameraPosition cameraPosition = _mapboxMap.getCameraPosition();
        LatLng latLng;
        try{
            int duration = position.isNull("duration") ? 5000 : position.getInt("duration");

            _mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(MapController.getCameraPosition(position, cameraPosition)), duration);

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    public void addGeoJSON() throws JSONException{

    }

    public void addMarkers(JSONArray markers) throws JSONException{
        for (int i = 0; i < markers.length(); i++) {
            JSONObject marker = markers.getJSONObject(i);
            LatLng latLng = new LatLng(
                    marker.getJSONObject("geometry").getJSONArray("coordinates").getDouble(1),
                    marker.getJSONObject("geometry").getJSONArray("coordinates").getDouble(0)
            );

            String title = null;
            String snippet = null;
            boolean noIcon = true;

            JSONObject properties = marker.isNull("properties") ? null : marker.getJSONObject("properties");

            if(properties != null) {
                title = properties.isNull("title") ? null : properties.getString("title");
                snippet = properties.isNull("subtitle") ? null : properties.getString("subtitle");
                noIcon = !properties.isNull("noIcon") && properties.getBoolean("noIcon");
            }

            if(noIcon){
                _mapboxMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .snippet(snippet));
            } else {
                // Create an Icon object for the marker to use
                IconFactory iconFactory = IconFactory.getInstance(_activity);
                Drawable iconDrawable = ContextCompat.getDrawable(_activity, R.drawable.default_marker);
                Icon icon = iconFactory.fromDrawable(iconDrawable);

                // Add the custom icon marker to the map
                _mapboxMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(title)
                        .snippet(snippet)
                        .icon(icon));
            }
        }
    }

    public double getZoom(){
        return _mapboxMap.getCameraPosition().zoom;
    }

    public void setZoom(double zoom){
        CameraPosition position = new CameraPosition.Builder()
                .zoom(zoom)
                .build();

        _mapboxMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(position));

    }

    public void zoomTo(double zoom){
        CameraPosition position = new CameraPosition.Builder()
                .zoom(zoom)
                .build();

        _mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position));

    }


    public void setCenter(LatLng coords) throws JSONException {
        _mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(coords)
                        .build()
        ));
    }

    public PointF convertCoordinates(LatLng coords){
        return _mapboxMap.getProjection().toScreenLocation(coords);
    }

    public LatLng convertPoint(PointF point){
        return _mapboxMap.getProjection().fromScreenLocation(point);
    }

    private static int applyRetinaFactor(int i) {
        return (int) (i * retinaFactor);
    }

    private MapboxMapOptions _createMapboxMapOptions(JSONObject options) throws JSONException {
        MapboxMapOptions opts = new MapboxMapOptions();
        opts.styleUrl(_getStyle(options.getString("style")));
        opts.attributionEnabled(options.isNull("hideAttribution") || !options.getBoolean("hideAttribution"));
        opts.logoEnabled(options.isNull("hideLogo") || options.getBoolean("hideLogo"));
        //opts.locationEnabled(!options.isNull("showUserLocation") && options.getBoolean("showUserLocation")); // todo bug #5607
        //opts.camera(MapController.getCameraPosition(options.isNull("position")?null:options.getJSONObject("position"), null)); // todo bug #5607
        opts.compassEnabled(options.isNull("hideCompass") || !options.getBoolean("hideCompass"));
        opts.rotateGesturesEnabled(options.isNull("disableRotation") || !options.getBoolean("disableRotation"));
        opts.scrollGesturesEnabled(options.isNull("disableScroll") || !options.getBoolean("disableScroll"));
        opts.zoomGesturesEnabled(options.isNull("disableZoom") || !options.getBoolean("disableZoom"));
        opts.tiltGesturesEnabled(options.isNull("disableTilt") || !options.getBoolean("disableTilt"));
        return opts;
    }

    private static String _getStyle(final String requested) {
        if ("light".equalsIgnoreCase(requested)) {
            return Style.LIGHT;
        } else if ("dark".equalsIgnoreCase(requested)) {
            return Style.DARK;
        } else if ("emerald".equalsIgnoreCase(requested)) {
            return Style.EMERALD;
        } else if ("satellite".equalsIgnoreCase(requested)) {
            return Style.SATELLITE;
            // TODO not currently supported on Android
//    } else if ("hybrid".equalsIgnoreCase(requested)) {
//      return Style.HYBRID;
        } else if ("streets".equalsIgnoreCase(requested)) {
            return Style.MAPBOX_STREETS;
        } else {
            return requested;
        }
    }

    public static CameraPosition getCameraPosition(JSONObject position, @Nullable CameraPosition start) throws JSONException {
        CameraPosition.Builder builder = new CameraPosition.Builder(start);

        if(position != null) {
            if (!position.isNull("target")) {
                JSONObject target = position.getJSONObject("target");
                builder.target(new LatLng(target.getDouble("lat"), target.getDouble("lng")));
            }

            if (!position.isNull("zoom")) {
                builder.zoom(position.getDouble("zoom"));
            }

            if (!position.isNull("bearing")) {
                builder.bearing(position.getDouble("bearing"));
            }

            if (!position.isNull("tilt")) {
                builder.tilt(position.getDouble("tilt"));
            }
        }
        return builder.build();
    }
}
