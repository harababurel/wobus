package ml.sergiu.wobus;

import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.android.SphericalUtil;
import com.google.maps.model.DirectionsResult;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.maps.model.JointType.ROUND;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    GeoApiContext context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);
        // Get the SupportMapFragment and request notification
        // when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_maps_key))
                .build();
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
        LatLng cluj = new LatLng(46.770109, 23.587726);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cluj, 12));
        mMap.setMinZoomPreference(10);

        TransitLine current_line = (TransitLine) getIntent().getSerializableExtra("current_line");

        if(current_line.accurateRouteAB == null || current_line.accurateRouteBA == null) {
            Log.i("MAP", "Current line is missing accurate route. Requesting now.");
            current_line.requestAccurateRoute(context);
        }

        drawPath(mMap, current_line.accurateRouteAB);
        drawPath(mMap, current_line.accurateRouteBA);

        drawVehicles(mMap, current_line, current_line.accurateRouteAB, current_line.departuresA);
        drawVehicles(mMap, current_line, current_line.accurateRouteBA, current_line.departuresB);
    }


    private void drawPath(GoogleMap mMap, List<com.google.maps.model.LatLng> route) {
        PolylineOptions path = new PolylineOptions();
        Log.i("MAP", "Polyline has " + route.size() + " points");

        for (com.google.maps.model.LatLng node : route) {
            path.add(new LatLng(node.lat, node.lng));
        }

        path.color(0xAA4885ed); // google blue, partially transparent
        path.jointType(ROUND);
        Polyline polyline = mMap.addPolyline(path);
    }

//    private void drawBetterPath(GoogleMap mMap, List<com.google.maps.model.LatLng>
//            polyline_nodes) {
//        PolylineOptions path = new PolylineOptions();
//        Log.i("MAP", "Polyline has " + polyline_nodes.size() + " points");
//
//        for (com.google.maps.model.LatLng node : polyline_nodes) {
//            path.add(new LatLng(node.lat, node.lng));
//        }
//
//        path.color(0xAA4885ed); // google blue, partially transparent
//        path.jointType(ROUND);
//        Polyline polyline = mMap.addPolyline(path);
//    }

    private void drawVehicles(GoogleMap mMap, TransitLine current_line, List<com.google.maps
            .model.LatLng> route,
                              List<Date> departures) {
//        TransitLine currentLine = (TransitLine) getIntent().getSerializableExtra("current_line");

        Date now = Calendar.getInstance().getTime();
        for (Date departure : departures) {
            if (departure.after(now)) {
                continue;
            }

            double delta_hours = (Math.abs(now.getTime() - departure.getTime())) / 1000.0 / 60.0 / 60.0;
            double speed_kmph = 19.6;
            double distance_km = speed_kmph * delta_hours;
            double traveled = distance_km * 1000.0; // meters

            Log.i("MAP", "Bus departing at " + departure + " traveled " + distance_km + " km so" +
                    " far");

            for (int i = 0; i + 1 < route.size(); i++) {
                com.google.maps.model.LatLng current_node = route.get(i);
                com.google.maps.model.LatLng next_node = route.get(i+1);

                double distance = SphericalUtil.computeDistanceBetween(
                        new LatLng(current_node.lat, current_node.lng),
                        new LatLng(next_node.lat, next_node.lng));

//                Log.i("MAP", "Distance between " + current_stop.name + " and " + next_stop.name
//                        + " is " + new DecimalFormat("#0.00").format(distance) + " meters");

                if (traveled > distance) {
                    traveled -= distance;
                    continue;
                } else {
                    LatLng current_position = SphericalUtil.interpolate(
                            new LatLng(current_node.lat, current_node.lng),
                            new LatLng(next_node.lat, next_node.lng),
                            traveled / distance);


                    // compute marker rotation
                    Location x = new Location(LocationManager.GPS_PROVIDER);
                    x.setLatitude(current_node.lat);
                    x.setLongitude(current_node.lng);
                    Location y = new Location(LocationManager.GPS_PROVIDER);
                    y.setLatitude(next_node.lat);
                    y.setLongitude(next_node.lng);
                    float bearing = x.bearingTo(y);

                    Log.i("MAP", "Bearing is: " + bearing);
                    Log.i("MAP", "Exact location: " + current_position);

                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable
                                    .ic_red_bus_marker))
                            .position(current_position)
                            .rotation(bearing)
                            .anchor(0.5f, 0.5f)
                            .flat(true)
                            .title(current_line.name));
                    break;
                }
            }

        }

    }
}
