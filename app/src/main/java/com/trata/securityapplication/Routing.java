package com.trata.securityapplication;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.here.sdk.core.GeoCoordinates;
import com.here.sdk.core.GeoPolyline;
import com.here.sdk.core.errors.InstantiationErrorException;
import com.here.sdk.mapviewlite.Camera;
import com.here.sdk.mapviewlite.CameraUpdate;
import com.here.sdk.mapviewlite.LayerState;
import com.here.sdk.mapviewlite.MapLayer;
import com.here.sdk.mapviewlite.MapMarker;
import com.here.sdk.mapviewlite.MapPolyline;
import com.here.sdk.mapviewlite.MapPolylineStyle;
import com.here.sdk.mapviewlite.MapScene;
import com.here.sdk.mapviewlite.MapViewLite;
import com.here.sdk.mapviewlite.Padding;
import com.here.sdk.mapviewlite.PixelFormat;
import com.here.sdk.routing.CalculateRouteCallback;
import com.here.sdk.routing.Maneuver;
import com.here.sdk.routing.ManeuverAction;
import com.here.sdk.routing.Route;
import com.here.sdk.routing.RouteLeg;
import com.here.sdk.routing.RoutingEngine;
import com.here.sdk.routing.RoutingError;
import com.here.sdk.routing.Waypoint;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class Routing {

    private final MapViewLite mapView;
    private RoutingEngine routingEngine;
    private Context context;
    private final List<MapMarker> mapMarkerList = new ArrayList<>();
    private final List<MapPolyline> mapPolylines = new ArrayList<>();


    public Routing(Context context ,MapViewLite mapView, GeoCoordinates geoCoordinatesAlert, GeoCoordinates geoCoordinatesSaviour){
        this.mapView = mapView;
        this.context = context;

        Camera camera = mapView.getCamera();
        //Sets the mapview at alert location
        camera.setTarget(geoCoordinatesAlert);
        camera.setZoomLevel(12);
        //Initialize routing engine
        try {
            routingEngine = new RoutingEngine();
        } catch (InstantiationErrorException e) {
            new RuntimeException("Initialization of RoutingEngine failed: " + e.error.name());
        }



    }

    public void addRoute(GeoCoordinates geoCoordinatesAlert, GeoCoordinates geoCoordinatesSaviour){
        clearMap();


        Waypoint startWaypoint = new Waypoint(geoCoordinatesAlert);
        Waypoint destinationWaypoint = new Waypoint(geoCoordinatesSaviour);

        List<Waypoint> waypoints =
                new ArrayList<>(Arrays.asList(startWaypoint, destinationWaypoint));
        routingEngine.calculateRoute(
                waypoints,
                new RoutingEngine.PedestrianOptions(),
                new CalculateRouteCallback() {
                    @Override
                    public void onRouteCalculated(@Nullable RoutingError routingError, @Nullable List<Route> routes) {
                        if (routingError == null) {
                            Route route = routes.get(0);
                            showRouteDetails(route);
                            showRouteOnMap(route);
                        } else {
                            //showDialog("Error while calculating a route:", routingError.toString());
                        }
                    }

                });
        /**Traffic*/
      /**  try {
            mapView.getMapScene().setLayerState(MapLayer.TRAFFIC_FLOW, LayerState.ENABLED);
            mapView.getMapScene().setLayerState(MapLayer.TRAFFIC_INCIDENTS, LayerState.ENABLED);
        } catch (MapScene.MapSceneException e) {
            Toast.makeText(context, "Exception when enabling traffic visualization.", Toast.LENGTH_LONG).show();
        } **/
    }

    private void showRouteOnMap(Route route) {
        // Show route as polyline.
        GeoPolyline routeGeoPolyline;
        try {
            routeGeoPolyline = new GeoPolyline(route.getShape());
        } catch (InstantiationErrorException e) {
            // It should never happen that the route shape contains less than two vertices.
            return;
        }
        MapPolylineStyle mapPolylineStyle = new MapPolylineStyle();
        mapPolylineStyle.setColor(0x00908AA0, PixelFormat.RGBA_8888);
        mapPolylineStyle.setWidth(10);
        MapPolyline routeMapPolyline = new MapPolyline(routeGeoPolyline, mapPolylineStyle);
        mapView.getMapScene().addMapPolyline(routeMapPolyline);
        mapPolylines.add(routeMapPolyline);

        //Zoom to route
        CameraUpdate cameraUpdate = mapView.getCamera().calculateEnclosingCameraUpdate(
                route.getBoundingBox(),
                new Padding(10, 10, 10, 10));
        mapView.getCamera().updateCamera(cameraUpdate);

        // Draw a circle to indicate starting point and destination.
        /**  addCircleMapMarker(geoCoordinatesSaviour, R.drawable.saviour);
         addCircleMapMarker(geoCoordinatesAlert, R.drawable.alert);**/

        // Log maneuver instructions per route leg.
       /** List<RouteLeg> routeLegs = route.getLegs();
        for (RouteLeg routeLeg : routeLegs) {
            logManeuverInstructions(routeLeg);
        }*/
    }
    /**Shows directions within html tags*/
    /**private void logManeuverInstructions(RouteLeg routeLeg) {
        Log.d(TAG, "Log maneuver instructions per route leg:");
        List<Maneuver> maneuverInstructions = routeLeg.getManeuvers();
        for (Maneuver maneuverInstruction : maneuverInstructions) {
            ManeuverAction maneuverAction = maneuverInstruction.getAction();
            GeoCoordinates maneuverLocation = maneuverInstruction.getCoordinates();
            String maneuverInfo = maneuverInstruction.getText()
                    + ", Action: " + maneuverAction.name()
                    + ", Location: " + maneuverLocation.toString();
            Log.d(TAG, maneuverInfo);
        }
    }*/
    /** shows distance and time to reach*/
    private void showRouteDetails(Route route) {
        int estimatedTravelTimeInSeconds = route.getTravelTimeInSeconds();
        int lengthInMeters = route.getLengthInMeters();

        String routeDetails =
                "Travel Time: " + formatTime(estimatedTravelTimeInSeconds)
                        + ", Length: " + formatLength(lengthInMeters);

        //showDialog("Route Details", routeDetails);
        Log.d("Route","Distance"+formatLength(lengthInMeters));
    }

    private String formatTime(int sec) {
        int hours = sec / 3600;
        int minutes = (sec % 3600) / 60;

        return String.format(Locale.getDefault(), "%02d:%02d", hours, minutes);
    }

    private String formatLength(int meters) {
        int kilometers = meters / 1000;
        int remainingMeters = meters % 1000;

        return String.format(Locale.getDefault(), "%02d.%02d km", kilometers, remainingMeters);
    }

    public void clearMap() {
        clearWaypointMapMarker();
        clearRoute();
    }


    private void clearWaypointMapMarker() {
        for (MapMarker mapMarker : mapMarkerList) {
            mapView.getMapScene().removeMapMarker(mapMarker);
        }
        mapMarkerList.clear();
    }

    private void clearRoute() {
        for (MapPolyline mapPolyline : mapPolylines) {
            mapView.getMapScene().removeMapPolyline(mapPolyline);
        }
        mapPolylines.clear();
    }

  /**  private void showDialog(String title, String message) {
        AlertDialog.Builder builder =
                new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }*/

}