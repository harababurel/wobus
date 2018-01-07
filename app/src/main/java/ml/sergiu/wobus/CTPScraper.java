package ml.sergiu.wobus;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.google.maps.model.LatLng;
import com.opencsv.CSVReader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CTPScraper {
    private static CTPScraper instance = null;
    private final URI baseURI;
    private final List<TransitLine> transitLines;
    private final Map<String, TransitLine> transitLinesMap;
    private final Map<String, TransitStop> transitStopsMap;
    private final AssetManager assetManager;


    private CTPScraper(Context ctx) {
        // private in order to defeat instantiation.

        URI tmpURI = null;
        try {
            tmpURI = new URI("http://ctpcj.ro/index.php/en/timetables/urban-lines/");
        } catch (Exception e) {
            Log.e("BOBS", "Could not create base URL");
        }
        baseURI = tmpURI;
        transitLines = new ArrayList<>();
        transitLinesMap = new HashMap<>();
        transitStopsMap = new HashMap<>();

        assetManager = ctx.getAssets();

    }

    public static CTPScraper getInstance(Context ctx) {
        if (instance == null) {
            instance = new CTPScraper(ctx);
        }
        return instance;
    }

    public void Scrape() {
        LoadStops();
        try {
            Log.i("BOBS", "Opening doc = \"" + baseURI + "\".");
            Document doc = Jsoup.connect(baseURI.toString()).get();
            Log.i("BOBS", "Doc opened.");
            Elements lines = doc.select("a[href^=/index.php/en/timetables/urban-lines/lin]");

            lines.stream().filter(line -> line.text().contains("24B") || line.text().contains("25")).forEach(line -> {
                URI transitLineURI = baseURI.resolve(line.attr("href"));
                Log.i("BOBS", line.text());
                Log.i("BOBS", transitLineURI.toString());

                Optional<TransitLine> transitLine = ScrapeTransitLinePage(transitLineURI);

                if (transitLine.isPresent()) {
                    AddOrUpdateBusLine(transitLine.get());
                }
            });
        } catch (Exception e) {
            Log.e("BOBS", "could not scrape" + baseURI);
        }
    }

    public Optional<TransitLine> ScrapeTransitLinePage(URI uri) {
        Optional<TransitLine> ret = Optional.empty();

        try {
            Document doc = Jsoup.connect(uri.toString()).get();
            ret = Optional.of(new TransitLine("", null));
//            Log.i("BOBS", doc.html());

            String name = doc.select("h1[class^=TzArticleTitle]").first().text();
            ret.get().name = name;

            ScrapeOrar(name, uri, ret.get());
            LoadRoute(name);

            try {
                String mapImageURI = doc.select("a[href^=/orare/harta]").first().attr("href").toString();
                ret.get().setMapImageURI(new URI("http://ctpcj.ro" + mapImageURI));
            } catch (Exception e) {
                Log.i("BOBS", "Line " + uri.toString() + " has no map; skipping");
                Log.i("BOBS", "Exception: " + e);
            }
        } catch (Exception e) {
            Log.e("BOBS", "could not scrape " + uri);
            Log.e("BOBS", "reason: " + e);
        }

        return ret;
    }

    public void LoadStops() {
        CSVReader reader;
        try {
            reader = new CSVReader(new InputStreamReader(assetManager.open("stops.csv"),
                    "UTF-8"));
        } catch (Exception e) {
            Log.e("STOPS", "Could not open asset file: " + e.toString());
            return;
        }


        String[] s = null;
        while (true) {
            try {
                s = reader.readNext();
                if (s == null) {
                    break;
                }
            } catch (Exception e) {
                Log.e("STOPS", e.toString());
            }

            Log.i("STOPS", "Found stop \"" + s[0] + "\" with coordinates (" +
                    s[1] + ", " + s[2] + ").");

            try {
                String stop_name = s[0];
                double lat = Double.parseDouble(s[1]);
                double lng = Double.parseDouble(s[2]);
                transitStopsMap.put(stop_name, new TransitStop(stop_name, new LatLng(lat, lng)));
            } catch (Exception e) {
                Log.e("STOPS", "Could not parse stop line: " + e.toString());
            }
        }
    }

    public void LoadRoute(String line_name) {
        String line_number = line_name.split(" ")[1];
        BufferedReader reader = null;
        try {
            InputStreamReader strim = new InputStreamReader(assetManager.open("buses/" + line_number), "UTF-8");
            reader = new BufferedReader(strim);
        } catch (Exception e) {
            Log.e("ROUTE", e.toString());
            return;
        }

        String s = null;
        while (true) {
            try {
                s = reader.readLine();
            } catch (Exception e) {
                Log.e("ROUTE", e.toString());
            }
            if (s == null) {
                break;
            }

            Log.i("ROUTE", "Adding stop " + s + " to line " + line_number);
            getBusLine(line_name).get().routeAB.add(transitStopsMap.get(s));
        }

    }

    public void ScrapeOrar(String line_name, URI page_uri, TransitLine line) {
        String[] parts = line_name.split(" ");
        String line_number = parts[parts.length - 1];

        String today = (new SimpleDateFormat("E")).format(Calendar.getInstance().getTime());
        Log.i("ORAR", "today = " + today);

        switch (today) {
            case "Sat":
                today = "s";
                break;
            case "Sun":
                today = "d";
                break;
            default:
                today = "lv";
        }

        String orar_filename = "orare/orar_" + line_number + "_" + today + ".csv";
        try {
            InputStreamReader strim = new InputStreamReader(assetManager.open(orar_filename),
                    "UTF-8");

            BufferedReader reader = new BufferedReader(strim);

            String s;
            for (int i = 0; i < 5; i++) {
                reader.readLine();
            }

            DateFormat fmt = new SimpleDateFormat("hh:mm");
            while ((s = reader.readLine()) != null) {
                String[] times;
                try {
                    times = s.split(",");
                } catch (Exception e) {
                    Log.e("ORAR", "could not split orar line: \"" + s + "\"");
                    continue;
                }

                try {
                    line.departuresA.add(fmt.parse(times[0]));
                } catch (Exception e) {
                    Log.e("ORAR", "could not add departureA time = \"" + times[0] + "\": " + e
                            .toString());
                }

                try {
                    line.departuresB.add(fmt.parse(times[1]));
                } catch (Exception e) {
                    Log.e("ORAR", "could not add departureB time = \"" + times[1] + "\": " + e
                            .toString());
                }
            }
        } catch (Exception e) {
            Log.e("ORAR", "could not open asset \"" + orar_filename + "\"; reason: " + e
                    .toString());
        }


//        URL csv_uri;
//        try {
//            csv_uri = new URL("http://www.ctpcj.ro/orare/csv/orar_" + line_number + "_" + today +
//                    "" +
//                    ".csv");
//        } catch (MalformedURLException e) {
//            Log.e("ORAR", "could not construct csv_uri: " + e.toString());
//            return;
//        }
//
//        Log.d("ORAR", "URL is " + csv_uri.toString());
//
//        try {
//            HttpURLConnection connection = (HttpURLConnection) csv_uri.openConnection();
//            URLConnection con = (URLConnection) csv_uri.openConnection();
//
//            con.connect();
//
//
//            Log.d("ORAR", con.getContent().toString());


//            if (con.getResponseCode() == 200) {
//                try (InputStreamReader streamReader = new InputStreamReader(connection.getInputStream());
//                     BufferedReader br = new BufferedReader(streamReader);
//                     Stream<String> lines = br.lines()) {
//                    lines.forEach(s -> Log.i("ORAR", "read line: " + s));
//                } catch (Exception e) {
//                    Log.e("ORAR", "could not read orar from csv: " + e.toString());
//                }
//            } else {
//                Log.w("ORAR", "Response code is " + connection.getResponseCode());
//            }
//
//        } catch (Exception e) {
//            Log.e("ORAR", "could not open connection to " + csv_uri.toString());
//            Log.e("ORAR", "reason: " + e.toString());
//        }


//        try {
//            Document doc = Jsoup.connect(csv_uri.toString()).get();
//        } catch (Exception e) {
//            Log.e("ORAR", "could not get orar from " + csv_uri);
//            Log.e("ORAR", "reason: " + e.toString());
//        }
    }

    public void AddOrUpdateBusLine(TransitLine transitLine) {
        if (transitLinesMap.containsKey(transitLine.name)) {
            transitLines.set(transitLines.indexOf(transitLine), transitLine);
            transitLinesMap.put(transitLine.name, transitLine);
        } else {
            transitLines.add(transitLine);
            transitLinesMap.put(transitLine.name, transitLine);
        }
    }

    public Optional<TransitLine> getBusLine(String name) {
        if (transitLinesMap.containsKey(name)) {
            return Optional.of(transitLinesMap.get(name));
        }
        return Optional.empty();
    }

    public List<TransitLine> busLines() {
        return transitLines;
    }
}
