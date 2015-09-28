package com.telerik.plugins.mapbox;

import android.util.DisplayMetrics;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.mapbox.mapboxgl.annotations.MarkerOptions;
import com.mapbox.mapboxgl.geometry.LatLng;
import com.mapbox.mapboxgl.geometry.LatLngZoom;
import com.mapbox.mapboxgl.views.MapView;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONException;
import org.json.JSONObject;


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
  private float retinaFactor;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    DisplayMetrics metrics = new DisplayMetrics();
    cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
    retinaFactor = metrics.density;
  }

  @Override
  public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_SHOW.equals(action)) {
        final JSONObject options = args.getJSONObject(0);

        final String style = getStyle(options.optString("style"));

        final JSONObject margins = options.isNull("margins") ? null : options.getJSONObject("margins");
        final int left = margins == null || margins.isNull("left") ? 0 : margins.getInt("left");
        final int right = margins == null || margins.isNull("right") ? 0 : margins.getInt("right");
        final int top = margins == null || margins.isNull("top") ? 0 : margins.getInt("top");
        final int bottom = margins == null || margins.isNull("bottom") ? 0 : margins.getInt("bottom");

        final double zoomLevel = options.isNull("zoomLevel") ? 10 : options.getDouble("zoomLevel");

        final JSONObject center = options.isNull("center") ? null : options.getJSONObject("center");

        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {

            mapView = new MapView(webView.getContext(), "sk.eyJ1IjoiZWRkeXZlcmJydWdnZW4iLCJhIjoia1JpRW82NCJ9.OgnvpsKzB3GJhzyofQNUBw");

            // need to do this to register a receiver which onPause later needs
            mapView.onResume();

            mapView.onCreate(null);

            mapView.setCompassEnabled(true);
            mapView.setRotateEnabled(true);
            mapView.setScrollEnabled(true);
            mapView.setZoomEnabled(true);

            mapView.setZoomLevel(zoomLevel);

            if (center != null) {
              try {
                final double lat = center.getDouble("lat");
                final double lng = center.getDouble("lng");
                mapView.setCenterCoordinate(new LatLngZoom(lat, lng, zoomLevel));
              } catch (JSONException e) {
                callbackContext.error(e.getMessage());
                return;
              }
            } else {
              mapView.setZoomLevel(zoomLevel);
            }

            mapView.setStyleUrl("asset://styles/" + style + "-v8.json");

            // TODO show the user on the map based on a boolean, also for iOS
            if (true) {
              //          UserLocationOverlay myLocationOverlay = new UserLocationOverlay(this, mapView);
              //          userLocationOverlay.enableMyLocation();
              //          userLocationOverlay.setDrawAccuracyEnabled(true);
              //          mapView.getOverlays().add(myLocationOverlay);
            }

            // position the mapView overlay
            int webViewWidth = webView.getView().getWidth();
            int webViewHeight = webView.getView().getHeight();
            final FrameLayout layout = (FrameLayout) webView.getView().getParent();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(webViewWidth - left - right, webViewHeight - top - bottom);
            params.setMargins(left, top, right, bottom);
            mapView.setLayoutParams(params);

            layout.addView(mapView);
            callbackContext.success();
          }
        });

      } else if (ACTION_HIDE.equals(action)) {
        if (mapView != null) {
          cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              ViewGroup vg = (ViewGroup) mapView.getParent();
              vg.removeView(mapView);
              callbackContext.success();
            }
          });
        }

      } else if (ACTION_GET_ZOOMLEVEL.equals(action)) {
        if (mapView != null) {
          cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              final double zoomLevel = mapView.getZoomLevel();
              callbackContext.success("" + zoomLevel);
            }
          });
        }

      } else if (ACTION_SET_ZOOMLEVEL.equals(action)) {
        if (mapView != null) {
          cordova.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
              mapView.setZoomLevel(7, true);
              callbackContext.success();
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
            callbackContext.success();
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

            callbackContext.success();
          }
        });

      } else {
        return false;
      }
    } catch (Throwable t) {
      t.printStackTrace();
      callbackContext.error(t.getMessage());
    }
    return true;
  }

  private static String getStyle(final String requested) {
    if ("light".equalsIgnoreCase(requested)) {
      return "light";
    } else if ("dark".equalsIgnoreCase(requested)) {
      return "dark";
    } else if ("emerald".equalsIgnoreCase(requested)) {
      return "emerald";
    } else if ("satellite".equalsIgnoreCase(requested)) {
      return "satellite";
    } else {
      return "streets";
    }
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