package com.telerik.plugins.mapbox;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.offline.OfflineManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

// TODO for screen rotation, see https://www.mapbox.com/mapbox-android-sdk/#screen-rotation
// TODO fox Xwalk compat, see nativepagetransitions plugin
// TODO look at demo app: https://github.com/mapbox/mapbox-gl-native/blob/master/android/java/MapboxGLAndroidSDKTestApp/src/main/java/com/mapbox/mapboxgl/testapp/MainActivity.java
public class Mapbox extends CordovaPlugin {

  public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
  public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

  public static final int PERMISSION_DENIED_ERROR = 20;

  private static final String MAPBOX_ACCESSTOKEN_RESOURCE_KEY = "mapbox_accesstoken";

  private static final String ACTION_CREATE = "create";
  private static final String ACTION_JUMP_TO = "jumpTo";
  private static final String ACTION_SHOW_USER_LOCATION = "showUserLocation";
  private static final String ACTION_CREATE_OFFLINE_REGION = "createOfflineRegion";
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

  private static float retinaFactor;
  private String accessToken;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    this.retinaFactor = this.getRetinaFactor();
    this.accessToken = this.getAccessToken();
  }

  @Override
  public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
    Command command = Command.create(action, args, callbackContext);
    return execute(command);
  }

  public boolean execute(Command command) throws JSONException {
    final String action = command.getAction();
    final CordovaArgs args = command.getArgs();
    final CallbackContext callbackContext = command.getCallbackContext();

    if (ACTION_CREATE.equals(action)) {
      final JSONObject options = args.getJSONObject(0);
      boolean showUserLocation = !options.isNull("showUserLocation") && options.getBoolean("showUserLocation");
      if (!showUserLocation || requestPermission(command, COARSE_LOCATION, FINE_LOCATION)) {
        this.create(options, callbackContext);
      }
    }

    else if (ACTION_SHOW_USER_LOCATION.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      final boolean enabled = args.getBoolean(1);
      if (requestPermission(command, COARSE_LOCATION, FINE_LOCATION)) {
        cordova.getActivity().runOnUiThread(new Runnable() {
          @Override
          public void run() {
            map.showUserLocation(enabled);
          }
        });
      }
    }

    else if (ACTION_JUMP_TO.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      final JSONObject options = args.getJSONObject(1);

      cordova.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          try {
            map.jumpTo(options);
            callbackContext.success();
          } catch (JSONException e) {
            callbackContext.error(e.getMessage());
          }
        }
      });
    }

    else if (ACTION_GET_CENTER.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      try {
        callbackContext.success(map.getCenter());
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    }

    else if (ACTION_SET_CENTER.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      final JSONArray center = args.getJSONArray(1);
      try {
        map.setCenter(center);
        callbackContext.success();
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    }

    else if (ACTION_GET_ZOOMLEVEL.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      callbackContext.success("" + map.getZoom());
    }

    else if (ACTION_SET_ZOOMLEVEL.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      final double zoom = args.getDouble(1);
      map.setZoom(zoom);
      callbackContext.success();
    }

    else if (ACTION_ADD_MARKERS.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      try {
        map.addMarkers(args.getJSONArray(1));
        callbackContext.success();
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    }

    else if (ACTION_ADD_MARKER_CALLBACK.equals(action)) {
      final int mapId = args.getInt(0);
      final MapInstance map = MapInstance.getMap(mapId);
      map.addMarkerListener(
        new MapboxMap.OnInfoWindowClickListener() {
          @Override
          public boolean onInfoWindowClick(Marker marker) {
            try {
              callbackContext.success(
                new JSONObject()
                        .put("title", marker.getTitle())
                        .put("subtitle", marker.getSnippet())
                        .put("lat", marker.getPosition().getLatitude())
                        .put("lng", marker.getPosition().getLongitude())
              );
              return true;
            } catch (JSONException e) {
              String message = "Error in callback of " + ACTION_ADD_MARKER_CALLBACK + ": " + e.getMessage();
              callbackContext.error(message);
              return false;
            }
          }
        }
      );
    }
    else if (ACTION_CREATE_OFFLINE_REGION.equals(action)) {
      final int mapId = args.getInt(0);
      final JSONObject options = args.getJSONObject(1);
      final MapInstance map = MapInstance.getMap(mapId);
    }
    else if (ACTION_GET_TILT.equals(action)) {
//        if (mapView != null) {
//          cordova.getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//              final double tilt = mapView.getTilt();
//              callbackContext.success("" + tilt);
//            }
//          });
//        }

    } else if (ACTION_SET_TILT.equals(action)) {
//        if (mapView != null) {
//          cordova.getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//              try {
//                final JSONObject options = args.getJSONObject(0);
//                mapView.setTilt(
//                    options.optDouble("pitch", 20),      // default 20
//                    options.optLong("duration", 5000)); // default 5s
//                callbackContext.success();
//              } catch (JSONException e) {
//                callbackContext.error(e.getMessage());
//              }
//            }
//          });
//        }

    } else if (ACTION_ANIMATE_CAMERA.equals(action)) {
//        if (mapView != null) {
//          cordova.getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//              try {
//                // TODO check mandatory elements
//                final JSONObject options = args.getJSONObject(0);
//
//                final JSONObject target = options.getJSONObject("target");
//                final double lat = target.getDouble("lat");
//                final double lng = target.getDouble("lng");
//
//                final CameraPosition.Builder builder =
//                    new CameraPosition.Builder()
//                        .target(new LatLng(lat, lng));
//
//                if (options.has("bearing")) {
//                  builder.bearing(((Double)options.getDouble("bearing")).floatValue());
//                }
//                if (options.has("tilt")) {
//                  builder.tilt(((Double)options.getDouble("tilt")).floatValue());
//                }
//                if (options.has("zoomLevel")) {
//                  builder.zoom(((Double)options.getDouble("zoomLevel")).floatValue());
//                }
//
//                mapView.animateCamera(
//                    CameraUpdateFactory.newCameraPosition(builder.build()),
//                    (options.optInt("duration", 15)) * 1000, // default 15 seconds
//                    null);
//
//                callbackContext.success();
//              } catch (JSONException e) {
//                callbackContext.error(e.getMessage());
//              }
//            }
//          });
//        }

    } else if (ACTION_ADD_POLYGON.equals(action)) {
//        cordova.getActivity().runOnUiThread(new Runnable() {
//          @Override
//          public void run() {
//            try {
//              final PolygonOptions polygon = new PolygonOptions();
//              final JSONObject options = args.getJSONObject(0);
//              final JSONArray points = options.getJSONArray("points");
//              for (int i = 0; i < points.length(); i++) {
//                final JSONObject marker = points.getJSONObject(i);
//                final double lat = marker.getDouble("lat");
//                final double lng = marker.getDouble("lng");
//                polygon.add(new LatLng(lat, lng));
//              }
//              mapView.addPolygon(polygon);
//
//              callbackContext.success();
//            } catch (JSONException e) {
//              callbackContext.error(e.getMessage());
//            }
//          }
//        });

    } else if (ACTION_ADD_GEOJSON.equals(action)) {
      cordova.getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          // TODO implement
          callbackContext.success();
        }
      });

    } else {
      return false;
    }
    return true;
  }

  private void create(final JSONObject options, final CallbackContext callback) {
    if (accessToken == null) {
      callback.error(MAPBOX_ACCESSTOKEN_RESOURCE_KEY + " not set in strings.xml");
      return;
    }

    cordova.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        MapView mapView = createMapView(accessToken, options);
        MapInstance.createMap(mapView, options, new MapInstance.MapCreatedCallback() {
          @Override
          public void onMapReady(final MapInstance map) {
            JSONObject resp = new JSONObject();
            try {
              resp.put("id", map.getId());
              callback.success(resp);
              return;
            } catch (JSONException e) {
              e.printStackTrace();
              callback.error("Failed to create map.");
              return;
            }
          }
        });
      }
    });
  }

  private MapView createMapView(String accessToken, JSONObject options) {
    MapView mapView = new MapView(this.webView.getContext());
    mapView.setAccessToken(accessToken);

    try {
      final JSONObject margins = options.isNull("margins") ? null : options.getJSONObject("margins");
      final int left = (int) (retinaFactor * (margins == null || margins.isNull("left") ? 0 : margins.getInt("left")));
      final int right = (int) (retinaFactor * (margins == null || margins.isNull("right") ? 0 : margins.getInt("right")));
      final int top = (int) (retinaFactor * (margins == null || margins.isNull("top") ? 0 : margins.getInt("top")));
      final int bottom = (int) (retinaFactor * (margins == null || margins.isNull("bottom") ? 0 : margins.getInt("bottom")));

      // need to do this to register a receiver which onPause later needs
      mapView.onResume();
      mapView.onCreate(null);

      // position the mapView overlay
      int webViewWidth = webView.getView().getWidth();
      int webViewHeight = webView.getView().getHeight();
      final FrameLayout layout = (FrameLayout) webView.getView().getParent();
      FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(webViewWidth - left - right, webViewHeight - top - bottom);
      params.setMargins(left, top, right, bottom);
      mapView.setLayoutParams(params);

      layout.addView(mapView);
    } catch (JSONException e) {
      e.printStackTrace();
    }
    return mapView;
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

  private boolean requestPermission(Command command, String... types) {
    if (!permissionGranted(types)) {
      int commandId = Command.save(command);
      cordova.requestPermissions(this, commandId, types);
      return false;
    } else {
      return true;
    }
  }

  // TODO
  public void onRequestPermissionResult(int commandId, String[] permissions, int[] grantResults) throws JSONException {
    for (int r : grantResults) {
      if (r == PackageManager.PERMISSION_DENIED) {
        Command.error(commandId, PERMISSION_DENIED_ERROR);
        return;
      }
    }

    Command.execute(this, commandId);
  }

//  public void onPause(boolean multitasking) {
//    mapView.onPause();
//  }
//
//  public void onResume(boolean multitasking) {
//    mapView.onResume();
//  }
//
//  public void onDestroy() {
//    mapView.onDestroy();
//  }

  private float getRetinaFactor() {
    DisplayMetrics metrics = new DisplayMetrics();
    this.cordova.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
    return metrics.density;
  }

  private String getAccessToken() {
    Activity activity = cordova.getActivity();
    Resources res = activity.getResources();
    String packageName = activity.getPackageName();
    int resourceId;
    String accessToken;

    try {
      resourceId = res.getIdentifier(MAPBOX_ACCESSTOKEN_RESOURCE_KEY, "string", packageName);
      accessToken = activity.getString(resourceId);
    } catch (Resources.NotFoundException e) {
      // we'll deal with this when the accessToken property is read, but for now let's dump the error:
      e.printStackTrace();
      throw e;
    }

    return accessToken;
  }

}

class Command {
  private static int ids = 0;

  private static HashMap<Integer, Command> commands = new HashMap<Integer, Command>();

  public static Command create(final String action, final CordovaArgs args, final CallbackContext callbackContext) {
    return new Command(action, args, callbackContext);
  }

  public static int save(final String action, final CordovaArgs args, final CallbackContext callbackContext) {
    return save(create(action, args, callbackContext));
  }

  public static int save(Command command) {
    int id = command.getId();
    commands.put(id, command);
    return id;
  }

  public static void execute(Mapbox plugin, int id) throws JSONException {
    Command command = commands.remove(id);
    plugin.execute(command);
  }

  public static void error(final int id, final int errorCode) {
    error(id, errorCode, null);
  }

  public static void error(final int id, final int errorCode, final String errorMessage) {
    Command command = commands.remove(id);
    CallbackContext callback = command.getCallbackContext();
    JSONObject error = new JSONObject();
    String message = "Error (" + errorCode + ")";

    if (errorMessage != null) {
      message += ": "  + errorMessage;
    }

    try {
      error.put("code", id);
      error.put("message", message);
      callback.error(error);
    } catch (JSONException e) {
      callback.error(message);
    }
  }

  private int id;

  private String action;

  private CordovaArgs args;

  private CallbackContext callbackContext;

  private Command(final String action, final CordovaArgs args, final CallbackContext callbackContext) {
    this.id = ids++;
    this.action = action;
    this.args = args;
    this.callbackContext = callbackContext;
  }

  public int getId() {
    return id;
  }

  public String getAction() {
    return action;
  }

  public CordovaArgs getArgs() {
    return args;
  }

  public CallbackContext getCallbackContext() {
    return callbackContext;
  }
}
