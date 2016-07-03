package com.telerik.plugins.mapbox;

import android.graphics.Color;
import android.graphics.RectF;
import android.telecom.Call;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

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
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vikti on 25/06/2016.
 */
public class MapboxController {
    public MapboxMap mapboxMap;
    public FrameLayout.LayoutParams mapFrame;

    private static float retinaFactor;

    private MapView _mapView;
    private MapboxMapOptions _initOptions;
    private MapboxMap _mapboxMap;
    private UiSettings _uiSettings;
    private CameraPosition _cameraPosition;
    private CordovaWebView _cdvWebView;

    public View getMapView(){
        return (View) _mapView;
    }

    public MapboxController(final MapboxMapOptions options, CordovaWebView cdvWebView, final FrameLayout.LayoutParams mapFrame, CDVMapbox plugRef, final CallbackContext callbackContext){

        _initOptions = options;
        this.mapFrame = mapFrame;
        _cdvWebView = cdvWebView;

        _mapView = new MapView(_cdvWebView.getContext(), options);
        _mapView.setLayoutParams(mapFrame);


        // need to do this to register a receiver which onPause later needs
        _mapView.onResume();
        _mapView.onCreate(null);

        _mapView.getMapAsync(new OnMapReadyCallback() {

            public void onMapReady(MapboxMap map) {

   /*             _mapboxMap = map;
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

    public void setZoom(float zoom){
        CameraPosition position = new CameraPosition.Builder()
                .zoom(zoom) // Sets the zoom
                .build(); // Creates a CameraPosition from the builder

        mapboxMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(position));

    }

    public void setCenter(LatLng coords) throws JSONException {
        mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(coords)
                        .build()
        ));
    }

    private static int applyRetinaFactor(int i) {
        return (int) (i * retinaFactor);
    }


    public static MapboxMapOptions createMapboxMapOptions(JSONObject options) throws JSONException {
        MapboxMapOptions opts = new MapboxMapOptions();
        opts.styleUrl(_getStyle(options.getString("style")));
        opts.attributionEnabled(options.isNull("hideAttribution") || !options.getBoolean("hideAttribution"));
        opts.logoEnabled(options.isNull("hideLogo") || options.getBoolean("hideLogo"));
        opts.locationEnabled(!options.isNull("showUserLocation") && options.getBoolean("showUserLocation"));
        //opts.camera(Map.getCameraPostion(options, null));
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

}
