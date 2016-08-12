package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.util.SparseArray;


import com.mapbox.mapboxsdk.maps.MapView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;

/**
 * Created by vikti on 24/06/2016.
 */
public class MapsManager {
    private static MapsManager mOurInstance = new MapsManager();
    private static SparseArray<Map> mMaps = new SparseArray();
    private static CDVMapbox mPlugRef;
    private static Activity mActivity;

    public static MapsManager getInstance() {
        return mOurInstance;
    }

    public static void init(final CDVMapbox plugin, Activity activity) {
        mPlugRef = plugin;
        mActivity = activity;
    }

    public static Map createMap(CordovaArgs args, int id, CallbackContext callbackContext){
        Map map = new Map(id, args, mPlugRef, mActivity, callbackContext);
        mMaps.put(id, map);
        return map;
    }

    public static Map getMap(int id){
        return mMaps.get(id);
    }

    public static int getCount(){
        return mMaps.size();
    }

    public static void removeMap(int mapId){
        mMaps.delete(mapId);
    }

    public static void onPause() {
        for( int i = 0; i < mMaps.size(); i++){
            mMaps.get(i).getMapCtrl().getMapView().onPause();
        }
    }

    public static void onResume() {
        for( int i = 0; i < mMaps.size(); i++){
            mMaps.get(i).getMapCtrl().getMapView().onResume();
        }
    }

    public static void onDestroy() {
        for( int i = 0; i < mMaps.size(); i++){
            mMaps.get(i).getMapCtrl().getMapView().onDestroy();
        }
    }
}