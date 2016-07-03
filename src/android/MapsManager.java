package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.graphics.Color;
import android.util.SparseArray;
import android.view.View;
import android.widget.FrameLayout;


import com.mapbox.mapboxsdk.maps.MapView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaWebView;

/**
 * Created by vikti on 24/06/2016.
 */
public class MapsManager {
    private static CordovaWebView _cdvWebView;
    private static MapsManager _ourInstance = new MapsManager();
    private static SparseArray<Map> _maps = new SparseArray<>();
    private static CDVMapbox _plugRef;
    private static Activity _activity;
    private static CordovaWebView _temp;

    public static MapsManager getInstance() {
        return _ourInstance;
    }

    public static void init(final CDVMapbox plugin, Activity activity, CordovaWebView cdvWebView) {
        _plugRef = plugin;
        _activity = activity;
        _cdvWebView = cdvWebView;
        _temp = _plugRef.webView;
    }

    public static Map createMap(CordovaArgs args, int id, CallbackContext callbackContext){
        Map map = new Map(args, _plugRef, _activity, callbackContext);
        _maps.setValueAt(id, map);
        return map;
    }

    public static Map getMap(int id){
        return _maps.get(id);
    }

    public static int getCount(){
        return _maps.size();
    }

    public static void removeMap(int mapId){

    }

    public static void onPause() {

    }

    public static void onResume() {

    }

    public static void onDestroy() {

    }
}