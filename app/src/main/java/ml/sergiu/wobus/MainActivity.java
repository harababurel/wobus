package ml.sergiu.wobus;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final CTPScraper ctpScraper = CTPScraper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scrape_btn = (Button) findViewById(R.id.scrape_button);
        scrape_btn.setOnClickListener(this);


//        EditText scraperDebugText = (EditText) findViewById(R.id.scraperDebugText);
//        scraperDebugText.setText("Scraping CTP...");
//        ctpScraper.Scrape();
//        scraperDebugText.setText("Done");
    }

    public void onClick(View view) {
        // detect the view that was "clicked"
        switch (view.getId()) {
            case R.id.scrape_button:
                new ScrapeOperation().execute();
                break;
        }
    }

    private class ScrapeOperation extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ctpScraper.Scrape();
            Log.i("BOBS", "finished scraping");
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            EditText scraperDebugText = (EditText) findViewById(R.id.scraperDebugText);
            scraperDebugText.setText("Done.");
        }

        @Override
        protected void onPreExecute() {
            EditText scraperDebugText = (EditText) findViewById(R.id.scraperDebugText);
            scraperDebugText.setText("Scraping CTP...");
        }

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    public void showLocation(View view) {
        Intent intent = new Intent(this, MapsMarkerActivity.class);
        startActivity(intent);
        Log.e("DOG", "pula");
    }

//    public void showAllBusLines(View view) {
//        Intent intent = new Intent(this, BusDescriptionActivity.class);
//        startActivity(intent);
//    }
}
