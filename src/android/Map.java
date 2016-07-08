package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.RectF;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by vikti on 24/06/2016.
 *
 * This file handle a single Map.
 * He has a visual responsibility only: hide, show, resize...
 * All map actions are handled by the controller and called from CDVMapbox.java to ensure
 * a decoupling and easily switch to GoogleMap or whatever in the futur.
 */
public class Map {

    private int _id;
    private CDVMapbox _plugRef;
    private FrameLayout _layersGroup;
    private JSONObject _mapDivLayoutJSON;
    private MapController _mapCtrl;

    private static CordovaWebView _cdvWebView;
    private static float _retinaFactor;

    public MapController getMapCtrl(){
        return _mapCtrl;
    }

    public ViewGroup getViewGroup(){
        return _layersGroup;
    }
    public int getId(){return _id;}

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
        Context _context = _cdvWebView.getView().getContext();
        _retinaFactor = Resources.getSystem().getDisplayMetrics().density;

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

            _mapCtrl = new MapController(options, activity, callbackContext);

            // The view container. Contains maps and addons views.
            _layersGroup = new FrameLayout(_context);
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

                divW = _applyRetinaFactor(elemSize.getLong("width"));
                divH = _applyRetinaFactor(elemSize.getLong("height"));
                divLeft = _applyRetinaFactor(elemSize.getLong("left"));
                divTop = _applyRetinaFactor(elemSize.getLong("top"));
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

    private float _applyRetinaFactor(long d) {
        return d * _retinaFactor;
    }


    private RectF _toRect(JSONObject rect, float... scroll) {
        float scrollX = scroll.length > 0 ? scroll[0] : 0;
        float scrollY = scroll.length > 1 ? scroll[1] : 0;

        try{
            float left = _applyRetinaFactor(rect.getLong("left"));
            float width = _applyRetinaFactor(rect.getLong("width"));
            float top = _applyRetinaFactor(rect.getLong("top"));
            float height = _applyRetinaFactor(rect.getLong("height"));

            return new RectF(
                left - scrollX,
                top - scrollY,
                left + width - scrollX,
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

        return params;
    }

    protected void sendNoResult(CallbackContext callbackContext) {
        PluginResult pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);
        callbackContext.sendPluginResult(pluginResult);
    }
}