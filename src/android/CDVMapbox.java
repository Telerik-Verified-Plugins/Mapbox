package com.telerik.plugins.mapbox;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CDVMapbox extends CordovaPlugin implements ViewTreeObserver.OnScrollChangedListener {
  private static final String TAG = CDVMapbox.class.getSimpleName();

  public FrameLayout mapsGroup;

  public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
  public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
  public static final int LOCATION_REQ_CODE = 0;
  public static final int PERMISSION_DENIED_ERROR = 20;

  private static final String MAPBOX_ACCESSTOKEN_RESOURCE_KEY = "mapbox_accesstoken";
  private static final String ACTION_SHOW = "show";
  private static final String ACTION_HIDE = "hide";
  private static final String ACTION_RESIZE = "resize";
  private static final String ACTION_SET_CLICKABLE = "setClickable";
  private static final String ACTION_ADD_MARKERS = "addMarkers";
  private static final String ACTION_ADD_MARKER_CALLBACK = "addMarkerCallback";
  private static final String ACTION_ADD_POLYGON = "addPolygon";
  private static final String ACTION_ADD_GEOJSON = "addGeoJSON";
  private static final String ACTION_GET_ZOOMLEVEL = "getZoomLevel";
  private static final String ACTION_SET_ZOOMLEVEL = "setZoomLevel";
  private static final String ACTION_GET_CENTER = "getCenter";
  private static final String ACTION_SET_CENTER = "setCenter";
  private static final String ACTION_GET_TILT = "getTilt";
  private static final String ACTION_SET_TILT = "setTilt";
  private static final String ACTION_ANIMATE_CAMERA = "animateCamera";
  private static final String ACTION_CONVERT_COORDINATES = "convertCoordinates";
  private static final String ACTION_CONVERT_POINT = "convertPoint";
  private static final String ACTION_ON_REGION_WILL_CHANGE = "onRegionWillChange";
  private static final String ACTION_ON_REGION_IS_CHANGING = "onRegionIsChanging";
  private static final String ACTION_ON_REGION_DID_CHANGE = "onRegionDidChange";
  private static ScrollView _pluginScrollView;
  private static FrameLayout _scrollFrameLayout;
  private float _density;
  private String _accessToken;
  private CordovaWebView _webView;
  private Activity _activity;
  private CallbackContext _callback;
  private CallbackContext _markerCallbackContext;
  private boolean _showUserLocation;
  private ViewGroup _root;
  private View _background;
  private JSONObject _mapDivLayoutJSON;

  //todo webView or _webView ?

  public CordovaInterface _cordova;
  public PluginLayout pluginLayout;

  @Override
  public void initialize(CordovaInterface cordova, final CordovaWebView webView) {
    super.initialize(cordova, webView);

    _cordova = cordova;
    _webView = webView;
    _root = (ViewGroup) _webView.getView().getParent();
    _activity = _cordova.getActivity();
    _density = Resources.getSystem().getDisplayMetrics().density;
    _webView.getView().getViewTreeObserver().addOnScrollChangedListener(CDVMapbox.this);

    /**
     * Init MapsManager. It handles multiple maps.
     */
    MapsManager.init(this, _activity);

     /*
      * Init the plugin layer responsible to capture touch events.
      * It permits to have Dom Elements on top of the map.
      * If a touch event occurs in one of the embed rectangles and outside of a inner html element,
      * the plugin layer considers that is a map action (drag, pan, etc.).
      * If not, the user surely want to access the UIWebView.
      */
    pluginLayout = new PluginLayout(_webView.getView(), _activity);


    /**
     * Create the maps container.
     */
    mapsGroup = new FrameLayout(webView.getContext());
    mapsGroup.setLayoutParams(
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            )
    );

    /**
     * Arrange the layers. The final order is:
     * - root (Application View)
     *   - pluginLayout
     *     - frontLayout
     *       - webView
     *     - scrollView
     *       - scrollFrameLayout
     *         - mapsGroup
     *         - background
     */
    _activity.runOnUiThread(new Runnable() {
      public void run(){
        pluginLayout.attachMapsGroup(mapsGroup);
      }
    });

    // make webview transparent to see the map through
    //_root.setBackgroundColor(Color.WHITE);
    //webView.getView().setBackgroundColor(Color.TRANSPARENT);

    try {
      int mapboxAccesstokenResourceId = cordova.getActivity().getResources().getIdentifier(MAPBOX_ACCESSTOKEN_RESOURCE_KEY, "string", cordova.getActivity().getPackageName());
      _accessToken = cordova.getActivity().getString(mapboxAccesstokenResourceId);
      MapboxAccountManager.start(webView.getContext(), _accessToken);
    } catch (Resources.NotFoundException e) {
      // we'll deal with this when the _accessToken property is read, but for now let's dump the error:
      e.printStackTrace();
    }
  }

  @Override
  /**
   * Handler listening to scroll changes.
   * Important! Both pluginLayout and maps have to be updated.
   */
  public void onScrollChanged() {
    if (pluginLayout == null) {
      return;
    }
    int scrollX = _webView.getView().getScrollX();
    int scrollY = _webView.getView().getScrollY();

    pluginLayout.scrollTo(scrollX, scrollY);

    for(int i = 0; i < MapsManager.getCount(); i++){
      MapsManager.getMap(i).scrollTo(scrollX, scrollY);
    }
  }

  public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {

    _callback = callbackContext;

    try {
      final int id = args.getInt(0);
      final Map map = MapsManager.getMap(id);
      final MapController mapCtrl = map.getMapCtrl();

      if (ACTION_SHOW.equals(action)) {

        _activity.runOnUiThread(new Runnable() {
          public void run() {

           final Map aMap = map == null ? MapsManager.createMap(args, id, callbackContext) : map;

            exec(new Runnable() {
              @Override
              public void run() {
                aMap.setDiv(args, callbackContext);
                mapsGroup.addView(aMap.getViewGroup());
                //pluginLayout.addMap(newMap);
                callbackContext.success();
              }
            });
          }
        });
      } else if (ACTION_HIDE.equals(action)) {
        MapsManager.removeMap(id);
      } else if (ACTION_RESIZE.equals(action)){
        exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            aMap.setDiv(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });

      } else if (ACTION_GET_ZOOMLEVEL.equals(action)) {
        exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.getZoomLevel(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });

      } else if (ACTION_SET_ZOOMLEVEL.equals(action)) {
        exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.setZoomLevel(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });

      } else if (ACTION_GET_CENTER.equals(action)) {
         exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.getCenterCoordinates(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });


      } else if (ACTION_SET_CENTER.equals(action)) {
         exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.setCenterCoordinates(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });


      } else if (ACTION_GET_TILT.equals(action)) {
         exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.getTilt(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });


      } else if (ACTION_ANIMATE_CAMERA.equals(action)) {
         exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.animateCamera(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });


      } else if (ACTION_ADD_GEOJSON.equals(action)) {
        exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.addGeoJSON(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });

      } else if (ACTION_ADD_MARKERS.equals(action)) {
        exec(new Runnable() {
          Map aMap;

          @Override
          public void run() {
            mapCtrl.addMarkers(args, callbackContext);
          }

          public Runnable init(Map aMap) {
            this.aMap = aMap;
            return (this);
          }
        });

      return false;
      }
    } catch (Throwable t) {
      t.printStackTrace();
      callbackContext.error(t.getMessage());
    }
    return true;
  }

  private void exec(Runnable _callback){
    _activity.runOnUiThread(_callback);
  }


  private void addMarkers(JSONArray markers) throws JSONException {
    for (int i=0; i<markers.length(); i++) {
      final JSONObject marker = markers.getJSONObject(i);
      final MarkerOptions mo = new MarkerOptions();
      mo.title(marker.isNull("title") ? null : marker.getString("title"));
      mo.snippet(marker.isNull("subtitle") ? null : marker.getString("subtitle"));
      mo.position(new LatLng(marker.getDouble("lat"), marker.getDouble("lng")));
    }
  }

  private class MarkerClickListener implements MapboxMap.OnMarkerClickListener {


    public boolean onMarkerClick(@NonNull Marker marker) {
      // _callback
      if (_markerCallbackContext != null) {
        final JSONObject json = new JSONObject();
        try {
          json.put("title", marker.getTitle());
          json.put("subtitle", marker.getSnippet());
          json.put("lat", marker.getPosition().getLatitude());
          json.put("lng", marker.getPosition().getLongitude());
        } catch (JSONException e) {
          PluginResult pluginResult = new PluginResult(PluginResult.Status.ERROR,
              "Error in _callback of " + ACTION_ADD_MARKER_CALLBACK + ": " + e.getMessage());
          pluginResult.setKeepCallback(true);
          _markerCallbackContext.sendPluginResult(pluginResult);
        }
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
        pluginResult.setKeepCallback(true);
        _markerCallbackContext.sendPluginResult(pluginResult);
        return true;
      }
      return false;
    }
  }

  private boolean permissionGranted(String... types) {
    if (Build.VERSION.SDK_INT < 23) {
      return true;
    }
    for (final String type : types) {
      if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this.cordova.getActivity(), type)) {
        return false;
      }
    }
    return true;
  }

  protected void _showUserLocation() {

  }


  private void requestPermission(String... types) {
    ActivityCompat.requestPermissions(
        this.cordova.getActivity(),
        types,
        LOCATION_REQ_CODE);
  }

  // TODO
  public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {
    for (int r : grantResults) {
      if (r == PackageManager.PERMISSION_DENIED) {
        _callback.sendPluginResult(new PluginResult(PluginResult.Status.ERROR, PERMISSION_DENIED_ERROR));
        return;
      }
    }
    switch (requestCode) {
      case LOCATION_REQ_CODE:
        _showUserLocation();
        break;
    }
  }

  private float contentToView(long d) {
    return d * _density;
  }

  public void onPause(boolean multitasking) {
    MapsManager.onPause();
  }

  public void onResume(boolean multitasking) {
    MapsManager.onResume();
  }

  public void onDestroy() {
    MapsManager.onDestroy();
  }
}
