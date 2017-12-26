package ml.sergiu.wobus;

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
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    public static final CTPScraper ctpScraper = CTPScraper.getInstance();
    TabsPagerAdapter pagerAdapter;
//    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

    public static class TransitDetailsFragment extends Fragment {
        public static TransitDetailsFragment newInstance(TransitLine line) {
            TransitDetailsFragment f = new TransitDetailsFragment();
            // Supply index input as an argument.
            Bundle args = new Bundle();
            args.putString("name", line.name);
            f.setArguments(args);
            return f;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.transit_details_fragment, container, false);

            Bundle args = getArguments();
            String name = args.getString("name");

            TextView line_number_label = (TextView) view.findViewById(R.id.line_number_label);
            line_number_label.setText(name);

            return view;
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
