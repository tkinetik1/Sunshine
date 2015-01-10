package sunshine.android.tkinetik.net.sunshine;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

//import android.widget.Toast;

//import android.widget.Toast;

public class ForecastFragment extends Fragment {

    private ArrayAdapter<String> mForecastAdapter;

    public ForecastFragment() {
    }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.forecastfragment, menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            // Handle action bar item clicks here. The action bar will
            // automatically handle clicks on the Home/Up button, so long
            // as you specify a parent activity in AndroidManifest.xml.
            int id = item.getItemId();
            if (id == R.id.action_refresh) {
                updateWeather();
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Everything commented out is the source for the previous mock data.
        //Create an ArrayList of strings to show.
/*        String[] forecastArrayData = {
                "Mon - Light Rain - 31/17",
                "Tue - Fog - 21/8",
                "Wed - Sunny - 18/11",
                "Thur - Sunny - 22/16",
                "Fri - Rain - 23/14",
                "Sat - Light Rain - 18/8",
                "Sun - Overcast - 18/9"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArrayData));*/

        // The ArrayAdapter will take data from a source and use it to populate the ListView it's attached to. 4 parameters
        mForecastAdapter =
                new ArrayAdapter<>(
                    getActivity(), // The current context (this activity)
                    R.layout.list_item_forecast,  // name of layout id.
                    R.id.list_item_forecast_textview, // The ID of the textview to populate in list_item_forecast as ID.
                    new ArrayList<String>());

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        //Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                //Toast.makeText(getActivity(), forecast, Toast.LENGTH_SHORT).show();
                //Creating a new Explicit Intent to the DetailActivity class.  with putExtra as a key value pair in the intent for forecast text.  Any string for key.
                String forecast = mForecastAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, forecast);
                startActivity(intent);

            }
        });

        return rootView;
        }

    // Refactor updateWeather to a helper method.
    private void updateWeather() {
        String location = Utility.getPreferredLocation(getActivity());
        new FetchWeatherTask(getActivity(), mForecastAdapter).execute(location);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }
}