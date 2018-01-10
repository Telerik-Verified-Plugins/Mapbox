package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.os.Handler;
import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.caverock.androidsvg.SVGParseException;
import com.mapbox.mapboxsdk.Mapbox;
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

import java.io.IOException;
import java.util.ArrayList;

public class CDVMapbox extends CordovaPlugin implements ViewTreeObserver.OnScrollChangedListener {

    private FrameLayout mapsGroup;

    private static final String ADD_GEOJSON = "ADD_GEOJSON"; // todo in JS
    private static final String ADD_MARKER = "ADD_MARKER";
    private static final String ADD_MARKER_CALLBACK = "ADD_MARKER_CALLBACK";
    private static final String ADD_MARKERS = "ADD_MARKERS"; // todo in JS
    private static final String ADD_ON_MAP_CHANGE_LISTENER = "ADD_ON_MAP_CHANGE_LISTENER";
    private static final String CONVERT_COORDINATES = "CONVERT_COORDINATES";
    private static final String CONVERT_POINT = "CONVERT_POINT";
    private static final String DELETE_OFFLINE_REGION = "DELETE_OFFLINE_REGION";
    private static final String DESTROY = "DESTROY";
    private static final String DOWNLOAD_CURRENT_MAP = "DOWNLOAD_CURRENT_MAP";
    private static final String FLY_TO = "FLY_TO";
    private static final String GET_BOUNDS = "GET_BOUNDS";
    private static final String GET_CAMERA_POSITION = "GET_CAMERA_POSITION";
    private static final String GET_CENTER = "GET_CENTER";
    private static final String GET_MARKERS_POSITIONS = "GET_MARKERS_POSITIONS";
    private static final String GET_OFFLINE_REGIONS_LIST = "GET_OFFLINE_REGIONS_LIST";
    private static final String GET_PITCH = "GET_PITCH";
    private static final String GET_ZOOM = "GET_ZOOM";
    private static final String HIDE = "HIDE";
    private static final String MARKER__SET_ICON = "MARKER__SET_ICON";
    private static final String MARKER__SET_LNG_LAT = "MARKER__SET_LNG_LAT";
    private static final String NEXT_MARKERS_POSITIONS_PREDICATE = "NEXT_MARKERS_POSITIONS_PREDICATE";
    private static final String PAUSE_DOWNLOAD = "PAUSE_DOWNLOAD";
    private static final String REMOVE_MARKER = "REMOVE_MARKER";
    private static final String REMOVE_MARKERS = "REMOVE_MARKERS"; // todo in JS
    private static final String RESIZE = "RESIZE";
    private static final String SCROLL_MAP = "SCROLL_MAP";
    private static final String SET_CENTER = "SET_CENTER";
    private static final String SET_CLICKABLE = "SET_CLICKABLE";
    private static final String SET_CONTAINER = "SET_CONTAINER";
    private static final String SET_DEBUG = "SET_DEBUG";
    private static final String SET_PITCH = "SET_PITCH";
    private static final String SET_ZOOM = "SET_ZOOM";
    private static final String SHOW = "SHOW";
    private static final String ZOOM_TO = "ZOOM_TO";

    private static final String MAPBOX_ACCESSTOKEN_RESOURCE_KEY = "mapbox_accesstoken";
    private CordovaWebView _webView;
    private Activity activity;

    public PluginLayout pluginLayout;

    @Override
    public void initialize(CordovaInterface cordova, final CordovaWebView webView) {
        super.initialize(cordova, webView);

        _webView = webView;
        ViewGroup _root = (ViewGroup) _webView.getView().getParent();
        activity = cordova.getActivity();
        _webView.getView().getViewTreeObserver().addOnScrollChangedListener(CDVMapbox.this);

        /**
         * Init MapsManager. It handles multiple maps.
         */
        MapsManager.init(this, activity);

     /*
      * Init the plugin layer responsible to capture touch events.
      * It permits to have Dom Elements on top of the map.
      * If a touch event occurs in one of the embed rectangles and outside of a inner html element,
      * the plugin layer considers that is a map action (drag, pan, etc.).
      * If not, the user surely want to access the UIWebView.
      */
        pluginLayout = new PluginLayout(_webView.getView(), activity);


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
            final String _accessToken = cordova.getActivity().getString(mapboxAccesstokenResourceId);

            Handler mainHandler = new Handler(activity.getMainLooper());

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Mapbox.getInstance(activity, _accessToken);
                }
            });
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

        for (int i = 0; i < MapsManager.getCount(); i++) {
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

            if (SHOW.equals(action)) {
                if (map != null) {
                    if (map.getMapCtrl().getMapView().getVisibility() == View.GONE) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                map.getMapCtrl().getMapView().setVisibility(View.VISIBLE);
                            }
                        });
                        return true;
                    } else {
                        callbackContext.error("Map is already displayed");
                        return false;
                    }
                } else {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final Map aMap = MapsManager.createMap(args, id, callbackContext);
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
                            aMap.getMapCtrl().mapReady = new Runnable() {
                                @Override
                                public void run() {


                                    callbackContext.success();
                                }

                            };
                        }
                    });
                    return true;
                }
            }

            // need a map for all following actions
            if (map == null || !map.getMapCtrl().isReady) {
                callbackContext.error(action + " map is not ready");
                return false;
            }

            final MapController mapCtrl = map.getMapCtrl();

            if (HIDE.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        map.getMapCtrl().getMapView().setVisibility(View.GONE);
                        if (mapCtrl.isDownloading()) mapCtrl.pauseDownload();
                    }
                });
            } else if (DESTROY.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        if (mapCtrl.isDownloading()) mapCtrl.pauseDownload();
                        mapsGroup.removeView(map.getViewGroup());
                        MapsManager.removeMap(id);
                        if (MapsManager.getCount() == 0) {
                            pluginLayout.detachMapsGroup();
                        }
                    }
                });
            } else if (RESIZE.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        map.setContainer(args, callbackContext);
                    }
                });

            } else if (GET_ZOOM.equals(action)) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            callbackContext.success(new JSONObject("{\"zoom\":" + mapCtrl.getZoom() + '}'));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

            } else if (SET_ZOOM.equals(action)) {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1)) {
                                throw new JSONException(action + "needs a zoom level");
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

            } else if (ZOOM_TO.equals(action)) { //todo allow AnimationOptions
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1)) {
                                throw new JSONException(action + "needs a zoom level");
                            }
                            double zoom = args.getDouble(1);
                            JSONObject options = args.isNull(2) ? null : args.getJSONObject(2);
                            mapCtrl.zoomTo(zoom);
                            callbackContext.success();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });

            } else if (GET_CENTER.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        LatLng latLng = mapCtrl.getCenter();
                        JSONObject json = null;
                        try {
                            json = new JSONObject()
                                    .put("lat", latLng.getLatitude())
                                    .put("lng", latLng.getLongitude());
                            callbackContext.success(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });

            } else if (SET_CENTER.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1)) {
                                throw new JSONException(action + "need a [long, lat] coordinates");
                            }
                            JSONArray center = args.getJSONArray(1);
                            mapCtrl.setCenter(center.getDouble(0), center.getDouble(1));
                            callbackContext.success();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });

            } else if (SCROLL_MAP.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1)) {
                                throw new JSONException(action + "need a [x, y] screen coordinates");
                            }
                            JSONArray delta = args.getJSONArray(1);
                            mapCtrl.scrollMap(delta.getLong(0), delta.getLong(1));
                            callbackContext.success();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });

            } else if (SET_PITCH.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1)) {
                                throw new JSONException(action + " need a pitch value");
                            }
                            mapCtrl.setTilt(args.getDouble(1));
                            callbackContext.success();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });

            } else if (GET_PITCH.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject json = new JSONObject().put("pitch", mapCtrl.getTilt());
                            callbackContext.success(json);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });

            } else if (FLY_TO.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject options = args.isNull(1) ? null : args.getJSONObject(1);
                            if (options == null || options.isNull("cameraPosition"))
                                callbackContext.error("Need a camera position");
                            else {
                                mapCtrl.flyTo(options.getJSONObject("cameraPosition"));
                                callbackContext.success("Animation started.");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });

            } else if (ADD_GEOJSON.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callbackContext.error("Not yet implemented.");
                    }
                });

            } else if (ADD_MARKER_CALLBACK.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.markerCallbackContext = callbackContext;
                        mapCtrl.addMarkerCallBack(new Runnable() {
                            @Override
                            public void run() {
                                if (map.markerCallbackContext != null) {
                                    try {
                                        JSONObject json = new JSONObject().put("markerId", mapCtrl.getSelectedMarkerId());
                                        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, json);
                                        pluginResult.setKeepCallback(true);
                                        map.markerCallbackContext.sendPluginResult(pluginResult);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        callbackContext.error("action " + e.getMessage());
                                    }
                                }
                            }
                        });
                    }
                });
            } else if (ADD_MARKER.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1))
                                throw new JSONException(action + " need a source ID");
                            if (args.isNull(2))
                                throw new JSONException(action + " no source provided");
                            if (!args.getJSONObject(2).getString("type").equals("geojson"))
                                throw new JSONException(action + " only handle GeoJSON");

                            String dataType = args.getJSONObject(2).getJSONObject("data").getString("type");
                            if (!dataType.equals("Feature"))
                                throw new JSONException("Only feature are supported as markers source");

                            JSONObject marker = args.getJSONObject(2).getJSONObject("data");

                            String type = marker.getJSONObject("geometry").getString("type");

                            if (!type.equals("Point"))
                                throw new JSONException("Only type Point are supported for markers");

                            mapCtrl.addMarker(args.getString(1), marker);
                            callbackContext.success();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });
            } else if (ADD_MARKERS.equals(action)) {
                activity.runOnUiThread(new Runnable() {
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
            } else if (MARKER__SET_LNG_LAT.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1))
                                throw new JSONException(action + " need a source ID");
                            if (args.isNull(2))
                                throw new JSONException(action + " no coordinates provided");

                            JSONArray coords = args.getJSONArray(2);

                            mapCtrl.setMarkerPosition(args.getString(1), new LatLng(
                                    coords.getDouble(1),
                                    coords.getDouble(0)
                            ));

                            callbackContext.success();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });
            } else if (MARKER__SET_ICON.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (args.isNull(1))
                                throw new JSONException(action + " need a source ID");
                            if (args.isNull(2))
                                throw new JSONException(action + " no images properties provided");

                            JSONObject imageProps = args.getJSONObject(2);

                            if (!imageProps.has("url") && !imageProps.has("data") && !imageProps.has("svg"))
                                throw new JSONException(action + " no images url or file name provided");

                            mapCtrl.setMarkerIcon(args.getString(1), imageProps);

                            callbackContext.success();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        } catch (SVGParseException e) {
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        } catch (IOException e) {
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        }
                    }
                });

            } else if (REMOVE_MARKER.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            mapCtrl.removeMarker(args.getString(1));
                        } catch (JSONException e) {
                            callbackContext.error("action " + e.getMessage() + ". Delete need an array of ids");
                            e.printStackTrace();
                        }
                    }
                });
            } else if (REMOVE_MARKERS.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ArrayList<String> ids = new ArrayList<String>();
                            JSONArray JSONIds = args.getJSONArray(2);
                            for (int i = 0; i < JSONIds.length(); i++) {
                                ids.set(i, JSONIds.getString(i));
                            }
                            mapCtrl.removeMarkers(ids);
                        } catch (JSONException e) {
                            callbackContext.error("action " + e.getMessage() + ". Delete need an array of ids");
                            e.printStackTrace();
                        }
                    }
                });
            } else if (SET_CLICKABLE.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pluginLayout.setClickable(args.getBoolean(1));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });
            } else if (SET_DEBUG.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            pluginLayout.setDebug(args.getInt(1) != 0);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });
            } else if (CONVERT_COORDINATES.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject coords = args.getJSONObject(1);
                            PointF point = mapCtrl.convertCoordinates(new LatLng(
                                    coords.getDouble("lat"),
                                    coords.getDouble("lng")
                            ));
                            callbackContext.success(new JSONObject("{\"x\": " + point.x + ", \"y\": " + point.y + "}"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });
            } else if (CONVERT_POINT.equals(action)) {
                activity.runOnUiThread(new Runnable() {
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
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });
            } else if (GET_MARKERS_POSITIONS.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        PluginResult result = new PluginResult(PluginResult.Status.OK, mapCtrl.getJSONMarkersScreenPositions());
                        callbackContext.sendPluginResult(result);

                    }
                });
            } else if (NEXT_MARKERS_POSITIONS_PREDICATE.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONArray delta = args.getJSONArray(1);
                            PluginResult result = new PluginResult(PluginResult.Status.OK, mapCtrl.getJSONMarkersNextScreenPositions(new PointF(delta.getLong(0), delta.getLong(1))));
                            callbackContext.sendPluginResult(result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } else if (ADD_ON_MAP_CHANGE_LISTENER.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String listener = args.getString(1);

                            if ("REGION_IS_CHANGING".equals(listener)
                                    || "REGION_WILL_CHANGE".equals(listener)
                                    || "REGION_DID_CHANGE".equals(listener)) {
                                mapCtrl.addOnMapChangedListener(listener, new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            PluginResult result = new PluginResult(PluginResult.Status.OK, mapCtrl.getJSONCameraScreenPosition());
                                            result.setKeepCallback(true);
                                            callbackContext.sendPluginResult(result);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            } else throw new JSONException(listener + "not implemented yet.");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error("action " + e.getMessage());
                        }
                    }
                });
            } else if (SET_CONTAINER.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        map.setContainer(args, callbackContext);
                    }
                });
            } else if (DOWNLOAD_CURRENT_MAP.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Runnable startedCallback = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject startedMsg = new JSONObject("{" +
                                            "\"mapDownloadStatus\":{" +
                                            "\"name\": \"" + id + "\"," +
                                            "\"started\": true" +
                                            '}' +
                                            '}');
                                    PluginResult result = new PluginResult(PluginResult.Status.OK, startedMsg);
                                    result.setKeepCallback(true);
                                    callbackContext.sendPluginResult(result);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    callbackContext.error(e.getMessage());
                                }
                            }
                        };

                        Runnable progressCallback = new Runnable() {
                            @Override
                            public void run() {
                                try {
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
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    callbackContext.error(e.getMessage());
                                }
                            }
                        };

                        Runnable finishedCallback = new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject finishedMsg = new JSONObject("{" +
                                            "\"mapDownloadStatus\":{" +
                                            "\"name\": \"" + id + "\"," +
                                            "\"finished\": true" +
                                            '}' +
                                            '}');
                                    PluginResult result = new PluginResult(PluginResult.Status.OK, finishedMsg);
                                    result.setKeepCallback(true);
                                    callbackContext.sendPluginResult(result);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    callbackContext.error(e.getMessage());
                                }
                            }
                        };
                        mapCtrl.downloadRegion("" + id, startedCallback, progressCallback, finishedCallback);
                    }
                });
            } else if (PAUSE_DOWNLOAD.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mapCtrl.pauseDownload();
                        callbackContext.success();
                    }
                });
            } else if (GET_OFFLINE_REGIONS_LIST.equals(action)) {
                activity.runOnUiThread(new Runnable() {
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
            } else if (DELETE_OFFLINE_REGION.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
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
                        } catch (JSONException e) {
                            callbackContext.error("Need an id region to delete.");
                            e.printStackTrace();
                        }
                    }
                });
            } else if (GET_BOUNDS.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
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
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callbackContext.error(e.getMessage());
                        }
                    }
                });
            } else if (GET_CAMERA_POSITION.equals(action)) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            callbackContext.success(mapCtrl.getJSONCameraGeoPosition());
                        } catch (JSONException e) {
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

    private boolean _validateSource() {
        return true;
    }

    private boolean _validateGeoJSON() {
        return true;
    }

    private boolean _validateVector() {
        return true;
    }

    private boolean _validateRaster() {
        return true;
    }

    private boolean _validateImage() {
        return true;
    }

    private boolean _validateVideo() {
        return true;
    }

    private boolean _validateGeoJSONFeature() {
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