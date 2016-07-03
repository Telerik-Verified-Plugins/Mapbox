package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.UiSettings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vikti on 24/06/2016.
 */
public class Map {

    private static Activity _activity;
    private CDVMapbox _plugRef;
    private MapboxMap _mapboxMap;
    private FrameLayout _layersGroup;
    private UiSettings _uiSettings;
    private FrameLayout.LayoutParams _mapFrame;
    private MapboxController _mapCtrl;

    private static CordovaWebView _cdvWebView;
    private static float _density;

    public CameraPosition cameraPosition;

    public Map(final CordovaArgs args, CDVMapbox plugRef, Activity activity, CallbackContext callbackContext) {

        _plugRef = plugRef;
        _cdvWebView = _plugRef.webView;
        _activity = activity;
        Context _context = _cdvWebView.getView().getContext();
        _density = Resources.getSystem().getDisplayMetrics().density;

        try {
            JSONObject options = args.getJSONObject(1);
            JSONObject margins = options.isNull("margins") ? null : options.getJSONObject("margins");
            _mapFrame = _getFrameWithDictionary(margins);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        /* The view container
         */
        _layersGroup = new FrameLayout(_context);
        _layersGroup.setLayoutParams(
            new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
        );

        /**
         * Create a controller (which instantiate the MGLMapbox view)
         */
        final JSONObject options;
        final JSONArray HTMLs;
        try {
            options = args.getJSONObject(1);
            HTMLs = options.isNull("HTMLs") ? new JSONArray() : options.getJSONArray("HTMLs");
            JSONObject margins = options.isNull("margins") ? null : options.getJSONObject("margins");

            _mapFrame = _getFrameWithDictionary(margins);
            _mapCtrl = new MapboxController(MapboxController.createMapboxMapOptions(options), _cdvWebView, _mapFrame, _plugRef, callbackContext);
            _layersGroup.addView(_mapCtrl.getMapView());

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Update the Map Overlay Layer
        _updatePluginLayerLayout(HTMLs);
    }

    private void _updatePluginLayerLayout(JSONArray HTMLs){

        _plugRef.pluginLayout.mapView = _mapCtrl.getMapView();
        _plugRef.pluginLayout.mapFrame = _mapFrame;
        //_cdvMapbox.pluginScrollView.debugView.mapFrame = _mapFrame;

        JSONObject elemInfo, elemSize;
        String elemId;
        float divW, divH, divLeft, divTop;

        for (int i = 0; i < HTMLs.length(); i++) {
            try {
                elemInfo = HTMLs.getJSONObject(i);
                elemId = elemInfo.getString("id");
                elemSize = elemInfo.getJSONObject("size");

                divW = _contentToView(elemSize.getLong("width"));
                divH = _contentToView(elemSize.getLong("height"));
                divLeft = _contentToView(elemSize.getLong("left"));
                divTop = _contentToView(elemSize.getLong("top"));
                _plugRef.pluginLayout.setHTMLElement(elemId, divLeft, divTop, divLeft + divW, divTop + divH);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private float _contentToView(long d) {
        return d * _density;
    }

    public void show(final CordovaArgs args, final CallbackContext callbackContext) {
        _plugRef.mapsLayout.addView(_layersGroup);
        callbackContext.success();
    }

    public void hide(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void refreshMap(final CordovaArgs args, final CallbackContext callbackContext) {
        JSONObject options;
        JSONObject margins;
        JSONArray HTMLs;
        try {
            options = args.getJSONObject(1);
            HTMLs = options.isNull("HTMLs") ? new JSONArray() : options.getJSONArray("HTMLs");
            margins = options.isNull("margins") ? null : options.getJSONObject("margins");
            _mapFrame = _getFrameWithDictionary(margins);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        _mapFrame = _getFrameWithDictionary(margins);

        // update the touchable zone in the plugin layer
        _updatePluginLayerLayout(HTMLs);

        // resize the map view
        _mapCtrl.mapFrame = _mapFrame;

        callbackContext.success();
    }
    public void getCenterCoordinates(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void setCenterCoordinates(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void setZoomLevel(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void getZoomLevel(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void getBoundsCoordinates(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void setTilt(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void getTilt(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void onRegionWillChange(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void onRegionIsChanging(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void onRegionDidChange(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void animateCamera(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void addPolygon(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void addGeoJSON(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void addMarkers(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void addMarkerCallback(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void convertCoordinates(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    public void convertPoint(final CordovaArgs args, final CallbackContext callbackContext) {
    }

    /**
     * Set the touchable frame of the plugin layer and the controller map view from the input margins
     */
    private static FrameLayout.LayoutParams _getFrameWithDictionary(JSONObject margins) {
        try{
            int left = margins.getInt("left");
            int right = margins.getInt("right");
            int top = margins.getInt("top");
            int bottom = margins.getInt("bottom");

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                _cdvWebView.getView().getWidth()- left - right,
                _cdvWebView.getView().getHeight()- top - bottom
            );

            params.setMargins(left, top, right, bottom);

            return params;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}


