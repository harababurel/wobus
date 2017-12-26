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
            lines.stream().forEach(line -> {
                URI busLineURI = baseURI.resolve(line.attr("href"));
                Log.i("BOBS", line.text());
                Log.i("BOBS", busLineURI.toString());
                AddOrUpdateBusLine(new TransitLine(line.text(), busLineURI));
            });
        } catch (Exception e) {
            Log.e("BOBS", "could not scrape" + baseURI);
        }
    }

    public String ScrapeBusPage(URI uri) {
        try {
            Document doc = Jsoup.connect(uri.toString()).get();
            return doc.text();
        } catch (Exception e) {
            Log.e("BOBS", "could not scrape" + uri);
        }
        return "";
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
