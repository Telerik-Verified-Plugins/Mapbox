package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.os.Handler;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.UiSettings;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vikti on 24/06/2016.
 */
public class Map {

    private int _id;
    private static Activity _activity;
    private CDVMapbox _plugRef;
    private MapboxMap _mapboxMap;
    private FrameLayout _layersGroup;
    private UiSettings _uiSettings;
    private JSONObject _mapDivLayoutJSON;
    //private RectF _mapRect;
    private MapboxController _mapCtrl;

    private JSONObject mapDivLayoutJSON = null;
    private static CordovaWebView _cdvWebView;
    private static float _density;

    public ViewGroup getViewGroup(){
        return _layersGroup;
    }
    public int getId(){return _id;}

    public CameraPosition cameraPosition;

    /**
     * Create a map without any layout set
     * @param args
     * @param plugRef
     * @param activity
     * @param callbackContext
     */
    public Map(int id, final CordovaArgs args, CDVMapbox plugRef, Activity activity, CallbackContext callbackContext) {

        _id = id;
        _plugRef = plugRef;
        _cdvWebView = _plugRef.webView;
        _activity = activity;
        Context _context = _cdvWebView.getView().getContext();
        _density = Resources.getSystem().getDisplayMetrics().density;

        /**
         * Create a controller (which instantiate the MGLMapbox view)
         */
        final JSONObject options;
        final JSONArray HTMLs;
        try {
            options = args.getJSONObject(1);

            // Set map overlay
            HTMLs = options.isNull("HTMLs") ? new JSONArray() : options.getJSONArray("HTMLs");
            _updateMapOverlay(HTMLs);

            // Create map
            if(options.isNull("rect")){
                callbackContext.error("Need a rect");
            }
/*            _mapDivLayoutJSON = options.getJSONObject("rect");
            _mapRect = _toRect(_mapDivLayoutJSON);*/
            _mapCtrl = new MapboxController(MapboxController.createMapboxMapOptions(options), _cdvWebView, _plugRef, callbackContext);

            // The view container. Contains maps and addons views.
            _layersGroup = new FrameLayout(_context);
            //_layersGroup.setLayoutParams(_toLayoutParams(_mapRect));
            _layersGroup.addView(_mapCtrl.getMapView());

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Update the Map Overlay Layer
    }

    //change to updateLauyoutWhenScroll
    private void _updateMapOverlay(JSONArray HTMLs){

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

    public void scrollTo(int x, int y){
        _plugRef.pluginLayout.setMapDrawingRect(
                _id,
                _toRect(_mapDivLayoutJSON, x, y)
        );
    }

    private void updateMapViewLayout() {
        if (_plugRef.pluginLayout == null) {
            return;
        }

        _plugRef.pluginLayout.setMapDrawingRect(
                _id,
                _toRect(_mapDivLayoutJSON, _cdvWebView.getView().getScrollX(), _cdvWebView.getView().getScrollY())
        );

        _plugRef.pluginLayout.updateViewPosition();

        _layersGroup.requestLayout(); //todo watch this line if nothing is resized

    }
//todo use setdiv in CDVMapbox
    /**
     * Resize the map to fit the dimensions of a div.
     * This function takes also care of update the overlay DOM elements touch boxes.
     * @param args Cordova Args. Contain a div JSONObject
     * @param callbackContext
     * @throws JSONException
     */
    public void setDiv(CordovaArgs args, CallbackContext callbackContext){

        try {
            JSONObject options = args.getJSONObject(1);

            if(options.isNull("rect")){
                callbackContext.error("Map.setDiv(... Need a rect");
            }

            // update the map size
            _mapDivLayoutJSON = options.getJSONObject("rect");
            //_mapRect = _toRect(_mapDivLayoutJSON);

            // update the map overlay DOM elements touch boxes
            JSONArray HTMLs = options.isNull("HTMLs") ? new JSONArray() : options.getJSONArray("HTMLs");
            _plugRef.pluginLayout.clearHTMLElement();
            _updateMapOverlay(HTMLs);

            // Finally, update the map view layout to take account of the new map dimension.
            updateMapViewLayout();

            this.sendNoResult(callbackContext);
        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    private float _contentToView(long d) {
        return d * _density;
    }

    public void hide(final CordovaArgs args, final CallbackContext callbackContext) {
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

    private float contentToView(long d) {
        return d * _density;
    }


    private RectF _toRect(JSONObject rect, float... scroll) {
        float scrollX = scroll.length > 0 ? scroll[0] : 0;
        float scrollY = scroll.length > 1 ? scroll[1] : 0;

        try{
            float left = _contentToView(rect.getLong("left"));
            float width = _contentToView(rect.getLong("width"));
            float top = _contentToView(rect.getLong("top"));
            float height = _contentToView(rect.getLong("height"));

            return new RectF(
                left + scrollX,
                top - scrollY,
                left + width + scrollX,
                top + height - scrollY);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    //todo take in account all type of layer
    private FrameLayout.LayoutParams _toLayoutParams(RectF rect) {
        int screenW = Math.round(_cdvWebView.getView().getWidth());
        int screenH = Math.round(_cdvWebView.getView().getHeight());
        int left = Math.round(rect.left);
        int right = Math.round(rect.right);
        int top = Math.round(rect.top);
        int bottom = Math.round(rect.bottom);
        int width = Math.round(screenW - left - (screenW - right));
        int height = Math.round(screenH - top - (screenH - bottom));

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);

        params.setMargins(left, top, width - right, height - bottom);
        //params.setMargins(left, top, 21, 0);

        return params;
    }

    protected void sendNoResult(CallbackContext callbackContext) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }
}