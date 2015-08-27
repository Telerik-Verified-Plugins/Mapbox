package com.telerik.plugins.mapbox;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.mapbox.mapboxsdk.overlay.Icon;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MapboxTileLayer;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONException;
import org.json.JSONObject;

// TODO for screen rotation, see https://www.mapbox.com/mapbox-android-sdk/#screen-rotation
// TODO fox Xwalk compat, see nativepagetransitions plugin
public class Mapbox extends CordovaPlugin {

  private static final String ACTION_SHOW = "show";
  private static final String ACTION_HIDE = "hide";
  private static final String ACTION_ADD_GEOJSON = "addGeoJson";
  private static final String ACTION_ADD_ANNOTATIONS = "addAnnotations";

  private MapView mapView;

  @Override
  public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    try {
      if (ACTION_SHOW.equals(action)) {
        final JSONObject options = args.getJSONObject(0);

        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {

            mapView = new MapView(webView.getContext());
            mapView.setZoom(5);
            mapView.setCenter(new LatLng(55.94629, -3.20777));


            // TODO get from strings.xml via plugin.xml
            mapView.setAccessToken("sk.eyJ1IjoiZWRkeXZlcmJydWdnZW4iLCJhIjoia1JpRW82NCJ9.OgnvpsKzB3GJhzyofQNUBw");

            // streets | outdoors | satellite | run-bike-hike | pencil
            mapView.setTileSource(new MapboxTileLayer("mapbox.run-bike-hike"));

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

            int left = 0;
            int right = 0;
            int top = 0;
            int bottom = 240;
            final FrameLayout layout = (FrameLayout) webView.getView().getParent();
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(webViewWidth-left-right, webViewHeight-top-bottom);
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

      } else if (ACTION_ADD_GEOJSON.equals(action)) {
        final JSONObject options = args.getJSONObject(0);
        final String url = options.optString("url");
        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mapView.loadFromGeoJSONURL(url);
            callbackContext.success("OK");
          }
        });

      } else if (ACTION_ADD_ANNOTATIONS.equals(action)) {
        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Context ctx = webView.getContext();
            Marker m = new Marker(mapView, "Edinburgh", "Scotland", new LatLng(55.94629, -3.20777));
            m.setIcon(new Icon(ctx, Icon.Size.SMALL, "marker-stroked", "ee8a65"));
            mapView.addMarker(m);

            m = new Marker(mapView, "Stockholm", "Sweden", new LatLng(59.32995, 18.06461));
            m.setIcon(new Icon(ctx, Icon.Size.LARGE, "city", "3887be"));
            mapView.addMarker(m);

            m = new Marker(mapView, "Prague", "Czech Republic", new LatLng(50.08734, 14.42112));
            m.setIcon(new Icon(ctx, Icon.Size.MEDIUM, "land-use", "3bb2d0"));
            mapView.addMarker(m);

            m = new Marker(mapView, "Athens", "Greece", new LatLng(37.97885, 23.71399));
            m.setIcon(new Icon(ctx, Icon.Size.LARGE, "land-use", "3887be"));
            mapView.addMarker(m);

            m = new Marker(mapView, "Tokyo", "Japan", new LatLng(35.70247, 139.71588));
            m.setIcon(new Icon(ctx, Icon.Size.LARGE, "city", "3887be"));
            mapView.addMarker(m);

            m = new Marker(mapView, "Ayacucho", "Peru", new LatLng(-13.16658, -74.21608));
            m.setIcon(new Icon(ctx, Icon.Size.LARGE, "city", "3887be"));
            mapView.addMarker(m);

            m = new Marker(mapView, "Nairobi", "Kenya", new LatLng(-1.26676, 36.83372));
            m.setIcon(new Icon(ctx, Icon.Size.LARGE, "city", "3887be"));
            mapView.addMarker(m);

            m = new Marker(mapView, "Canberra", "Australia", new LatLng(-35.30952, 149.12430));
            m.setIcon(new Icon(ctx, Icon.Size.LARGE, "city", "3887be"));
            mapView.addMarker(m);

            mapView.loadFromGeoJSONURL("https://gist.githubusercontent.com/tmcw/10307131/raw/21c0a20312a2833afeee3b46028c3ed0e9756d4c/map.geojson");

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