package ml.sergiu.wobus;

import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

// https://stackoverflow.com/questions/33885378/animate-a-carmarker-along-a-path-in-google-map-android
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    //    private static Location oldLocation;
//    private static float angle;
    private GoogleMap mMap;
//    private LocationManager locationManager;
//    private Marker marker;

    private static void animateMarker(GoogleMap myMap, final Marker marker, final List<LatLng> directionPoint,
                                      final boolean hideMarker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = myMap.getProjection();
        final long duration = 30000;

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                if (i < directionPoint.size())
                    marker.setPosition(directionPoint.get(i));
                i++;


                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });

    }

    public void setAnimation(final List<LatLng> directionPoint) {
        Marker marker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker))
                .position(directionPoint.get(0))
                .flat(true));

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(directionPoint.get(0), 10));
        animateMarker(mMap, marker, directionPoint, false);
    }

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
    public void onMapReady(GoogleMap googleMap) {
        // Add a marker in Sydney, Australia,
        // and move the map's camera to the same location.
//        LatLng sydney = new LatLng(-33.852, 151.211);
//        googleMap.addMarker(new MarkerOptions().position(sydney)
//                .title("Marker in Sydney"));
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap = googleMap;
        List<LatLng> points = new ArrayList<>();
        points.add(new LatLng(46.7712, 23.6236));
        points.add(new LatLng(47.1855, 23.0573));
        setAnimation(points);


        drawPath(mMap);
        drawVehicles(mMap);
    }

    private void drawPath(GoogleMap mMap) {
        PolylineOptions path = new PolylineOptions();
        TransitLine currentLine = (TransitLine) getIntent().getSerializableExtra("current_line");
        Log.i("MAP", "" + currentLine.routeAB.size());

        for (TransitStop stop : currentLine.routeAB) {
            path.add(new LatLng(stop.coords.lat, stop.coords.lng));
            Log.i("MAP", "Added coordinates to polyline path: " + stop.coords);
        }

        Polyline polyline = mMap.addPolyline(path);
    }

    private void drawVehicles(GoogleMap mMap) {
        TransitLine currentLine = (TransitLine) getIntent().getSerializableExtra("current_line");

        Date now = Calendar.getInstance().getTime();
        for (Date departure : currentLine.departuresA) {
            if (departure.after(now)) {
                continue;
            }

            double delta_hours = (Math.abs(now.getTime() - departure.getTime())) / 1000.0 / 60.0 / 60.0;
            double speed_kmph = 30.0;
            double distance_km = speed_kmph * delta_hours;

            Log.i("MAP", "Bus departing at " + departure + " traveled " + distance_km + " km so" +
                    " far");

            for (int i = 0; i + 1 < currentLine.routeAB.size(); i++) {
                TransitStop current_stop = currentLine.routeAB.get(i);
                TransitStop next_stop = currentLine.routeAB.get(i + 1);

                double distance = SphericalUtil.computeDistanceBetween(
                        new LatLng(current_stop.coords.lat, current_stop.coords.lng),
                        new LatLng(next_stop.coords.lat, next_stop.coords.lng));

                Log.i("MAP", "Distance between " + current_stop.name + " and " + next_stop.name
                        + " is " + new DecimalFormat("#0.00").format(distance) + " meters");

            }

        }

    }


//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//        mMap = googleMap;
//    }
//
//    @Override
//    public void onLocationChanged(Location location) {
//        locationUpdate(location);
//        if (oldLocation != null) {
//            double bearing = angleFromCoordinate(oldLocation.getLatitude(), oldLocation.getLongitude(), location.getLatitude(), location.getLongitude());
//            changeMarkerPosition(bearing);
//        }
//        oldLocation = location;
//    }
//
//    private void locationUpdate(Location location) {
//        LatLng latLng = new LatLng((location.getLatitude()), (location.getLongitude()));
//        MarkerOptions markerOptions = new MarkerOptions();
//        markerOptions.position(latLng);
//        markerOptions.title("Current Position");
//        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bus_marker));
//        mMap.clear();
//        marker = mMap.addMarker(markerOptions);
//        CameraPosition position = CameraPosition.builder()
//                .target(new LatLng(location.getLatitude(), location.getLongitude()))
//                .zoom(19)
//                .tilt(30)
//                .build();
//        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(position));
//    }
//
//    private void changeMarkerPosition(double position) {
//        float direction = (float) position;
//        Log.e("LocationBearing", "" + direction);
//
//        if (direction == 360.0) {
//            //default
//            marker.setRotation(angle);
//        } else {
//            marker.setRotation(direction);
//            angle = direction;
//        }
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//
//    }
//
//
//    private double angleFromCoordinate(double lat1, double long1, double lat2,
//                                       double long2) {
//        double dLon = (long2 - long1);
//
//        double y = Math.sin(dLon) * Math.cos(lat2);
//        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
//                * Math.cos(lat2) * Math.cos(dLon);
//
//        double brng = Math.atan2(x, y);
//
//        brng = Math.toDegrees(brng);
//        brng = (brng + 360) % 360;
//        brng = 360 - brng;
//        return brng;
//    }
}
