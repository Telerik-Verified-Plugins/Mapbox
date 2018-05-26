package com.telerik.plugins.mapbox;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by vikti on 25/06/2016.
 * This file handles the concrete action with MapBox API.
 * //todo decouple the file from MapBox to easily switch to other APIs. Need interface.
 * Kudos to:
 * - Mapbox https://www.mapbox.com/android-sdk/examples/offline-manager/ for the offline part
 * - Anothar (@anothar) for the custom icon createIcon method
 */
class MapController extends AppCompatActivity {
    public FrameLayout.LayoutParams mapFrame;

    private static float _retinaFactor;
    private final static String TAG = "MAP_CONTROLLER";

    private MapView mMapView;
    private HashMap<String, Icon> mIcons = new HashMap<String, Icon>();
    private MapboxMapOptions mInitOptions;
    private MapboxMap mMapboxMap;
    private UiSettings mUiSettings;
    private CameraPosition mCameraPosition;
    private Activity mActivity;
    private OfflineManager mOfflineManager;
    private OfflineRegion mOfflineRegion;
    private boolean mDownloading;
    private int mDownloadingProgress;
    private String mSelectedMarkerId;
    private ArrayList<String> mOfflineRegionsNames = new ArrayList<String>();
    private HashMap<String, String> mAnchors = new HashMap<String, String>();
    private HashMap<String, Marker> mMarkers = new HashMap<String, Marker>();

    private final static String JSON_CHARSET = "UTF-8";
    private final static String JSON_FIELD_REGION_NAME = "FIELD_REGION_NAME";
    boolean isReady = false;
    Runnable mapReady;

    MapView getMapView() {
        return mMapView;
    }

    int getDownloadingProgress() {
        return mDownloadingProgress;
    }

    boolean isDownloading() {
        return mDownloading;
    }

    ArrayList<String> getOfflineRegionsNames() {
        return mOfflineRegionsNames;
    }

    String getSelectedMarkerId() {
        return mSelectedMarkerId;
    }

    MapController(final JSONObject options, Activity activity, Context context, @Nullable final ScrollView scrollView) {

        try {
            mInitOptions = _createMapboxMapOptions(options);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
        _retinaFactor = Resources.getSystem().getDisplayMetrics().density;
        mOfflineManager = OfflineManager.getInstance(context);
        mActivity = activity;

        mMapView = new MapView(mActivity, mInitOptions);
        mMapView.setLayoutParams(
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        // need to do this to register a receiver which onPause later needs
        mMapView.onResume();
        mMapView.onCreate(null);

        // Prevent scroll to intercept the touch when pane the map
        if (scrollView != null) {
            mMapView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            scrollView.requestDisallowInterceptTouchEvent(true);
                            break;
                        case MotionEvent.ACTION_UP:
                        case MotionEvent.ACTION_CANCEL:
                            scrollView.requestDisallowInterceptTouchEvent(false);
                            break;
                    }
                    return mMapView.onTouchEvent(event);
                }
            });
        }

        mMapView.getMapAsync(new OnMapReadyCallback() {

            public void onMapReady(MapboxMap map) {
                mMapboxMap = map;
                mapReady.run();
                isReady = true;

                try {
                    // drawing initial markers
                    if (options.has("sources")) {
                        JSONArray sources = options.getJSONArray("sources");
                        for (int i = 0; i < sources.length(); i++) {
                            //todo refactor when #5626
                            if (!sources.getJSONObject(i).getJSONObject("source").getString("type").equals("geojson"))
                                throw new JSONException("Sources only handle GeoJSON at map creation");

                            String sourceId = sources.getJSONObject(i).getString("sourceId");
                            JSONObject source = sources.getJSONObject(i).getJSONObject("source");

                            String dataType = source.getJSONObject("data").getString("type");
                            if (!dataType.equals("Feature"))
                                throw new JSONException("Only feature are supported as markers source");

                            String type = source.getJSONObject("data").getJSONObject("geometry").getString("type");
                            if (!type.equals("Point"))
                                throw new JSONException("Only type Point are supported for markers");

                            addMarker(sourceId, source.getJSONObject("data"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public LatLng getCenter() {
        CameraPosition cameraPosition = mMapboxMap.getCameraPosition();
        double lat = cameraPosition.target.getLatitude();
        double lng = cameraPosition.target.getLongitude();
        return new LatLng(lat, lng);
    }

    public void setCenter(double... coords) {
        CameraPosition cameraPosition = mMapboxMap.getCameraPosition();
        double lng = coords.length > 0
                ? coords[0]
                : cameraPosition.target != null
                ? cameraPosition.target.getLongitude()
                : 0;
        double lat = coords.length > 1
                ? coords[1]
                : cameraPosition.target != null
                ? cameraPosition.target.getLatitude()
                : 0;
        double alt = coords.length > 2
                ? coords[2]
                : cameraPosition.target != null
                ? cameraPosition.target.getAltitude()
                : 1000;

        mMapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lat, lng, alt))
                        .build()
        ));
    }

    void scrollMap(float x, float y) {
        //CameraPosition cameraPosition = mMapboxMap.getCameraPosition();
        mMapboxMap.moveCamera(CameraUpdateFactory.scrollBy(x, y));
    }

    public void panMap(PointF delta) {
        PointF centerPos = convertCoordinates(getCenter());
        LatLng newCenterLatLng = convertPoint(new PointF(centerPos.x - delta.x, centerPos.y - centerPos.y));
        setCenter(newCenterLatLng.getLongitude(), newCenterLatLng.getLatitude());
    }

    double getTilt() {
        return mMapboxMap.getCameraPosition().tilt;
    }

    void setTilt(double titl) {
        mMapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .tilt(titl)
                        .build()
        ));
    }

    void flyTo(JSONObject position) throws JSONException {
        CameraPosition cameraPosition = mMapboxMap.getCameraPosition();

        try {
            int duration = position.isNull("duration") ? 5000 : position.getInt("duration");

            mMapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(MapController.getCameraPosition(position, cameraPosition)), duration);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Download the actual region for offline use.
     *
     * @param regionName the region name
     * @param onStart    a callback fired when download start
     * @param onProgress a callback fired along the download progression
     * @param onFinish   a callback fired at the end of the download
     */
    void downloadRegion(final String regionName, final Runnable onStart, final Runnable onProgress, final Runnable onFinish) {

        // Set the style, bounds zone and the min/max zoom whidh will be available once offline.
        String styleURL = mInitOptions.getStyle();
        LatLngBounds bounds = mMapboxMap.getProjection().getVisibleRegion().latLngBounds;
        double minZoom = mMapboxMap.getCameraPosition().zoom;
        double maxZoom = mMapboxMap.getMaxZoomLevel();
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
        mOfflineManager.createOfflineRegion(definition, metadata, new OfflineManager.CreateOfflineRegionCallback() {
            @Override
            public void onCreate(OfflineRegion offlineRegion) {
                Log.d(TAG, "Offline region created: " + regionName);
                mOfflineRegion = offlineRegion;
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
        mDownloading = true;
        onStart.run();

        mOfflineRegion.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // Compute a percentage
                double percentage = status.getRequiredResourceCount() >= 0 ?
                        (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0;

                if (status.isComplete()) {
                    // Download complete
                    mDownloading = false;
                    onFinish.run();
                    return;
                } else if (status.isRequiredResourceCountPrecise()) {
                    // Switch to determinate state
                    onProgress.run();
                    mDownloadingProgress = ((int) Math.round(percentage));
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
        mOfflineRegion.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    void pauseDownload() {
        mOfflineRegion.setDownloadState(OfflineRegion.STATE_INACTIVE);
    }

    void getOfflineRegions(final Runnable callback) {
        mOfflineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {

                // Check result. If no regions have been
                // downloaded yet, notify user and return
                if (offlineRegions == null || offlineRegions.length == 0) {
                    return;
                }

                // Clean the last ref array and add all of the region names to the list.
                mOfflineRegionsNames.clear();
                for (OfflineRegion offlineRegion : offlineRegions) {
                    mOfflineRegionsNames.add(getRegionName(offlineRegion));
                }
                callback.run();
            }

            @Override
            public void onError(String error) {

            }
        });
    }

    void removeOfflineRegion(final int regionSelected, final Runnable onDeleteCallback) {
        mOfflineManager.listOfflineRegions(new OfflineManager.ListOfflineRegionsCallback() {
            @Override
            public void onList(final OfflineRegion[] offlineRegions) {

                if (offlineRegions.length > regionSelected) return;

                offlineRegions[regionSelected].delete(new OfflineRegion.OfflineRegionDeleteCallback() {
                    @Override
                    public void onDelete() {
                        onDeleteCallback.run();
                    }

                    @Override
                    public void onError(String error) {
                    }
                });
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

    /**
     * Store empty markers from.
     * The markers are then hydrated in updateMarkers method.
     *
     * @param markers a JSONArray of markers. The markers repscect the GEOJSON specs.
     * @return the array of new markers ids. Insertion order is preserved.
     * @throws JSONException
     */
    public void addMarkers(JSONArray markers) throws JSONException {
        for (int i = 0; i < markers.length(); i++) {
            //todo refactor when #5626
            throw new JSONException("Add multiple marker is not implemented yet");
        }
    }

    void addMarker(String id, JSONObject marker) throws JSONException {
        Marker nativeMarker = mMarkers.get(id);

        if (nativeMarker != null) {
            removeMarker(id);
        }
        MarkerOptions markerOptions = new MarkerOptions();
        JSONObject geometry = marker.isNull("geometry") ? null : marker.getJSONObject("geometry");

        if (geometry != null) {
            markerOptions.position(new LatLng(
                    geometry.getJSONArray("coordinates").getDouble(1),
                    geometry.getJSONArray("coordinates").getDouble(0)
            ));
        } else throw new JSONException("No position found in marker.");

        nativeMarker = mMapboxMap.addMarker(markerOptions);

        // Store in the map markers collection
        mMarkers.put(id, nativeMarker);

        // Hydrate the marker
        hydrateMarker(id, marker);
    }

    void setMarkerPosition(String id, LatLng latLng) throws JSONException {
        Marker marker = mMarkers.get(id);
        if (marker != null) {
            marker.setPosition(latLng);
        } else throw new JSONException(" MapController.setMarkerPosition: unknown marker id " + id);
    }

    void setMarkerIcon(String id, JSONObject imageObject) throws JSONException, IOException, SVGParseException {
        Marker marker = mMarkers.get(id);
        if (marker != null) {
            marker.setIcon(getIcon(imageObject));
        } else throw new JSONException(" MapController.setMarkerIcon: unknown marker id " + id);
    }


    private void hydrateMarker(String id, JSONObject jsonMarker) throws JSONException {
        JSONObject geometry = jsonMarker.isNull("geometry") ? null : jsonMarker.getJSONObject("geometry");
        JSONObject properties = jsonMarker.isNull("properties") ? null : jsonMarker.getJSONObject("properties");
        boolean domAnchor = false;
        Marker marker;

        marker = mMarkers.get(id);

        if (geometry != null) {
            marker.setPosition(new LatLng(
                    geometry.getJSONArray("coordinates").getDouble(1),
                    geometry.getJSONArray("coordinates").getDouble(0)
            ));
        } else throw new JSONException("No position found in marker.");


        if (properties.has("title")) {
            marker.setTitle(properties.getString("title"));
        }

        if (properties.has("snippet")) {
            marker.setSnippet(properties.getString("snippet"));
        }

        domAnchor = properties.has("domAnchor");
        if (domAnchor) {
            // Store the marker as a dom element anchor
            mAnchors.put(id, properties.getString("domAnchor"));
            // Make an invisible marker
            IconFactory iconFactory = IconFactory.getInstance(mActivity);
            Bitmap bmp = Bitmap.createBitmap(new int[Color.TRANSPARENT], 1, 1, Bitmap.Config.ARGB_8888);
            marker.setIcon(iconFactory.fromBitmap(bmp));
            marker.setTitle(null);
            marker.setSnippet(null);
        } else {
            //if it was a dom anchor, delete it
            if (mAnchors.get(id) != null) mAnchors.remove(id);
            if (properties.has("image")) {
                try {
                    marker.setIcon(getIcon(properties.getJSONObject("image")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                IconFactory iconFactory = IconFactory.getInstance(mActivity);
                marker.setIcon(iconFactory.defaultMarker());
            }
        }
    }

    /**
     * Retrun an icon if exists or create a new one.
     *
     * @param imageObject {"image": {height: 40,width: 20,url: 'leaf-orange.png'}}
     * @return Icon
     * @throws JSONException
     * @throws IOException
     * @throws SVGParseException
     */
    private Icon getIcon(JSONObject imageObject) throws JSONException, IOException, SVGParseException {
        String filePath = "";
        if (imageObject.has("url")) filePath = imageObject.getString("url");
        if (imageObject.has("image")) filePath = imageObject.getString("url");
        if (filePath.equals(""))
            throw new JSONException("MapController.setMarkerIconImage: No url or image path found in " + imageObject);

        Icon icon = mIcons.get(filePath) != null ? mIcons.get(filePath) : createIcon(imageObject);
        mIcons.put(filePath, icon); // TODO remove icons when non used anymore. Does C++ part remove image that are no longer needed ?

        return icon;
    }

    void removeMarkers(ArrayList<String> ids) {
        for (int i = 0; i < ids.size(); i++) removeMarker(ids.get(i));
    }

    void removeMarker(String id) {
        if (mMarkers.get(id) == null) return;
        mMapboxMap.removeMarker(mMarkers.get(id));
        if (mMarkers.get(id) != null) mMarkers.remove(id);
        if (mAnchors.get(id) != null) mAnchors.remove(id);
    }

    void addMarkerCallBack(Runnable callback) {
        if (mMapboxMap == null) return;
        mMapboxMap.setOnMarkerClickListener(new MarkerClickListener(callback));
        mMapboxMap.setOnMapClickListener(new MapClickListener(callback));
    }

    public double getZoom() {
        return mMapboxMap.getCameraPosition().zoom;
    }

    public void setZoom(double zoom) {
        CameraPosition position = new CameraPosition.Builder()
                .zoom(zoom)
                .build();

        mMapboxMap.moveCamera(CameraUpdateFactory
                .newCameraPosition(position));
    }

    void zoomTo(double zoom) {
        CameraPosition position = new CameraPosition.Builder()
                .zoom(zoom)
                .build();

        mMapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(position));
    }

    LatLngBounds getBounds() {
        return mMapboxMap.getProjection().getVisibleRegion().latLngBounds;
    }

    PointF convertCoordinates(LatLng coords) {
        return mMapboxMap.getProjection().toScreenLocation(coords);
    }

    LatLng convertPoint(PointF point) {
        return mMapboxMap.getProjection().fromScreenLocation(point);
    }

    void addOnMapChangedListener(String listenerType, Runnable callback) throws JSONException {
        MapChangedListener handler;

        try {
            handler = new MapChangedListener();
            handler.set(listenerType, callback);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException(e.getMessage());
        }
        mMapView.addOnMapChangedListener(handler);
    }

    private int _applyRetinaFactor(long d) {
        return Math.round(d * _retinaFactor);
    }

    private MapboxMapOptions _createMapboxMapOptions(JSONObject options) throws JSONException {
        MapboxMapOptions opts = new MapboxMapOptions();
        opts.styleUrl(_getStyle(options.getString("style")));
        opts.attributionEnabled(options.isNull("hideAttribution") || !options.getBoolean("hideAttribution"));
        opts.logoEnabled(options.isNull("hideLogo") || options.getBoolean("hideLogo"));
//        opts.locationEnabled(!options.isNull("showUserLocation") && options.getBoolean("showUserLocation")); // todo
        opts.camera(MapController.getCameraPosition(options.isNull("cameraPosition") ? null : options.getJSONObject("cameraPosition"), null));
        opts.compassEnabled(options.isNull("hideCompass") || !options.getBoolean("hideCompass"));
        opts.rotateGesturesEnabled(options.isNull("disableRotation") || !options.getBoolean("disableRotation"));
        opts.scrollGesturesEnabled(options.isNull("disableScroll") || !options.getBoolean("disableScroll"));
        opts.zoomGesturesEnabled(options.isNull("disableZoom") || !options.getBoolean("disableZoom"));
        opts.tiltGesturesEnabled(options.isNull("disableTilt") || !options.getBoolean("disableTilt"));
        opts.attributionMargins((!options.isNull("hideAttribution") && options.getBoolean("hideAttribution")) ? new int[]{-300, 0, 0, 0} : null);
        opts.logoMargins((!options.isNull("hideLogo") && options.getBoolean("hideLogo")) ? new int[]{-300, 0, 0, 0} : null);
        return opts;
    }

    private static String _getStyle(final String requested) {
        if ("light".equalsIgnoreCase(requested)) {
            return Style.LIGHT;
        } else if ("dark".equalsIgnoreCase(requested)) {
            return Style.DARK;
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

    private static CameraPosition getCameraPosition(JSONObject position, @Nullable CameraPosition start) throws JSONException {
        CameraPosition.Builder builder = new CameraPosition.Builder(start);

        if (position != null) {
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

    JSONArray getJSONMarkersNextScreenPositions(PointF delta) {
        JSONObject json = new JSONObject();
        JSONArray nextMarkerPositions = new JSONArray();

        try {

            for (Map.Entry<String, Marker> entry : mMarkers.entrySet()) {
                String id = entry.getKey();
                Marker marker = entry.getValue();
                PointF screenPosition = convertCoordinates(marker.getPosition());
                LatLng nextMarkerPos = convertPoint(new PointF(screenPosition.x - delta.x, screenPosition.y - delta.y));
                PointF nextMarkerScreenPos = convertCoordinates(nextMarkerPos);
                JSONObject position = new JSONObject();
                position.put("id", id);
                position.put("x", nextMarkerScreenPos.x);
                position.put("y", nextMarkerScreenPos.y);
                nextMarkerPositions.put(position);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return nextMarkerPositions;
    }

    JSONArray getJSONMarkersScreenPositions() {
        JSONArray positions = new JSONArray();
        try {
            for (Map.Entry<String, Marker> entry : mMarkers.entrySet()) {
                String id = entry.getKey();
                Marker marker = entry.getValue();
                PointF screenPosition = mMapboxMap.getProjection().toScreenLocation(marker.getPosition());
                JSONObject position = new JSONObject();
                position.put("id", id);
                position.put("x", screenPosition.x);
                position.put("y", screenPosition.y);
                positions.put(position);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return positions;
    }

    JSONObject getJSONCameraScreenPosition() throws JSONException {
        CameraPosition position = mMapboxMap.getCameraPosition();
        PointF screenPosition = mMapboxMap.getProjection().toScreenLocation(position.target);
        try {
            return new JSONObject()
                    .put("x", screenPosition.x)
                    .put("y", screenPosition.y)
                    .put("alt", position.target.getAltitude())
                    .put("tilt", position.tilt)
                    .put("bearing", position.bearing);
        } catch (JSONException e) {
            e.printStackTrace();
            throw new JSONException(e.getMessage());
        }
    }

    JSONObject getJSONCameraGeoPosition() throws JSONException {
        CameraPosition position = mMapboxMap.getCameraPosition();
        try {
            return new JSONObject()
                    .put("zoom", position.zoom)
                    .put("long", position.target.getLongitude())
                    .put("alt", position.target.getAltitude())
                    .put("tilt", position.tilt)
                    .put("bearing", position.bearing);
        } catch (JSONException e) {
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
        return new BitmapDrawable(mActivity.getApplicationContext().getResources(), newBM);
    }

    /**
     * Creates image for marker
     *
     * @param imageObject The properties.image part of a JSON marker
     * @return an icon with a custom image
     * @throws JSONException
     * @throws IOException
     * @throws SVGParseException
     */
    // Thanks Anothar :)
    private Icon createIcon(JSONObject imageObject) throws JSONException, IOException, SVGParseException {
        InputStream istream = null;
        BitmapDrawable bitmapDrawable;
        Icon icon = null;
        Context ctx = mActivity.getApplicationContext();
        AssetManager am = ctx.getResources().getAssets();
        IconFactory iconFactory = IconFactory.getInstance(mActivity);
        try {
            if (imageObject != null) {
                if (imageObject.has("url")) {
                    String stringURI = imageObject.getString("url");

                    final Uri uri = Uri.parse(stringURI);
                    final String uriPath = uri.getPath();
                    boolean isAsset;
                    String path;
                    String fileName;

                    // Only accept file:/// URI
                    if (uri.getScheme().equals("file")) {
                        isAsset = uriPath.indexOf("/android_asset/", 0) > -1;
                        final String filesDir = mActivity.getFilesDir().getPath();
                        fileName = uri.getLastPathSegment();
                        // Asset path is different from app file path.
                        path = uriPath.substring(isAsset ? "/android_asset/".length() : filesDir.length(), uriPath.lastIndexOf('/') + 1);
                    } else throw new JSONException("createIcon: " + uri + " URI");

                    // We first look in the current asset bundle.
                    File iconFile = new File(mActivity.getFilesDir(), path + fileName);
                    if (iconFile.exists()) istream = new FileInputStream(iconFile);

                        // If file does not exists we get the original version in the initial asset bundle with AssetsManager
                    else {
                        istream = am.open(path + fileName);
                    }

                    if (fileName.endsWith(".svg")) {
                        bitmapDrawable = createSVG(SVG.getFromInputStream(istream), imageObject.has("width") ? _applyRetinaFactor(imageObject.getInt("width")) : 0,
                                imageObject.has("height") ? _applyRetinaFactor(imageObject.getInt("height")) : 0);
                    } else {
                        bitmapDrawable = new BitmapDrawable(ctx.getResources(), istream);
                    }
                } else if (imageObject.has("data")) {
                    byte[] decodedBytes = Base64.decode(imageObject.getString("data"), 0);
                    bitmapDrawable = new BitmapDrawable(ctx.getResources(), BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length));

                } else if (imageObject.has("svg")) {
                    bitmapDrawable = createSVG(SVG.getFromString(imageObject.getString("svg")), imageObject.has("width") ? _applyRetinaFactor(imageObject.getInt("width")) : 0,
                            imageObject.has("height") ? _applyRetinaFactor(imageObject.getInt("height")) : 0);
                } else {
                    throw new JSONException("Not found image data");
                }
                if (imageObject.has("width") && imageObject.has("height")) {
                    Bitmap bitmap = new BitmapDrawable(ctx.getResources(),
                            Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(),
                                    _applyRetinaFactor(imageObject.getInt("width")),
                                    _applyRetinaFactor(imageObject.getInt("height")),
                                    true
                            )).getBitmap();
                    icon = iconFactory.fromBitmap(bitmap);
                } else {
                    icon = iconFactory.fromBitmap(bitmapDrawable.getBitmap());
                }
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

        public void set(String listenerType, Runnable callback) throws JSONException {
            _callback = callback;

            if (listenerType.equals("REGION_WILL_CHANGE")) _listener = MapView.REGION_WILL_CHANGE;
            else if (listenerType.equals("REGION_WILL_CHANGE_ANIMATED"))
                _listener = MapView.REGION_WILL_CHANGE_ANIMATED;
            else if (listenerType.equals("REGION_IS_CHANGING"))
                _listener = MapView.REGION_IS_CHANGING;
            else if (listenerType.equals("REGION_DID_CHANGE"))
                _listener = MapView.REGION_DID_CHANGE;
            else if (listenerType.equals("REGION_DID_CHANGE_ANIMATED"))
                _listener = MapView.REGION_DID_CHANGE_ANIMATED;
            else if (listenerType.equals("WILL_START_LOADING_MAP"))
                _listener = MapView.WILL_START_LOADING_MAP;
            else if (listenerType.equals("DID_FAIL_LOADING_MAP"))
                _listener = MapView.DID_FAIL_LOADING_MAP;
            else if (listenerType.equals("DID_FINISH_LOADING_MAP"))
                _listener = MapView.DID_FINISH_LOADING_MAP;
            else if (listenerType.equals("WILL_START_RENDERING_FRAME"))
                _listener = MapView.WILL_START_RENDERING_FRAME;
            else if (listenerType.equals("DID_FINISH_RENDERING_FRAME"))
                _listener = MapView.DID_FINISH_RENDERING_FRAME;
            else if (listenerType.equals("DID_FINISH_RENDERING_FRAME_FULLY_RENDERED"))
                _listener = MapView.DID_FINISH_RENDERING_FRAME_FULLY_RENDERED;
            else if (listenerType.equals("WILL_START_RENDERING_MAP"))
                _listener = MapView.WILL_START_RENDERING_MAP;
            else if (listenerType.equals("DID_FINISH_RENDERING_MAP"))
                _listener = MapView.DID_FINISH_RENDERING_MAP;
            else if (listenerType.equals("DID_FINISH_RENDERING_MAP_FULLY_RENDERED"))
                _listener = MapView.DID_FINISH_RENDERING_MAP_FULLY_RENDERED;
            else throw new JSONException("Unknown map listener type:" + listenerType);
        }

        @Override
        public void onMapChanged(int listener) {
            if (_listener == listener) _callback.run();
        }
    }

    private class MarkerClickListener implements MapboxMap.OnMarkerClickListener {
        private Runnable _callback;

        public MarkerClickListener(Runnable callback) {
            _callback = callback;
        }

        @Override
        public boolean onMarkerClick(@NonNull Marker marker) {
            Set<Map.Entry<String, Marker>> elements = mMarkers.entrySet();
            Iterator<Map.Entry<String, Marker>> iterator = elements.iterator();
            Map.Entry<String, Marker> entry;
            while (iterator.hasNext()) {
                entry = iterator.next();
                if (entry.getValue() == marker) mSelectedMarkerId = entry.getKey();
            }

            _callback.run();
            return true;
        }
    }

    private class MapClickListener implements MapboxMap.OnMapClickListener {
        private Runnable _callback;

        public MapClickListener(Runnable callback) {
            _callback = callback;
        }

        @Override
        public void onMapClick(@NonNull LatLng point) {
            mSelectedMarkerId = "";

            _callback.run();
        }
    }

    /*
        private class PanListener implements MoveGestureDetector.OnMoveGestureListener {

            @Override
            public boolean onMove(MoveGestureDetector detector) {
                Log.d("motion", "paning");
                return false;
            }

            @Override
            public boolean onMoveBegin(MoveGestureDetector detector) {
                Log.d("motion", "pan began");
                return false;
            }

            @Override
            public void onMoveEnd(MoveGestureDetector detector) {
                Log.d("motion", "pan ended");
            }
        }*/
    @Override
    public void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }
}