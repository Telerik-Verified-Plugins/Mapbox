package com.telerik.plugins.mapbox;

import android.support.annotation.NonNull;
import android.util.Log;
import android.webkit.GeolocationPermissions;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.GeoJSONObject;
import com.cocoahero.android.geojson.Geometry;
import com.cocoahero.android.geojson.GeometryCollection;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.MultiLineString;
import com.cocoahero.android.geojson.MultiPoint;
import com.cocoahero.android.geojson.MultiPolygon;
import com.cocoahero.android.geojson.Point;
import com.cocoahero.android.geojson.Polygon;
import com.cocoahero.android.geojson.Position;
import com.cocoahero.android.geojson.Ring;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

interface DataSource {
    List<Feature> getFills();
    List<Feature> getLines();
    List<Feature> getSymbols();
}

class FeatureManager {
    private String TAG = "FeatureManager";
    protected MapView mapView;

    protected HashMap<String, DataSource> sources = new HashMap<String, DataSource>();

    public FeatureManager(MapView mapView) {
        this.mapView = mapView;
    }

    public void addGeoJSONSource(String name, String json) throws JSONException {
        this.sources.put(name, new GeoJSONSource(name).addGeoJSON(json));
    }

    public void addGeoJSONSource(String name, JSONObject json) throws JSONException {
        this.sources.put(name, new GeoJSONSource(name).addGeoJSON(json));
    }

    public void addFillLayer(String id, String source, JSONObject layer) {
        for (Feature feature : this.sources.get(source).getFills()) {
            ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
            for (Ring ring : ((Polygon) feature.getGeometry()).getRings()) {
                for (Position position : ring.getPositions()) {
                    latLngs.add(new LatLng(position.getLatitude(), position.getLongitude()));
                }
            }
            LatLng[] points = latLngs.toArray(new LatLng[latLngs.size()]);

            PolygonOptions polygon = new PolygonOptions()
                    // TODO: Need to use values in layer to set options.
                    .add(points);

            this.mapView.addPolygon(polygon);
        }
    }

    public void addLineLayer(String id, String source, JSONObject layer) {
        for (Feature feature : this.sources.get(source).getLines()) {
            ArrayList<LatLng> latLngs = new ArrayList<LatLng>();
            for (Position position : ((LineString) feature.getGeometry()).getPositions()) {
                latLngs.add(new LatLng(position.getLatitude(), position.getLongitude()));
            }
            LatLng[] points = latLngs.toArray(new LatLng[latLngs.size()]);

            PolylineOptions line = new PolylineOptions()
                    // TODO: Need to use values in layer to set options.
                    .add(points);

            this.mapView.addPolyline(line);
        }
    }

    public void addMarkerLayer(String id, String source, JSONObject layer) {
        for (Feature feature : this.sources.get(source).getSymbols()) {
            Position p = ((Point) feature.getGeometry()).getPosition();
            JSONObject properties = feature.getProperties();

            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(p.getLatitude(), p.getLongitude()));

            try {
                // TODO: Need to use values in layer to set options.
                marker.title(properties.getString("title"));
                marker.snippet(properties.getString("description"));
            } catch (JSONException e) {
                Log.w(TAG, "Error parsing GeoJSON Feature properties: " + e.getMessage());
            }

            this.mapView.addMarker(marker);
        }
    }

    private class GeoJSONSource implements DataSource {
        private String TAG = "GeoJSONSource";

        protected String name;

        protected ArrayList<Feature> polygons = new ArrayList<Feature>();
        protected ArrayList<Feature> polylines = new ArrayList<Feature>();
        protected ArrayList<Feature> markers = new ArrayList<Feature>();

        public GeoJSONSource(String name) {
            this.name = name;
        }

        @Override
        public List<Feature> getFills() {
            return polygons;
        }

        @Override
        public List<Feature> getLines() {
            return polylines;
        }

        @Override
        public List<Feature> getSymbols() {
            return markers;
        }

        public GeoJSONSource addGeoJSON(String json) throws JSONException {
            return this.addGeoJSON(GeoJSON.parse(json));
        }

        public GeoJSONSource addGeoJSON(JSONObject json) throws JSONException {
            return this.addGeoJSON(GeoJSON.parse(json));
        }

        public GeoJSONSource addGeoJSON(GeoJSONObject geojson) {
            if (geojson instanceof FeatureCollection) {
                FeatureCollection fc = (FeatureCollection) geojson;
                for (Feature f : fc.getFeatures()) {
                    this.addGeoJSON(f);
                }
            } else if (geojson instanceof Feature) {
                Feature feature = (Feature) geojson;
                addFeature(feature);
            } else {
                Log.e(TAG, "GeoJSON must be FeatureCollection or Feature.");
            }

            return this;
        }

        /**
         * TODO: Handling of Complex geometries (GeometryCollection & Multi*) needs improvement when Mapbox SDK supports them better.
         * TODO: Recursive processing of GeoJSON could probably be optimized.
         *
         * @param feature
         */
        public void addFeature(Feature feature) {
            Geometry geom = feature.getGeometry();

            if (geom instanceof GeometryCollection) {
                GeometryCollection gc = (GeometryCollection) geom;
                for (Geometry g : gc.getGeometries()) {
                    this.addFeature(feature, g);
                }
            }
            else if (geom instanceof MultiPolygon) {
                MultiPolygon multiPoly = (MultiPolygon) geom;
                for (Polygon poly : multiPoly.getPolygons()) {
                    this.addFeature(feature, poly);
                }
            }
            else if (geom instanceof MultiLineString) {
                MultiLineString multiLine = (MultiLineString) geom;
                for (LineString ls : multiLine.getLineStrings()) {
                    this.addFeature(feature, ls);
                }
            }
            else if (geom instanceof MultiPoint) {
                MultiPoint multiPoint = (MultiPoint) geom;
                for (Position p : multiPoint.getPositions()) {
                    this.addFeature(feature, new Point(p));
                }
            }
            else {
                this.addFeature(feature, geom);
            }
        }

        protected void addFeature(Feature feature, Geometry geom) {
            if (geom instanceof MultiPolygon || geom instanceof MultiLineString || geom instanceof MultiPoint) {
                feature.setGeometry(geom);
                Feature f = new Feature(geom);
                f.setProperties(feature.getProperties());
                this.addFeature(f);
            }
            else if (geom instanceof Polygon) {
                this.polygons.add(this.createFeature(feature.getProperties(), geom));
            }
            else if (geom instanceof LineString) {
                this.polylines.add(this.createFeature(feature.getProperties(), geom));
            }
            else if (geom instanceof Point) {
                this.markers.add(this.createFeature(feature.getProperties(), geom));
            } else {
                // Unsupported geometry type.
                Log.e(TAG, String.format("Unsupported GeoJSON geometry type: %s.", geom.getType()));
            }
        }

        protected Feature createFeature(JSONObject properties, Geometry geom) {
            Feature f = new Feature();
            f.setGeometry(geom);
            f.setProperties(properties);
            return f;
        }
    }
}