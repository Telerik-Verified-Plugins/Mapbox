package com.telerik.plugins.mapbox;
import android.app.Activity;
import android.content.res.Resources;
import android.graphics.PointF;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CDVMapbox extends CordovaPlugin implements ViewTreeObserver.OnScrollChangedListener {

  public FrameLayout mapsGroup;

  private static final String MAPBOX_ACCESSTOKEN_RESOURCE_KEY = "mapbox_accesstoken";
  private static final String ACTION_SHOW = "show";
  private static final String ACTION_HIDE = "hide";
  private static final String ACTION_RESIZE = "resize";
  private static final String ACTION_SET_CLICKABLE = "setClickable";
  private static final String ACTION_SET_DEBUG = "setDebug";
  private static final String ACTION_ADD_MARKERS = "addMarkers";
  private static final String ACTION_ADD_MARKER = "addMarker";
  private static final String ACTION_UPDATE_MARKERS = "updateMarkers";
  private static final String ACTION_UPDATE_MARKER = "updateMarker";
  private static final String ACTION_ADD_MARKER_CALLBACK = "addMarkerCallback";
  private static final String ACTION_REMOVE_MARKERS = "removeMarkers";
  private static final String ACTION_REMOVE_MARKER = "removeMarker";
  private static final String ACTION_DOWNLOAD_CURRENT_MAP = "downloadCurrentMap";
  private static final String ACTION_PAUSE_DOWNLOAD = "pauseDownload";
  private static final String ACTION_GET_OFFLINE_REGIONS_LIST = "getOfflineRegionsList";
  private static final String ACTION_DELETE_OFFLINE_REGION = "deleteOfflineRegion";
  private static final String ACTION_ADD_GEOJSON = "addGeoJSON";
  private static final String ACTION_GET_ZOOM = "getZoom";
  private static final String ACTION_SET_ZOOM = "setZoom";
  private static final String ACTION_ZOOM_TO = "zoomTo";
  private static final String ACTION_GET_BOUNDS = "getBounds";
  private static final String ACTION_GET_CAMERA_POSITION = "getCameraPosition";
  private static final String ACTION_GET_CENTER = "getCenter";
  private static final String ACTION_SET_CENTER = "setCenter";
  private static final String ACTION_GET_PITCH = "getPitch";
  private static final String ACTION_SET_PITCH = "setPitch";
  private static final String ACTION_FLY_TO = "flyTo";
  private static final String ACTION_CONVERT_COORDINATES = "convertCoordinates";
  private static final String ACTION_CONVERT_POINT = "convertPoint";
  private static final String ACTION_ADD_ON_MAP_CHANGE_LISTENER = "addOnMapChangeListener";
  private static final String ACTION_SET_CONTAINER = "setContainer";
  private CordovaWebView _webView;
  private Activity _activity;

  public CordovaInterface _cordova;
  public PluginLayout pluginLayout;

  @Override
  public void initialize(CordovaInterface cordova, final CordovaWebView webView) {
    super.initialize(cordova, webView);

    _cordova = cordova;
    _webView = webView;
    ViewGroup _root = (ViewGroup) _webView.getView().getParent();
    _activity = _cordova.getActivity();
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

    // make webview transparent to see the map through
    //_root.setBackgroundColor(Color.WHITE);
    //webView.getView().setBackgroundColor(Color.TRANSPARENT);

    try {
      int mapboxAccesstokenResourceId = cordova.getActivity().getResources().getIdentifier(MAPBOX_ACCESSTOKEN_RESOURCE_KEY, "string", cordova.getActivity().getPackageName());
      String _accessToken = cordova.getActivity().getString(mapboxAccesstokenResourceId);
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
      MapsManager.getMap(i).onScroll(scrollX, scrollY);
    }
  }

  public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext) throws JSONException {

    try {
      if (args.isNull(0)) {
        callbackContext.error(action + " needs a map id");
        return false;
      }

      final int id = args.getInt(0);
      final Map map = MapsManager.getMap(id);

      if (ACTION_SHOW.equals(action)) {

        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            final Map aMap = map == null ? MapsManager.createMap(args, id, callbackContext) : map;
            if(aMap == map) {
              callbackContext.error("Map is already displayed");
              return;
            }
            //If it is the first map, we set the general layout.
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

            if (MapsManager.getCount() == 1) {
              pluginLayout.attachMapsGroup(mapsGroup);
            }
            aMap.setContainer(args, callbackContext);
            mapsGroup.addView(aMap.getViewGroup());

            callbackContext.success();
          }
        });
        return true;
      }

      // need a map for all following actions
      if (map == null) {
        callbackContext.error(action + " needs a valid map");
        return false;
      }

      final MapController mapCtrl = map.getMapCtrl();

      if (ACTION_HIDE.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          public void run() {
            if(mapCtrl.isDownloading()) mapCtrl.pauseDownload();
            mapsGroup.removeView(map.getViewGroup());
            MapsManager.removeMap(id);
            if (MapsManager.getCount() == 0) {
              pluginLayout.detachMapsGroup();
            }
          }
        });
      } else if (ACTION_RESIZE.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          public void run() {
            map.setContainer(args, callbackContext);
          }
        });

      } else if (ACTION_GET_ZOOM.equals(action)) {
        _activity.runOnUiThread(new Runnable() {

        @Override
          public void run() {
          try {
            callbackContext.success(new JSONObject("{\"zoom\":" + mapCtrl.getZoom()+'}'));
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
        });

      } else if (ACTION_SET_ZOOM.equals(action)) {
        _activity.runOnUiThread(new Runnable() {

          @Override
          public void run() {
            try{
              if (args.isNull(1)){
                throw new JSONException( action + "needs a zoom level");
              }
              double zoom = args.getDouble(1);
              JSONObject options = args.isNull(2) ? null : args.getJSONObject(2);
              mapCtrl.setZoom(zoom);
              callbackContext.success();
            } catch (JSONException e) {
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });

      } else if (ACTION_ZOOM_TO.equals(action)) { //todo allow AnimationOptions
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              if (args.isNull(1)){
                throw new JSONException( action + "needs a zoom level");
              }
              double zoom = args.getDouble(1);
              JSONObject options = args.isNull(2) ? null : args.getJSONObject(2);
              mapCtrl.zoomTo(zoom);
              callbackContext.success();
            } catch (JSONException e) {
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });

      } else if (ACTION_GET_CENTER.equals(action)) {
         _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            LatLng latLng = mapCtrl.getCenter();
            callbackContext.success('{' +
                "\"center\": {" +
                "\"lat\": " + latLng.getLatitude() + ',' +
                "\"lng\": " + latLng.getLongitude() +
              '}'
            );
          }
        });

      } else if (ACTION_SET_CENTER.equals(action)) {
         _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              if (args.isNull(1)){
                throw new JSONException( action + "need a [long, lat] coordinates");
              }
              JSONArray center = args.getJSONArray(1);
              mapCtrl.setCenter(new LatLng(
                  center.getDouble(0),
                  center.getDouble(0))
              );
              callbackContext.success();
            } catch (JSONException e) {
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });

      } else if (ACTION_SET_PITCH.equals(action)) {
         _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              if (args.isNull(1)){
                throw new JSONException( action + " need a pitch value" );
              }
              mapCtrl.setTilt(args.getDouble(1));
              callbackContext.success();
            } catch (JSONException e) {
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });

      }  else if (ACTION_GET_PITCH.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              callbackContext.success(new JSONObject("{\"pitch\":" + mapCtrl.getTilt() + '}'));
            } catch (JSONException e) {
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });

      } else if (ACTION_FLY_TO.equals(action)) {
         _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              JSONObject options = args.isNull(1) ? null : args.getJSONObject(1);
              if(options == null || options.isNull("cameraPosition")) callbackContext.error("Need a camera position");
              else {
                mapCtrl.flyTo(options.getJSONObject("cameraPosition"));
                callbackContext.success("Animation started.");
              }
            } catch (JSONException e) {
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });

      } else if (ACTION_ADD_GEOJSON.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            callbackContext.error("Not yet implemented.");
          }
        });

      } else if (ACTION_ADD_MARKER_CALLBACK.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            map.markerCallbackContext = callbackContext;
            mapCtrl.addMarkerCallBack(new Runnable(){
              @Override
              public void run (){
                if (map.markerCallbackContext != null) {
                  PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, "{\"markerId\": \""+ mapCtrl.getSelectedMarkerId() +"\"}");
                  pluginResult.setKeepCallback(true);
                  map.markerCallbackContext.sendPluginResult(pluginResult);
                }
              }
            });
          }
        });
      } else if (ACTION_ADD_MARKER.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              if(args.isNull(1)) throw new JSONException(action + " need a source ID");
              if(args.isNull(2)) throw new JSONException(action + " no source provided");
              if(!args.getJSONObject(2).getString("type").equals("geojson")) throw new JSONException(action + " only handle GeoJSON");

              String dataType = args.getJSONObject(2).getJSONObject("data").getString("type");
              if (!dataType.equals("Feature")) throw new JSONException("Only feature are supported as markers source");

              JSONObject marker = args.getJSONObject(2).getJSONObject("data");

              String type = marker.getJSONObject("geometry").getString("type");

              if (!type.equals("Point")) throw new JSONException("Only type Point are supported for markers");

              mapCtrl.addMarker(args.getString(1), marker);
              callbackContext.success();
            }catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if (ACTION_ADD_MARKERS.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //todo refactor when #5626
            callbackContext.error("Not yet implemented.");
            /*
            try {
              if(args.isNull(1)) throw new JSONException(action + " need a source ID");
              if(args.isNull(2)) throw new JSONException(action + " no source provided");
              if(!args.getJSONObject(2).getString("type").equals("geojson")) throw new JSONException(action + " only handle GeoJSON");

              String dataType = args.getJSONObject(2).getJSONObject("data").getString("type");
              if (!dataType.equals("FeatureCollection")) throw new JSONException("Only features collection are supported as markers source");

              JSONArray markers = args.getJSONObject(2).getJSONObject("data").getJSONArray("features");
              JSONObject marker;

              for (int i = 0; i < markers.length(); i++) {
                  marker = markers.getJSONObject(i);
                  String type = marker.getJSONObject("geometry").getString("type");

                  if (!type.equals("Point")) throw new JSONException("Only type Point are supported for markers");
              }

              mapCtrl.addMarkers(markers);
              callbackContext.success();
            }catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }*/
          }
        });
      } else if (ACTION_UPDATE_MARKER.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              if(args.isNull(1)) throw new JSONException(action + " need a source ID");
              if(args.isNull(2)) throw new JSONException(action + " no source provided");
              if(!args.getJSONObject(2).getString("type").equals("geojson")) throw new JSONException(action + " only handle GeoJSON");

              String dataType = args.getJSONObject(2).getJSONObject("data").getString("type");
              if (!dataType.equals("Feature")) throw new JSONException("Only feature are supported as markers source");

              JSONObject marker = args.getJSONObject(2).getJSONObject("data");

              String type = marker.getJSONObject("geometry").getString("type");

              if (!type.equals("Point")) throw new JSONException("Only type Point are supported for markers");

              mapCtrl.updateMarker(args.getString(1), marker);
              callbackContext.success();
            }catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if (ACTION_UPDATE_MARKERS.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            //todo refactor when #5626
            callbackContext.error("Not yet implemented.");
/*            try {
              if(args.isNull(1)) throw new JSONException(action + " need a source ID");
              if(args.isNull(2)) throw new JSONException(action + " no source provided");
              if(!args.getJSONObject(2).getString("type").equals("geojson")) throw new JSONException(action + " only handle GeoJSON");

              String dataType = args.getJSONObject(2).getJSONObject("data").getString("type");
              if (!dataType.equals("FeatureCollection")) throw new JSONException("Only features collection are supported as markers source");

              JSONArray markers = args.getJSONObject(2).getJSONObject("data").getJSONArray("features");
              JSONObject marker;

              for (int i = 0; i < markers.length(); i++) {
                marker = markers.getJSONObject(i);
                String type = marker.getJSONObject("geometry").getString("type");

                if (!type.equals("Point")) throw new JSONException("Only type Point are supported for markers");
              }

              mapCtrl.updateMarkers(markers);
              callbackContext.success();
            }catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }*/
          }
        });
      } else if(ACTION_REMOVE_MARKER.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              mapCtrl.removeMarker(args.getString(1));
            } catch (JSONException e){
              callbackContext.error("Delete need an array of ids");
              e.printStackTrace();
            }
          }
        });
      } else if(ACTION_REMOVE_MARKERS.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              ArrayList<String> ids = new ArrayList<>();
              JSONArray JSONIds =  args.getJSONArray(2);
              for (int i = 0; i < JSONIds.length(); i++){
                ids.set(i, JSONIds.getString(i));
              }
              mapCtrl.removeMarkers(ids);
            } catch (JSONException e){
              callbackContext.error("Delete need an array of ids");
              e.printStackTrace();
            }
          }
        });
      } else if(ACTION_SET_CLICKABLE.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              pluginLayout.setClickable(args.getInt(1) != 0);
            } catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if(ACTION_SET_DEBUG.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              pluginLayout.setDebug(args.getInt(1) != 0);
            } catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if(ACTION_CONVERT_COORDINATES.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              JSONObject coords = args.getJSONObject(1);
              PointF point = mapCtrl.convertCoordinates(new LatLng(
                      coords.getDouble("lat"),
                      coords.getDouble("lng")
              ));
              callbackContext.success(new JSONObject("{\"x\": "+point.x+", \"y\": "+point.y+"}"));
            } catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if(ACTION_CONVERT_POINT.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              JSONObject point = args.getJSONObject(1);
              LatLng latLng = mapCtrl.convertPoint(new PointF(
                      (float) point.getDouble("x"),
                      (float) point.getDouble("y")
              ));
              callbackContext.success(new JSONObject("{\"lat\": " + latLng.getLatitude() + ", \"lng\": " + latLng.getLongitude() + "}"));
            } catch (JSONException e) {
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if (ACTION_ADD_ON_MAP_CHANGE_LISTENER.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              String listener = args.getString(1);

              if("REGION_IS_CHANGING".equals(listener)
              || "REGION_WILL_CHANGE".equals(listener)
              || "REGION_DID_CHANGE".equals(listener)) {
                mapCtrl.addOnMapChangedListener(listener, new Runnable() {
                  @Override
                  public void run() {
                    try {
                      PluginResult result = new PluginResult(PluginResult.Status.OK, mapCtrl.getJSONCameraPosition());
                      result.setKeepCallback(true);
                      callbackContext.sendPluginResult(result);
                    } catch (JSONException e) {
                      e.printStackTrace();
                    }
                  }
                });
              } else throw new JSONException(listener + "not implemented yet.");
            } catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if (ACTION_SET_CONTAINER.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            map.setContainer(args, callbackContext);
          }
        });
      } else if(ACTION_DOWNLOAD_CURRENT_MAP.equals(action)) {
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            Runnable startedCallback = new Runnable() {
              @Override
              public void run() {
                try{
                  JSONObject startedMsg = new JSONObject("{" +
                          "\"mapDownloadStatus\":{" +
                            "\"name\": \"" + id + "\"," +
                            "\"started\": true"+
                          '}' +
                        '}');
                  PluginResult result = new PluginResult(PluginResult.Status.OK, startedMsg);
                  result.setKeepCallback(true);
                  callbackContext.sendPluginResult(result);
                } catch (JSONException e){
                  e.printStackTrace();
                  callbackContext.error(e.getMessage());
                }
              }
            };

            Runnable progressCallback = new Runnable() {
              @Override
              public void run() {
                try{
                  JSONObject progressMsg = new JSONObject("{" +
                          "\"mapDownloadStatus\":{" +
                            "\"name\": \"" + id + "\"," +
                            "\"downloading\":" + mapCtrl.isDownloading() + ',' +
                            "\"progress\":" + mapCtrl.getDownloadingProgress() +
                          '}' +
                        '}');
                  PluginResult result = new PluginResult(PluginResult.Status.OK, progressMsg);
                  result.setKeepCallback(true);
                  callbackContext.sendPluginResult(result);
                } catch (JSONException e){
                  e.printStackTrace();
                  callbackContext.error(e.getMessage());
                }
              }
            };

            Runnable finishedCallback = new Runnable() {
              @Override
              public void run() {
                try{
                  JSONObject finishedMsg = new JSONObject("{" +
                          "\"mapDownloadStatus\":{" +
                            "\"name\": \"" + id + "\"," +
                            "\"finished\": true"+
                            '}' +
                          '}');
                  PluginResult result = new PluginResult(PluginResult.Status.OK, finishedMsg);
                  result.setKeepCallback(true);
                  callbackContext.sendPluginResult(result);
                } catch (JSONException e){
                  e.printStackTrace();
                  callbackContext.error(e.getMessage());
                }
              }
            };
            mapCtrl.downloadRegion("" + id, startedCallback, progressCallback, finishedCallback);
          }
        });
      } else if (ACTION_PAUSE_DOWNLOAD.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mapCtrl.pauseDownload();
            callbackContext.success();
          }
        });
      } else if (ACTION_GET_OFFLINE_REGIONS_LIST.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            mapCtrl.getOfflineRegions(new Runnable() {
              @Override
              public void run() {
                ArrayList<String> regionsList = mapCtrl.getOfflineRegionsNames();
                callbackContext.success(new JSONArray(regionsList));
              }
            });
          }
        });
      } else if(ACTION_DELETE_OFFLINE_REGION.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              int regionId = args.getInt(1);
              mapCtrl.removeOfflineRegion(regionId, new Runnable() {
                @Override
                public void run() {
                  try {
                    callbackContext.success(new JSONObject("{\"ok\":true}"));
                  } catch (JSONException e) {
                    e.printStackTrace();
                  }

                }
              });
            } catch (JSONException e){
              callbackContext.error("Need an id region to delete.");
              e.printStackTrace();
            }
          }
        });
      } else if (ACTION_GET_BOUNDS.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try{
              LatLngBounds latLngBounds = mapCtrl.getBounds();
              callbackContext.success(new JSONObject("{" +
                      "\"sw\": {" +
                        "\"lat\":" + latLngBounds.getLatSouth() + ',' +
                        "\"lng\":" + latLngBounds.getLonWest() +
                      "}," +
                      "\"ne\": {" +
                        "\"lat\":" + latLngBounds.getLatNorth() + ',' +
                        "\"lng\":" + latLngBounds.getLonEast() +
                      "}" +
                    "}"
              ));
            } catch (JSONException e){
              e.printStackTrace();
              callbackContext.error(e.getMessage());
            }
          }
        });
      } else if (ACTION_GET_CAMERA_POSITION.equals(action)){
        _activity.runOnUiThread(new Runnable() {
          @Override
          public void run() {
            try {
              callbackContext.success(mapCtrl.getJSONCameraPosition());
            } catch (JSONException e){
              callbackContext.error(e.getMessage());
              e.printStackTrace();
            }
          }
        });
      } else return false;
    } catch (Throwable t) {
      t.printStackTrace();
      callbackContext.error(t.getMessage());
    }
    return true;
  }

  private boolean _validateSource(){
    return true;
  }

  private boolean _validateGeoJSON(){
    return true;
  }

  private boolean _validateVector(){
    return true;
  }

  private boolean _validateRaster(){
    return true;
  }

  private boolean _validateImage(){
    return true;
  }

  private boolean _validateVideo(){
    return true;
  }

  private boolean _validateGeoJSONFeature(){
    return true;
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