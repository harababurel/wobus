package ml.sergiu.wobus;

import android.util.Log;

import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class TransitLine implements Serializable {
    public String name;
    public URI uri;
    public TransitType type;
    public String endA, endB;
    List<Date> departuresA, departuresB;
    List<TransitStop> routeAB;
    List<TransitStop> routeBA;

    List<com.google.maps.model.LatLng> accurateRouteAB;
    List<com.google.maps.model.LatLng> accurateRouteBA;

//    private URI mapImageURI;
//    private SerialBitmap mapImage;


    public TransitLine(String name, URI uri) {
        this.name = name;
        this.uri = uri;
//        this.mapImageURI = null;
//        this.mapImage = null;
//        this.mapImagePath = null;


        departuresA = new LinkedList<>();
        departuresB = new LinkedList<>();

        routeAB = new LinkedList<>();
        routeBA = new LinkedList<>();

        accurateRouteAB = null;
        accurateRouteBA = null;
    }

    public String mapImagePath() {
        return "maps/" + name + ".jpeg";
    }

    public void requestAccurateRoute(GeoApiContext context) {
        accurateRouteAB = requestAccurateDirectedRoute(context, routeAB);
        accurateRouteBA = requestAccurateDirectedRoute(context, routeBA);
    }

    private List<com.google.maps.model.LatLng> requestAccurateDirectedRoute(GeoApiContext context, List<TransitStop> route) {
        List<com.google.maps.model.LatLng> polyline_nodes = null;

        try {
            DirectionsApiRequest req = DirectionsApi.newRequest(context)
                    .origin(route.get(0).coords)
                    .destination(route.get(route.size() - 1).coords)
                    .mode(TravelMode.TRANSIT);

//            for (int i = 1; i + 1 < route.size(); i++) {
//                req.waypoints(route.get(i).coords);
//            }

            DirectionsResult result = req.await();
            polyline_nodes = result.routes[0].overviewPolyline.decodePath();

            Log.i("BOBS", polyline_nodes.size() + " nodes");
        } catch (Exception e) {
            Log.e("BOBS", "geo api", e);
        }

        return polyline_nodes;
    }

//    public void DownloadMapImage() {
//        Log.i("BOBS", "Entered DownloadMapImage()");
//        if (mapImageURI == null) {
//            Log.e("BOBS", "Wanted to download map image but the URI is null.");
//            return;
//        }
//
//        Log.i("BOBS", "Will download image from " + mapImageURI);
//
//        try {
//            mapImage = new SerialBitmap((new DownloadImageTask()).execute(mapImageURI.toString())
//                    .get());
//        } catch (Exception e) {
//            Log.e("BOBS", e.toString());
//            e.printStackTrace();
//        }
//
//        if (mapImage == null) {
//            Log.e("BOBS", "Downloaded map image but it is still null!!!");
//        }
//    }

    @Override
    public String toString() {
        return this.name;
    }

//    public Optional<URI> getMapImageURI() {
//        if (this.mapImageURI == null) {
//            return Optional.empty();
//        }
//        return Optional.of(mapImageURI);
//    }

//    public Optional<Bitmap> getMapImage() {
//        if (this.mapImage == null) {
//            return Optional.empty();
//        }
//        return Optional.of(mapImage.bitmap);
//    }

//    public void setMapImageURI(URI x) {
//        this.mapImageURI = x;
//    }

    enum TransitType implements Serializable {
        BUS, MINIBUS, TRAM, TROLLEYBUS;

        public String toString() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    // https://stackoverflow.com/questions/5776851/load-image-from-url
//    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
//
//        protected Bitmap doInBackground(String... urls) {
//            String urldisplay = urls[0];
//            Bitmap img = null;
//            try {
//                InputStream in = new java.net.URL(urldisplay).openStream();
//                img = BitmapFactory.decodeStream(in);
//            } catch (Exception e) {
//                Log.e("BOBS", e.getMessage());
//                e.printStackTrace();
//            }
//            return img;
//        }
//    }

}
