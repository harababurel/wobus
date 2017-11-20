package ml.sergiu.wobus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import org.jsoup.nodes.Document;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "ml.sergiu.wobus.MESSAGE";
    public static final CTPScraper ctpScraper = CTPScraper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EditText scraperDebugText = (EditText) findViewById(R.id.scraperDebugText);

        ctpScraper.Scrape();
//        scraperDebugText.setText(doc.text());
    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editText = (EditText) findViewById(R.id.editText);
        String message = editText.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }

    public void showLocation(View view) {
        Intent intent = new Intent(this, MapsMarkerActivity.class);
        startActivity(intent);
        Log.e("DOG", "pula");
    }

    public void showAllBusLines(View view) {
        Intent intent = new Intent(this, BusDescriptionActivity.class);
        startActivity(intent);
    }
}
