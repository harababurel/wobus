package ml.sergiu.wobus;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CTPScraper {
    private static CTPScraper instance = null;
    private final URI baseURI;
    private final Map<String, TransitLine> transitLines;


    private CTPScraper() {
        // private in order to defeat instantiation.

        URI tmpURI = null;
        try {
            tmpURI = new URI("http://ctpcj.ro/index.php/en/timetables/urban-lines/");
        } catch (Exception e) {
            Log.e("BOBS", "Could not create base URL");
        }
        baseURI = tmpURI;
        transitLines = new HashMap<>();
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
            lines.stream().limit(5).forEach(line -> {
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

            try {
                String mapImageURI = doc.select("a[href^=/orare/harta]").first().attr("href").toString();
                ret.get().mapImageURI = Optional.of(new URI("http://ctpcj.ro" + mapImageURI));
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

    public void AddOrUpdateBusLine(TransitLine transitLine) {
        transitLines.put(transitLine.name, transitLine);
    }

    public Optional<TransitLine> getBusLine(String name) {
        if (transitLines.containsKey(name)) {
            return Optional.of(transitLines.get(name));
        }
        return Optional.empty();
    }

    public Collection<TransitLine> busLines() {
        return transitLines.values();
    }
}
