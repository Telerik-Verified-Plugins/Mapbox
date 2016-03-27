package com.telerik.plugins.mapbox;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.offline.OfflineManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

// TODO for screen rotation, see https://www.mapbox.com/mapbox-android-sdk/#screen-rotation
// TODO fox Xwalk compat, see nativepagetransitions plugin
// TODO look at demo app: https://github.com/mapbox/mapbox-gl-native/blob/master/android/java/MapboxGLAndroidSDKTestApp/src/main/java/com/mapbox/mapboxgl/testapp/MainActivity.java
public class Mapbox extends CordovaPlugin {

  private static final String LOG_TAG = "MapboxCordovaPlugin";

  public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
  public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;

  public static final int PERMISSION_DENIED_ERROR = 20;

  private static final String MAPBOX_ACCESSTOKEN_RESOURCE_KEY = "mapbox_accesstoken";

  private static final String ACTION_CREATE_MAP = "createMap";
  private static final String ACTION_JUMP_TO = "jumpTo";
  private static final String ACTION_SHOW_USER_LOCATION = "showUserLocation";
  private static final String ACTION_LIST_OFFLINE_REGIONS = "listOfflineRegions";
  private static final String ACTION_CREATE_OFFLINE_REGION = "createOfflineRegion";
  private static final String ACTION_DOWNLOAD_OFFLINE_REGION = "downloadOfflineRegion";
  private static final String ACTION_PAUSE_OFFLINE_REGION = "pauseOfflineRegion";
  private static final String ACTION_OFFLINE_REGION_STATUS = "offlineRegionStatus";
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

  private MapboxManager mapboxManager;

  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    this.mapboxManager = new MapboxManager(this.getAccessToken(), this.getRetinaFactor(), webView);
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

    if (ACTION_CREATE_MAP.equals(action)) {
      final JSONObject options = args.getJSONObject(0);
      boolean showUserLocation = !options.isNull("showUserLocation") && options.getBoolean("showUserLocation");
      if (!showUserLocation || requestPermission(command, COARSE_LOCATION, FINE_LOCATION)) {
        this.createMap(options, callbackContext);
      }
    }

    else if (ACTION_SHOW_USER_LOCATION.equals(action)) {
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
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
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
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
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
      try {
        callbackContext.success(map.getCenter());
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    }

    else if (ACTION_SET_CENTER.equals(action)) {
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
      final JSONArray center = args.getJSONArray(1);
      try {
        map.setCenter(center);
        callbackContext.success();
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    }

    else if (ACTION_GET_ZOOMLEVEL.equals(action)) {
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
      callbackContext.success("" + map.getZoom());
    }

    else if (ACTION_SET_ZOOMLEVEL.equals(action)) {
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
      final double zoom = args.getDouble(1);
      map.setZoom(zoom);
      callbackContext.success();
    }

    else if (ACTION_ADD_MARKERS.equals(action)) {
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
      try {
        map.addMarkers(args.getJSONArray(1));
        callbackContext.success();
      } catch (JSONException e) {
        callbackContext.error(e.getMessage());
      }
    }

    else if (ACTION_ADD_MARKER_CALLBACK.equals(action)) {
      final long mapId = args.getLong(0);
      final Map map = mapboxManager.getMap(mapId);
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

    else if (ACTION_LIST_OFFLINE_REGIONS.equals(action)) {
      this.listOfflineRegions(callbackContext);
    }

    else if (ACTION_CREATE_OFFLINE_REGION.equals(action)) {
      final JSONObject options = args.getJSONObject(0);
      final CallbackContext onProgress = new CallbackContext(args.getString(1), this.webView);
      final CallbackContext onComplete = new CallbackContext(args.getString(2), this.webView);
      this.createOfflineRegion(options, callbackContext, onProgress, onComplete);
    }

    else if (ACTION_DOWNLOAD_OFFLINE_REGION.equals(action)) {
      final long offlineRegionId = args.getLong(0);
      final OfflineRegion region = this.mapboxManager.getOfflineRegion(offlineRegionId);
      region.download();
      callbackContext.success();
    }

    else if (ACTION_PAUSE_OFFLINE_REGION.equals(action)) {
      final long offlineRegionId = args.getLong(0);
      final OfflineRegion region = this.mapboxManager.getOfflineRegion(offlineRegionId);
      region.pause();
      callbackContext.success();
    }

    else if (ACTION_OFFLINE_REGION_STATUS.equals(action)) {
      final long offlineRegionId = args.getLong(0);
      final OfflineRegion region = this.mapboxManager.getOfflineRegion(offlineRegionId);

      region.getStatus(new MapboxManager.OfflineRegionStatusCallback() {
        @Override
        public void onStatus(JSONObject status) {
          callbackContext.success(status);
        }

        @Override
        public void onError(String error) {
          callbackContext.error(error);
        }
      });
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

  private void createMap(final JSONObject options, final CallbackContext callback) {
      cordova.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mapboxManager.createMap(options, callback);
      }
    });
  }

  public void listOfflineRegions(final CallbackContext callback) {
    cordova.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mapboxManager.loadOfflineRegions(new MapboxManager.LoadOfflineRegionsCallback() {
          @Override
          public void onList(JSONArray offlineRegions) {
            callback.success(offlineRegions);
          }

          @Override
          public void onError(String error) {
            String message = "Error loading offline regions: " + error;
            callback.error(message);
          }
        });
      }
    });
  }

  public void createOfflineRegion(final JSONObject options, final CallbackContext callback, final CallbackContext onProgress, final CallbackContext onComplete) throws JSONException {
    cordova.getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mapboxManager.createOfflineRegion(options, callback, new MapboxManager.OfflineRegionProgressCallback() {
          @Override
          public void onProgress(JSONObject progress) {
            PluginResult result = new PluginResult(PluginResult.Status.OK, progress);
            result.setKeepCallback(true);
            onProgress.sendPluginResult(result);
          }

          @Override
          public void onComplete(JSONObject progress) {
            Log.d(LOG_TAG, "complete");
            PluginResult result = new PluginResult(PluginResult.Status.OK, progress);
            onComplete.sendPluginResult(result);
          }

          @Override
          public void onError(String error) {
            String message = "Failed to create offline region: " + error;
            Log.e(LOG_TAG, message);
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
            result.setKeepCallback(true);
            onComplete.error(message);
          }
        });
      }
    });
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

  @Override
  public void onStart() {
    for (Map map : mapboxManager.maps()) {
      map.getMapView().onStart();
    }
  }

  @Override
  public void onResume(boolean multitasking) {
    for (Map map : mapboxManager.maps()) {
      map.getMapView().onResume();
    }
  }

  @Override
  public void onPause(boolean multitasking) {
    for (Map map : mapboxManager.maps()) {
      map.getMapView().onPause();
    }
  }

  @Override
  public void onStop() {
    for (Map map : mapboxManager.maps()) {
      map.getMapView().onStop();
    }
  }

  @Override
  public void onDestroy() {
    for (Map map : mapboxManager.maps()) {
      map.getMapView().onDestroy();
    }
  }

  private float getRetinaFactor() {
    Activity activity = this.cordova.getActivity();
    Resources res = activity.getResources();
    DisplayMetrics metrics = res.getDisplayMetrics();
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
