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
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CTPScraper {
    private static CTPScraper instance = null;
    private final URI baseURI;
    private final Map<String, BusLine> busLines;


    private CTPScraper() {
        // exists only to defeat instantiation.

        URI tmpURI = null;
        try {
            tmpURI = new URI("http://ctpcj.ro/index.php/en/timetables/urban-lines/");
        } catch (Exception e) {
            Log.e("BOBS", "Could not create base URL");
        }
        baseURI = tmpURI;
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
            Log.i("BOBS", "Opening doc = \"" + baseURI + "\".");
            Document doc = new GetDocumentTask().execute(baseURI).get();
            Log.i("BOBS", "Doc opened.");
            Elements lines = doc.select("a[href^=/index.php/en/timetables/urban-lines/lin]");
            lines.stream().forEach(line -> {
                URI busLineURI = baseURI.resolve(line.attr("href"));
                Log.i("BOBS", line.text());
                Log.i("BOBS", busLineURI.toString());
                AddOrUpdateBusLine(new BusLine(line.text(), busLineURI));
            });
        } catch (Exception e) {
            Log.e("BOBS", "could not scrape" + baseURI);
        }

        Log.i("BOBS", "finished Scrape()");
    }

    public String ScrapeBusPage(URI uri) {
        try {
            Document doc = new GetDocumentTask().execute(uri).get();
            return doc.text();
        } catch (Exception e) {
            Log.e("BOBS", "could not scrape" + uri);
        }
        return "";
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

    class GetDocumentTask extends AsyncTask<URI, Void, Document> {
        private Exception exception;

        protected Document doInBackground(URI... uris) {
            try {
                return Jsoup.connect(uris[0].toString()).get();
            } catch (NullPointerException e) {
                Log.e("BOBS", "Connection is null");
                this.exception = e;
            } catch (MalformedURLException e) {
                Log.e("BOBS", "Malformed URL");
                this.exception = e;
            } catch (HttpStatusException e) {
                Log.e("BOBS", "Response not OK");
                this.exception = e;
            } catch (UnsupportedMimeTypeException e) {
                Log.e("BOBS", "Unsupported MIME type");
                this.exception = e;
            } catch (SocketTimeoutException e) {
                Log.e("BOBS", "Socket timeout");
                this.exception = e;
            } catch (IOException e) {
                Log.e("BOBS", "IO Exception");
                this.exception = e;
            }
            return null;
        }

    }
}
