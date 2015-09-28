package com.telerik.plugins.mapbox;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mapbox.mapboxgl.annotations.MarkerOptions;
import com.mapbox.mapboxgl.geometry.LatLng;
import com.mapbox.mapboxgl.views.MapView;

import org.apache.cordova.*;
import org.json.JSONException;
import org.json.JSONObject;

import android.opengl.GLSurfaceView;

import io.cordova.hellocordova.MainActivity;
import io.cordova.hellocordova.R;

import static com.mapbox.mapboxgl.views.MapView.*;


// TODO not using this because the current release is too unstable and requires newer devices
// .. perhaps fall back to old impl for older devices?


// TODO for screen rotation, see https://www.mapbox.com/mapbox-android-sdk/#screen-rotation
// TODO fox Xwalk compat, see nativepagetransitions plugin

// TODO look at demo app: https://github.com/mapbox/mapbox-gl-native/blob/master/android/java/MapboxGLAndroidSDKTestApp/src/main/java/com/mapbox/mapboxgl/testapp/MainActivity.java
public class Mapboxgl extends CordovaPlugin {

  private static final String ACTION_SHOW = "show";
  private static final String ACTION_HIDE = "hide";
  private static final String ACTION_ADD_POLYGON = "addPolygon";
  private static final String ACTION_ADD_MARKERS = "addMarkers";
  private static final String ACTION_GET_ZOOMLEVEL = "getZoomLevel";
  private static final String ACTION_SET_ZOOMLEVEL = "setZoomLevel";

  public static MapView mapView;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
  }

  public void onPause(boolean multitasking) {
    mapView.onPause();
  }

  public void onResume(boolean multitasking) {
    mapView.onResume();
  }

  public void onDestroy() {
    mapView.onDestroy();
  }

  // TODO impl onDestroy(), onPause(), onResume()
  @Override
  public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_SHOW.equals(action)) {
        final JSONObject options = args.getJSONObject(0);

        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {

            mapView = new MapView(webView.getContext(), "sk.eyJ1IjoiZWRkeXZlcmJydWdnZW4iLCJhIjoia1JpRW82NCJ9.OgnvpsKzB3GJhzyofQNUBw");

            //mapView.getContext().registerReceiver(mapView.new ConnectivityReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

            mapView.onResume();

//            mapView.onCreate(((MainActivity)cordova.getActivity()).state);
            mapView.onCreate(null);

            mapView.setCompassEnabled(true);
            mapView.setRotateEnabled(true);
            mapView.setScrollEnabled(true);
            mapView.setZoomEnabled(true);

//            cordova.getActivity().setContentView(R.layout.activity_main);
            // TODO get from strings.xml via plugin.xml
            //mapView = new MapView(webView.getContext(), "sk.eyJ1IjoiZWRkeXZlcmJydWdnZW4iLCJhIjoia1JpRW82NCJ9.OgnvpsKzB3GJhzyofQNUBw");

//            mapView.onCreate();
//            mapView = (MapView) findViewById(R.id.mainMapView());

//            mapView.setZoomLevel(5.0);
//            mapView.setCenterCoordinate(new LatLng(55.94629, -3.20777));

            // supported styles: https://github.com/mapbox/mapbox-gl-native/issues/1264
//            mapView.setStyleUrl("asset://styles/dark-v8.json");
            mapView.setStyleUrl("asset://styles/light-v8.json");

            // TODO show the user on the map based on a boolean, also for iOS
            if (true) {
              //          UserLocationOverlay myLocationOverlay = new UserLocationOverlay(this, mapView);
              //          userLocationOverlay.enableMyLocation();
              //          userLocationOverlay.setDrawAccuracyEnabled(true);
              //          mapView.getOverlays().add(myLocationOverlay);
            }

            // TODO pass in width stuff like  json.getInt("left");
            int webViewWidth = webView.getView().getWidth();
            int webViewHeight = webView.getView().getHeight();

            int left = 100;
            int right = 100;
            int top = 0;
            int bottom = 400; // todo correct this.. see nativepagetr?
            final FrameLayout layout = (FrameLayout) webView.getView().getParent();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(webViewWidth - left - right, webViewHeight - top - bottom);
            params.setMargins(left, top, right, bottom);
            mapView.setLayoutParams(params);
            layout.addView(mapView);
            callbackContext.success("OK");
          }
        });

      } else if (ACTION_HIDE.equals(action)) {
        if (mapView != null) {
          cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ViewGroup vg = (ViewGroup) mapView.getParent();
              vg.removeView(mapView);
              callbackContext.success("OK");
            }
          });
        }

      } else if (ACTION_ADD_POLYGON.equals(action)) {
        final JSONObject options = args.getJSONObject(0);
        final String url = options.optString("url");
        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
//            mapView.loadFromGeoJSONURL(url);
            callbackContext.success("OK");
          }
        });

      } else if (ACTION_ADD_MARKERS.equals(action)) {
        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
//            Context ctx = webView.getContext();
//            Marker m = new Marker(); //mapView, "Edinburgh", "Scotland", new LatLng(55.94629, -3.20777));
//            m.setMapView(mapView);
//            m.setVisible(true);
            MarkerOptions mo = new MarkerOptions();
            mo.title("Title");
            mo.position(new LatLng(59.32995, 18.06461));
            mapView.addMarker(mo);

            callbackContext.success("OK");
          }
        });

      } else {
        return false;
      }
    } catch (Throwable t) {
      t.printStackTrace();
      callbackContext.error("ERROR: " + t.getMessage());
    }
    return true;
  }
}

/*
    // other features:

    public LatLng getMapCenter() {
        return mv.getCenter();
    }

    public void setMapCenter(ILatLng center) {
        mv.setCenter(center);
    }
*/