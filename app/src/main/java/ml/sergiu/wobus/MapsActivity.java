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
import com.google.maps.android.SphericalUtil;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
//    private GoogleMap mMap;

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
    }

    @Override
    public void onMapReady(GoogleMap mMap) {
//        mMap = googleMap;

        LatLng cluj = new LatLng(46.770109, 23.587726);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(cluj, 12));
        mMap.setMinZoomPreference(10);

        TransitLine current_line = (TransitLine) getIntent().getSerializableExtra("current_line");

        drawPath(mMap, current_line.routeAB);
        drawPath(mMap, current_line.routeBA);

        drawVehicles(mMap, current_line, current_line.routeAB, current_line.departuresA);
        drawVehicles(mMap, current_line, current_line.routeBA, current_line.departuresB);
    }

    private void drawPath(GoogleMap mMap, List<TransitStop> route) {
        PolylineOptions path = new PolylineOptions();
        Log.i("MAP", "" + route.size());

        for (TransitStop stop : route) {
            path.add(new LatLng(stop.coords.lat, stop.coords.lng));
            Log.i("MAP", "Added coordinates to polyline path: " + stop.coords);
        }

        path.color(0xAA4885ed); // google blue, partially transparent
        Polyline polyline = mMap.addPolyline(path);
    }

    private void drawVehicles(GoogleMap mMap, TransitLine current_line, List<TransitStop> route,
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
                TransitStop current_stop = route.get(i);
                TransitStop next_stop = route.get(i + 1);

                double distance = SphericalUtil.computeDistanceBetween(
                        new LatLng(current_stop.coords.lat, current_stop.coords.lng),
                        new LatLng(next_stop.coords.lat, next_stop.coords.lng));

//                Log.i("MAP", "Distance between " + current_stop.name + " and " + next_stop.name
//                        + " is " + new DecimalFormat("#0.00").format(distance) + " meters");

                if (traveled > distance) {
                    traveled -= distance;
                    continue;
                } else {
                    Log.i("MAP", "Bus is between " + current_stop.name + " and " + next_stop.name);

                    LatLng current_position = SphericalUtil.interpolate(
                            new LatLng(current_stop.coords.lat, current_stop.coords.lng),
                            new LatLng(next_stop.coords.lat, next_stop.coords.lng),
                            traveled / distance);


                    // compute marker rotation
                    Location x = new Location(LocationManager.GPS_PROVIDER);
                    x.setLatitude(current_stop.coords.lat);
                    x.setLongitude(current_stop.coords.lng);
                    Location y = new Location(LocationManager.GPS_PROVIDER);
                    y.setLatitude(next_stop.coords.lat);
                    y.setLongitude(next_stop.coords.lng);
                    float bearing = x.bearingTo(y);

                    Log.i("MAP", "Bearing is: " + bearing);
                    Log.i("MAP", "Exact location: " + current_position);

                    mMap.addMarker(new MarkerOptions()
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable
                                    .ic_red_bus_marker))
                            .position(current_position)
                            .rotation(bearing)
                            .flat(true)
                            .title(current_line.name));
                    break;
                }
            }

        }

    }
}
