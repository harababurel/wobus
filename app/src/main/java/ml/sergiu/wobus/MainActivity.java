package ml.sergiu.wobus;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public CTPScraper ctpScraper;
    TabsPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ctpScraper = CTPScraper.getInstance(getApplicationContext());

        Button scrape_btn = (Button) findViewById(R.id.scrape_button);
        scrape_btn.setOnClickListener(this);

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
    }

    public void onClick(View view) {
        // detect the view that was "clicked"
        switch (view.getId()) {
            case R.id.scrape_button:
                new ScrapeOperation().execute();
                break;
        }
    }

    //    /** Called when the user taps the "Show on map" button */
    public void showMap(View view) {
        Intent intent = new Intent(this, MapsActivity.class);

        ViewPager pager = (ViewPager) findViewById(R.id.viewPager);

        TransitLine current_line = ctpScraper.busLines().get(pager.getCurrentItem());
        Log.i("BOBS", "Currently selected transit line: " + current_line.toString());

        Date now = Calendar.getInstance().getTime();
        Date closest_prev_time_a = null;
        Date closest_prev_time_b = null;

        for (Date x : current_line.departuresA) {
            if (x.compareTo(now) <= 0) {
                closest_prev_time_a = x;
            }
        }

        for (Date x : current_line.departuresB) {
            if (x.compareTo(now) <= 0) {
                closest_prev_time_b = x;
            }
        }
        intent.putExtra("current_line", current_line);
        intent.putExtra("closest_prev_time_a", closest_prev_time_a);
        intent.putExtra("closest_prev_time_b", closest_prev_time_b);

        startActivity(intent);
    }

    public static class TransitDetailsFragment extends Fragment {
        public static TransitDetailsFragment newInstance(TransitLine line) {
            TransitDetailsFragment f = new TransitDetailsFragment();
            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putSerializable("current_line", line);
            f.setArguments(args);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.transit_details_fragment, container, false);

            Bundle args = getArguments();

            TransitLine current_line = (TransitLine) args.getSerializable("current_line");
            ImageView map_image = view.findViewById(R.id.map_image);


//            if (!current_line.getMapImage().isPresent()) {
//                current_line.DownloadMapImage();
//            }

//            if (current_line.getMapImage().isPresent()) {
//                map_image.setImageBitmap(current_line.getMapImage().get());
//            }

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap;
            try {
                bitmap = BitmapFactory.decodeStream(
                        getContext().getAssets().open(current_line.mapImagePath()));
                map_image.setImageBitmap(bitmap);
            } catch (Exception e) {
                Log.e("BOBS", "map image path", e);
            }


            TextView line_number_label = (TextView) view.findViewById(R.id.line_number_label);
            line_number_label.setText(current_line.name);


            TextView line_ends_label = (TextView) view.findViewById(R.id.line_ends_label);
            line_ends_label.setText(current_line.endA().name + " - " + current_line.endB().name);

            return view;
        }
    }

    // https://stackoverflow.com/questions/5776851/load-image-from-url
    private static class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setImageBitmap(result);
        }
    }


    public class TabsPagerAdapter extends FragmentPagerAdapter {
        private List<Fragment> fragments = new ArrayList<>();
        private List<String> tabTitles = new ArrayList<>();

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles.get(position);
        }

        public void addFragment(Fragment fragment, String tabTitle) {
            fragments.add(fragment);
            tabTitles.add(tabTitle);
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
//            EditText scraperDebugText = (EditText) findViewById(R.id.scraperDebugText);
//            scraperDebugText.setText("Done.");
            Log.i("BOBS", "Creating new fragments");

            ctpScraper.busLines().forEach(transitLine -> {
                        pagerAdapter.addFragment(TransitDetailsFragment.newInstance(transitLine),
                                transitLine.name);
                    }
            );
            pagerAdapter.notifyDataSetChanged();
            Log.i("BOBS", "Done creating fragments");

            Button show_on_map = (Button) findViewById(R.id.showOnMapButton);
            show_on_map.setEnabled(true);
        }

        @Override
        protected void onPreExecute() {
//            EditText scraperDebugText = (EditText) findViewById(R.id.scraperDebugText);
//            scraperDebugText.setText("Scraping CTP...");
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }
    }
}
