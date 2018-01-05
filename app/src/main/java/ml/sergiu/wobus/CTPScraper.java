package ml.sergiu.wobus;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class CTPScraper {
    private static CTPScraper instance = null;
    private final URI baseURI;
    private final List<TransitLine> transitLines;
    private final Map<String, TransitLine> transitLinesMap;


    private CTPScraper() {
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
    }

    public static CTPScraper getInstance() {
        if (instance == null) {
            instance = new CTPScraper();
        }
        return instance;
    }

    public void Scrape() {
        try {
            Log.i("BOBS", "Opening doc = \"" + baseURI + "\".");
            Document doc = Jsoup.connect(baseURI.toString()).get();
            Log.i("BOBS", "Doc opened.");
            Elements lines = doc.select("a[href^=/index.php/en/timetables/urban-lines/lin]");
            lines.stream().limit(10).forEach(line -> {
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

            ScrapeOrar(name, uri);

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

    public void ScrapeOrar(String line_name, URI page_uri) {
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
        transitLines.add(transitLine);
        transitLinesMap.put(transitLine.name, transitLine);
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
