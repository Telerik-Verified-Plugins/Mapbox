package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;

import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import io.cordova.hellocordova.MainActivity;
import io.cordova.hellocordova.R;

/**
 * Created by vikti on 25/06/2016.
 * This file handles the concrete action with MapBox API.
 * //todo decouple the file from MapBox to easily switch to other APIs. Need interface.
 * Kudos to:
 * - Mapbox https://www.mapbox.com/android-sdk/examples/offline-manager/ for the offline part
 * - Anothar (@anothar) for the custom icon createIcon method
 */
public class MapController {
    public FrameLayout.LayoutParams mapFrame;

    private static float _retinaFactor;
    private final static String TAG = "MainActivity";

    private MapView _mapView;
    private MapboxMapOptions _initOptions;
    private MapboxMap _mapboxMap;
    private UiSettings _uiSettings;
    private CameraPosition _cameraPosition;
    private Activity _activity;
    private OfflineManager _offlineManager;
    private OfflineRegion _offlineRegion;
    private boolean _downloading;
    private int _downloadingProgress;
    private ArrayList<String> _offlineRegionsNames = new ArrayList<>();
    public final static String JSON_CHARSET = "UTF-8";
    public final static String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";

    public View getMapView(){
        return (View) _mapView;
    }
    public int getDownloadingProgress(){
        return _downloadingProgress;
    }
    public boolean isDownloading() {
        return _downloading;
    }
    public ArrayList<String> getOfflineRegionsNames(){
        return _offlineRegionsNames;
    }

    public MapController(final JSONObject options, Activity activity, Context context){

        try{
            _initOptions = _createMapboxMapOptions(options);
        } catch (JSONException e){
            e.printStackTrace();
            return;
        }
        _retinaFactor = Resources.getSystem().getDisplayMetrics().density;
        _offlineManager = OfflineManager.getInstance(context);

        _activity = activity;

        _mapView = new MapView(_activity, _initOptions);
        _mapView.setLayoutParams(
            new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
            ));

        // need to do this to register a receiver which onPause later needs
        _mapView.onResume();
        _mapView.onCreate(null);

        _mapView.getMapAsync(new OnMapReadyCallback() {

            public void onMapReady(MapboxMap map) {

                _mapboxMap = map;
                /*
                _uiSettings = _mapboxMap.getUiSettings();
                _cameraPosition = _mapboxMap.getCameraPosition();
                final JSONObject _initArgs;
                final JSONObject margins;
                final int left;
                final int right;
                final int top;
                final int bottom;
                final JSONObject center;

                try {
                    _initArgs = args.getJSONObject(1);
                    margins = _initArgs.isNull("margins") ? null : _initArgs.getJSONObject("margins");
                    center = _initArgs.isNull("center") ? null : _initArgs.getJSONObject("center");
                    left = _applyRetinaFactor(margins == null || margins.isNull("left") ? 0 : margins.getInt("left"));
                    right = _applyRetinaFactor(margins == null || margins.isNull("right") ? 0 : margins.getInt("right"));
                    top = _applyRetinaFactor(margins == null || margins.isNull("top") ? 0 : margins.getInt("top"));
                    bottom = _applyRetinaFactor(margins == null || margins.isNull("bottom") ? 0 : margins.getInt("bottom"));

                } catch (JSONException e) {
                    callbackContext.error(e.getMessage());
                    return;
                }
                /*
                final String style = getStyle(_initArgs.optString("style"));
                _mapView.setStyleUrl(style);

                _showUserLocation = !_initArgs.isNull("showUserLocation") && _initArgs.getBoolean("showUserLocation");


                _uiSettings.setCompassEnabled(_initArgs.isNull("hideCompass") || !_initArgs.getBoolean("hideCompass"));
                _uiSettings.setRotateGesturesEnabled(_initArgs.isNull("disableRotation") || !_initArgs.getBoolean("disableRotation"));
                _uiSettings.setScrollGesturesEnabled(_initArgs.isNull("disableScroll") || !_initArgs.getBoolean("disableScroll"));
                _uiSettings.setZoomGesturesEnabled(_initArgs.isNull("disableZoom") || !_initArgs.getBoolean("disableZoom"));
                _uiSettings.setTiltGesturesEnabled(_initArgs.isNull("disableTilt") || !_initArgs.getBoolean("disableTilt"));

                // placing these offscreen in case the user wants to hide them
                if (!_initArgs.isNull("hideAttribution") && _initArgs.getBoolean("hideAttribution")) {
                    _uiSettings.setAttributionMargins(-300, 0, 0, 0);
                }
                if (!_initArgs.isNull("hideLogo") && _initArgs.getBoolean("hideLogo")) {
                    _uiSettings.setLogoMargins(-300, 0, 0, 0);
                }

                if (_showUserLocation) {
                    _showUserLocation();
                }

                double zoom = _initArgs.isNull("zoomLevel") ? 10 : _initArgs.getDouble("zoomLevel");
                float zoomLevel = (float) zoom;
                if (center != null) {
                    final double lat = center.getDouble("lat");
                    final double lng = center.getDouble("lng");
                    setCenter(new LatLng(lat, lng));
                } else {
                    if (zoomLevel > 18.0) {
                        zoomLevel = 18.0f;
                    }
                    setZoom(zoomLevel);
                }

                if (_initArgs.has("markers")) {
                    addMarkers(_initArgs.getJSONArray("markers"));
                }
*/
                // position the _mapView overlay
               // _mapView.setLayoutParams(mapFrame);
            }
        });

    }

    public LatLng getCenter(){
        CameraPosition cameraPosition = _mapboxMap.getCameraPosition();
        double lat = cameraPosition.target.getLatitude();
        double lng = cameraPosition.target.getLongitude();
        return new LatLng(lat, lng);
    }

    public void setCenter(double... coords){
        CameraPosition cameraPosition = _mapboxMap.getCameraPosition();
        double lat = coords.length > 0 ? coords[0] : cameraPosition.target.getLatitude();
        double lng = coords.length > 1 ? coords[1] : cameraPosition.target.getLongitude();
        double alt = coords.length > 2 ? coords[2] : cameraPosition.target.getAltitude(); //todo alt or zoom ????

        _mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                    .target(new LatLng(lat, lng, alt))
                    .build()
        ));
    }

    public double getTilt(){
        return _mapboxMap.getCameraPosition().tilt;
    }

    public void setTilt(double titl){
        _mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .tilt(titl)
                        .build()
        ));
    }

    public void flyTo(JSONObject position) throws JSONException{
        CameraPosition cameraPosition = _mapboxMap.getCameraPosition();
        LatLng latLng;
        try{
            int duration = position.isNull("duration") ? 5000 : position.getInt("duration");

            _mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(MapController.getCameraPosition(position, cameraPosition)), duration);

        } catch (JSONException e){
            e.printStackTrace();
        }
    }

    /**
     * Download the actual region for offline use.
     *
     * @param regionName the region name
     * @param onStart a callback fired when download start
     * @param onProgress a callback fired along the download progression
     * @param onFinish a callback fired at the end of the download
     */
    public void downloadRegion(final String regionName, final Runnable onStart, final Runnable onProgress, final Runnable onFinish) {

        // Set the style, bounds zone and the min/max zoom whidh will be available once offline.
        String styleURL = _initOptions.getStyle();
        LatLngBounds bounds = _mapboxMap.getProjection().getVisibleRegion().latLngBounds;
        double minZoom = _mapboxMap.getCameraPosition().zoom;
        double maxZoom = _mapboxMap.getMaxZoom();
        OfflineTilePyramidRegionDefinition definition = new OfflineTilePyramidRegionDefinition(
                styleURL, bounds, minZoom, maxZoom, _retinaFactor);

        // Build a JSONObject using the user-defined offline region title,
        // convert it into string, and use it to create a metadata variable.
        // The metadata variable will later be passed to createOfflineRegion()
        byte[] metadata;
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(JSON_FIELD_REGION_NAME, regionName);
            String json = jsonObject.toString();
            metadata = json.getBytes(JSON_CHARSET);
        } catch (Exception e) {
            Log.e(TAG, "Failed to encode metadata: " + e.getMessage());
            metadata = null;
        }

        // Create the offline region and launch the download
        _offlineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                Log.d(TAG, "Offline region created: " + regionName);
                _offlineRegion = offlineRegion;
                launchDownload(onStart, onProgress, onFinish);
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void launchDownload(final Runnable onStart, final Runnable onProgress, final Runnable onFinish) {
        // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        // Start the progression
        _downloading = true;
        onStart.run();

        _offlineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // Compute a percentage
                double percentage = status.getRequiredResourceCount() >= 0 ?
                        (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0;

                if (status.isComplete()) {
                    // Download complete
                    _downloading = false;
                    onFinish.run();
                    return;
                } else if (status.isRequiredResourceCountPrecise()) {
                    // Switch to determinate state
                    onProgress.run();
                    _downloadingProgress = ((int) Math.round(percentage));
                }

                // Log what is being currently downloaded
                Log.d(TAG, String.format("%s/%s resources; %s bytes downloaded.",
                        String.valueOf(status.getCompletedResourceCount()),
                        String.valueOf(status.getRequiredResourceCount()),
                        String.valueOf(status.getCompletedResourceSize())));
            }

            @Override
            public void onError(OfflineRegionError error) {
                Log.e(TAG, "onError reason: " + error.getReason());
                Log.e(TAG, "onError message: " + error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
            }
        });

        // Change the region state
        _offlineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    public void pauseDownload(){
        _offlineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE);
    }

    public void getOfflineRegions(final Runnable callback){
        _offlineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {
                // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.length == 0) {
                    return;
                }

                // Add all of the region names to a list
                for (OfflineRegion offlineRegion : offlineRegions) {
                    _offlineRegionsNames.add(getRegionName(offlineRegion));
                }
                callback.run();
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    private String getRegionName(OfflineRegion offlineRegion) {
        // Get the retion name from the offline region metadata
        String regionName;

        try {
            byte[] metadata = offlineRegion.getMetadata();
            String json = new String(metadata, JSON_CHARSET);
            JSONObject jsonObject = new JSONObject(json);
            regionName = jsonObject.getString(JSON_FIELD_REGION_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Failed to decode metadata: " + e.getMessage());
            regionName = "Region " + offlineRegion.getID();
        }
        return regionName;
    }

    public void addMarkers(JSONArray markers) throws JSONException{
        for (int i = 0; i < markers.length(); i++) {
            MarkerOptions markerOptions = new MarkerOptions();
            JSONObject marker = markers.getJSONObject(i);
            JSONObject geometry = marker.isNull("geometry")?null:marker.getJSONObject("geometry");
            JSONObject properties = marker.isNull("properties")?null:marker.getJSONObject("properties");

            if(geometry != null){
                markerOptions.position(new LatLng(
                        geometry.getJSONArray("coordinates").getDouble(1),
                        geometry.getJSONArray("coordinates").getDouble(0)
                ));
            } else throw new JSONException("No position found in marker.");

            if(properties != null) {
                if(properties.has("title")) {
                    markerOptions.title(properties.getString("title"));
                }

                if(properties.has("snippet")) {
                    markerOptions.snippet(properties.getString("snippet"));
                }

                boolean domAnchor = !properties.isNull("domAnchor") && properties.getBoolean("domAnchor");
                if(!domAnchor) {
                    if (properties.has("image")) {
                        try{
                            markerOptions.icon(createIcon(properties));
                        } catch (SVGParseException | IOException e) {
                            e.printStackTrace();
                        }

                    } else {
                        IconFactory iconFactory = IconFactory.getInstance(_activity);
                        Drawable iconDrawable = ContextCompat.getDrawable(_activity, R.drawable.default_marker);
                        markerOptions.icon(iconFactory.fromDrawable(iconDrawable));
                    }
                }
            }
            _mapboxMap.addMarker(markerOptions);
        }
    }

    public double getZoom(){
        return _mapboxMap.getCameraPosition().zoom;
    }

    public void setZoom(double zoom){
        CameraPosition position = new CameraPosition.Builder()
                .zoom(zoom)
                .build();

        _mapboxMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(position));

    }

    public void zoomTo(double zoom){
        CameraPosition position = new CameraPosition.Builder()
                .zoom(zoom)
                .build();

        _mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position));

    }


    public void setCenter(LatLng coords) throws JSONException {
        _mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(coords)
                        .build()
        ));
    }

    public LatLngBounds getBounds(){
        return _mapboxMap.getProjection().getVisibleRegion().latLngBounds;
    }

    public PointF convertCoordinates(LatLng coords){
        return _mapboxMap.getProjection().toScreenLocation(coords);
    }

    public LatLng convertPoint(PointF point){
        return _mapboxMap.getProjection().fromScreenLocation(point);
    }


    public void addOnMapChangedListener(String listenerType, Runnable callback) throws JSONException{
        MapChangedListener handler;

        try{
            handler = new MapChangedListener();
            handler.set(listenerType, callback);
        } catch (JSONException e){
            e.printStackTrace();
            throw new JSONException(e.getMessage());
        }
        _mapView.addOnMapChangedListener(handler);
    }

    private int _applyRetinaFactor(long d) {
        return Math.round(d * _retinaFactor);
    }

    private MapboxMapOptions _createMapboxMapOptions(JSONObject options) throws JSONException {
        MapboxMapOptions opts = new MapboxMapOptions();
        opts.styleUrl(_getStyle(options.getString("style")));
        opts.attributionEnabled(options.isNull("hideAttribution") || !options.getBoolean("hideAttribution"));
        opts.logoEnabled(options.isNull("hideLogo") || options.getBoolean("hideLogo"));
        //opts.locationEnabled(!options.isNull("showUserLocation") && options.getBoolean("showUserLocation")); // todo bug #5607
        //opts.camera(MapController.getCameraPosition(options.isNull("position")?null:options.getJSONObject("position"), null)); // todo bug #5607
        opts.compassEnabled(options.isNull("hideCompass") || !options.getBoolean("hideCompass"));
        opts.rotateGesturesEnabled(options.isNull("disableRotation") || !options.getBoolean("disableRotation"));
        opts.scrollGesturesEnabled(options.isNull("disableScroll") || !options.getBoolean("disableScroll"));
        opts.zoomGesturesEnabled(options.isNull("disableZoom") || !options.getBoolean("disableZoom"));
        opts.tiltGesturesEnabled(options.isNull("disableTilt") || !options.getBoolean("disableTilt"));
        return opts;
    }

    private static String _getStyle(final String requested) {
        if ("light".equalsIgnoreCase(requested)) {
            return Style.LIGHT;
        } else if ("dark".equalsIgnoreCase(requested)) {
            return Style.DARK;
        } else if ("emerald".equalsIgnoreCase(requested)) {
            return Style.EMERALD;
        } else if ("satellite".equalsIgnoreCase(requested)) {
            return Style.SATELLITE;
            // TODO not currently supported on Android
//    } else if ("hybrid".equalsIgnoreCase(requested)) {
//      return Style.HYBRID;
        } else if ("streets".equalsIgnoreCase(requested)) {
            return Style.MAPBOX_STREETS;
        } else {
            return requested;
        }
    }

    public static CameraPosition getCameraPosition(JSONObject position, @Nullable CameraPosition start) throws JSONException {
        CameraPosition.Builder builder = new CameraPosition.Builder(start);

        if(position != null) {
            if (!position.isNull("target")) {
                JSONObject target = position.getJSONObject("target");
                builder.target(new LatLng(target.getDouble("lat"), target.getDouble("lng")));
            }

            if (!position.isNull("zoom")) {
                builder.zoom(position.getDouble("zoom"));
            }

            if (!position.isNull("bearing")) {
                builder.bearing(position.getDouble("bearing"));
            }

            if (!position.isNull("tilt")) {
                builder.tilt(position.getDouble("tilt"));
            }
        }
        return builder.build();
    }

    public JSONObject getJSONCameraPosition() throws JSONException{
        CameraPosition position = _mapboxMap.getCameraPosition();
        try{
            return new JSONObject(
                "{"+
                    "\"lat\":"+position.target.getLatitude()+','+
                    "\"lng\":"+position.target.getLatitude()+','+
                    "\"alt\":"+position.target.getAltitude()+','+
                    "\"tilt\":"+position.tilt+','+
                    "\"bearing\":"+position.bearing+
                "}");
        } catch (JSONException e){
            e.printStackTrace();
            throw new JSONException(e.getMessage());
        }
    }

    private BitmapDrawable createSVG(SVG svg, int width, int height) throws SVGParseException {
        if (width == 0)
            width = _applyRetinaFactor((int) Math.ceil(svg.getDocumentWidth()));
        if (height == 0)
            height = _applyRetinaFactor((int) Math.ceil(svg.getDocumentHeight()));
        Bitmap newBM = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas bmcanvas = new Canvas(newBM);
        svg.renderToCanvas(bmcanvas);
        return new BitmapDrawable(_activity.getApplicationContext().getResources(), newBM);
    }

    /**
     * Creates image for marker
     * @param properties The properties.image part of a JSON marker
     * @return an icon with a custom image
     * @throws JSONException if
     * @throws IOException
     * @throws SVGParseException
     */
    // Thanks Anothar :)
    private Icon createIcon(JSONObject properties) throws JSONException, IOException, SVGParseException {
        InputStream istream = null;
        BitmapDrawable bitmap;
        Icon icon;
        Context ctx = _activity.getApplicationContext();
        AssetManager am = ctx.getResources().getAssets();
        IconFactory iconFactory = IconFactory.getInstance(_activity);
        final JSONObject imageSettings = properties.optJSONObject("image");
        try {
            if (imageSettings != null) {
                if (imageSettings.has("url")) {
                    String filePath = imageSettings.getString("url");
                    istream = am.open(filePath);
                    if (filePath.endsWith(".svg")) {
                        bitmap = createSVG(SVG.getFromInputStream(istream), imageSettings.has("width") ? _applyRetinaFactor(imageSettings.getInt("width")) : 0,
                                imageSettings.has("height") ? _applyRetinaFactor(imageSettings.getInt("height")) : 0);
                    } else {
                        bitmap = new BitmapDrawable(ctx.getResources(), istream);
                    }
                } else if (imageSettings.has("data")) {
                    byte[] decodedBytes = Base64.decode(imageSettings.getString("data"), 0);
                    bitmap = new BitmapDrawable(ctx.getResources(), BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));

                } else if (imageSettings.has("svg")) {
                    bitmap = createSVG(SVG.getFromString(imageSettings.getString("svg")), imageSettings.has("width") ? _applyRetinaFactor(imageSettings.getInt("width")) : 0,
                            imageSettings.has("height") ? _applyRetinaFactor(imageSettings.getInt("height")) : 0);
                } else {
                    throw new JSONException("Not found image data");
                }
                if (imageSettings.has("width") && imageSettings.has("height")) {
                    icon = iconFactory.fromDrawable(bitmap, _applyRetinaFactor(imageSettings.getInt("width")),
                            _applyRetinaFactor(imageSettings.getInt("height")));
                } else {
                    icon = iconFactory.fromDrawable(bitmap);
                }

            } else {
                String filePath = properties.getString("image");
                istream = am.open(filePath);
                if (filePath.endsWith(".svg"))
                    bitmap = createSVG(SVG.getFromInputStream(istream), 0, 0);
                else
                    bitmap = new BitmapDrawable(ctx.getResources(), istream);
                icon = iconFactory.fromDrawable(bitmap);
            }
        } finally {
            if (istream != null)
                istream.close();
        }
        return icon;
    }

    private class MapChangedListener implements MapView.OnMapChangedListener {

        private Runnable _callback;
        private int _listener;

        public void set(String listenerType, Runnable callback) throws JSONException{
            _callback = callback;
            switch(listenerType) {
                case "REGION_WILL_CHANGE":
                    _listener = MapView.REGION_WILL_CHANGE;
                    break;
                case "REGION_WILL_CHANGE_ANIMATED":
                    _listener = MapView.REGION_WILL_CHANGE_ANIMATED;
                    break;
                case "REGION_IS_CHANGING":
                    _listener = MapView.REGION_IS_CHANGING;
                    break;
                case "REGION_DID_CHANGE":
                    _listener = MapView.REGION_DID_CHANGE;
                    break;
                case "REGION_DID_CHANGE_ANIMATED":
                    _listener = MapView.REGION_DID_CHANGE_ANIMATED;
                    break;
                case "WILL_START_LOADING_MAP":
                    _listener = MapView.WILL_START_LOADING_MAP;
                    break;
                case "DID_FAIL_LOADING_MAP":
                    _listener = MapView.DID_FAIL_LOADING_MAP;
                    break;
                case "DID_FINISH_LOADING_MAP":
                    _listener = MapView.DID_FINISH_LOADING_MAP;
                    break;
                case "WILL_START_RENDERING_FRAME":
                    _listener = MapView.WILL_START_RENDERING_FRAME;
                    break;
                case "DID_FINISH_RENDERING_FRAME":
                    _listener = MapView.DID_FINISH_RENDERING_FRAME;
                    break;
                case "DID_FINISH_RENDERING_FRAME_FULLY_RENDERED":
                    _listener = MapView.DID_FINISH_RENDERING_FRAME_FULLY_RENDERED;
                    break;
                case "WILL_START_RENDERING_MAP":
                    _listener = MapView.WILL_START_RENDERING_MAP;
                    break;
                case "DID_FINISH_RENDERING_MAP":
                    _listener = MapView.DID_FINISH_RENDERING_MAP;
                    break;
                case "DID_FINISH_RENDERING_MAP_FULLY_RENDERED":
                    _listener = MapView.DID_FINISH_RENDERING_MAP_FULLY_RENDERED;
                    break;
                default:
                    throw new JSONException("Unknown map listener type:" + listenerType);
            }
        }

        @Override
        public void onMapChanged(int listener) {
            if(_listener == listener) _callback.run();
        }
    }
}
