package ml.sergiu.wobus;

import android.os.AsyncTask;
import android.util.Log;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CTPScraper {
    private static final String baseURL = "http://ctpcj.ro/index.php/en/timetables/urban-lines/";
    private static CTPScraper instance = null;
    private final Map<String, BusLine> busLines;


    private CTPScraper() {
        // exists only to defeat instantiation.
        busLines = new HashMap<>();
    }

    public static CTPScraper getInstance() {
        if (instance == null) {
            instance = new CTPScraper();
        }
        return instance;
    }

    public void Scrape() {
        try {
            Document doc = new GetDocumentTask().execute(baseURL).get();
            Elements lines = doc.select("a[href^=/index.php/en/timetables/urban-lines/lin]");
            lines.stream().forEach(line -> {
                Log.i("LINE", line.text());
                AddOrUpdateBusLine(new BusLine(line.text(), BusLine.BusType.BUS));
            });
        }
        catch(Exception e) {
            Log.e("Scraper", "could not scrape");
        }
    }

    public void AddOrUpdateBusLine(BusLine busLine) {
        busLines.put(busLine.name, busLine);
    }

    public Optional<BusLine> getBusLine(String name) {
        if (busLines.containsKey(name)) {
            return Optional.of(busLines.get(name));
        }
        return Optional.empty();
    }

    public Collection<BusLine> busLines() {
        return busLines.values();
    }

class GetDocumentTask extends AsyncTask<String, Void, Document> {
    private Exception exception;

    protected Document doInBackground(String... urls) {
        try {
            return Jsoup.connect(urls[0]).get();
        } catch (NullPointerException e) {
            this.exception = e;
            Log.e("SCRAPER", "Connection is null");
        } catch (MalformedURLException e) {
            this.exception = e;
            Log.e("SCRAPER", "Malformed URL");
        } catch (HttpStatusException e) {
            this.exception = e;
            Log.e("SCRAPER", "Response not OK");
        } catch (UnsupportedMimeTypeException e) {
            this.exception = e;
            Log.e("SCRAPER", "Unsupported MIME type");
        } catch (SocketTimeoutException e) {
            this.exception = e;
            Log.e("SCRAPER", "Socket timeout");
        } catch (IOException e) {
            this.exception = e;
            Log.e("SCRAPER", "IO Exception");
        }
        return null;
    }

}
}
