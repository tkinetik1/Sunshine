package sunshine.android.tkinetik.net.sunshine;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
                FetchWeatherTask weatherTask = new FetchWeatherTask();
                weatherTask.execute("95112");
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //Create an ArrayList of strings to show.
        String[] forecastArrayData = {
                "Today - Raining Cats - 88/64",
                "Tomorrow - Dropping Frogs - 70/46",
                "Monday - Sexy Noises like Thunder - 77/54",
                "Tuesday - Water Fall Splashes - 72/49",
                "Wednesday - Overcast - 92/77",
                "Thursday - Rainy - 89/55",
                "Tuesday - Sunny - 72/49"
        };
        List<String> weekForecast = new ArrayList<String>(Arrays.asList(forecastArrayData));

        // The ArrayAdapter will take data from a source and use it to populate the ListView it's attached to. 4 parameters
        mForecastAdapter =
                new ArrayAdapter<String>(
                    getActivity(),  // < ---- The context functionality of ArrayAdapter and its contained activity.
                    R.layout.list_item_forecast,  // name of layout id.
                    R.id.list_item_forecast_textview, // The ID of the textview to populate in list_item_forecast as ID.
                    weekForecast);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        ListView listView = (ListView) rootView.findViewById(R.id.listview_forecast);
        listView.setAdapter(mForecastAdapter);

        return rootView;
        }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]>
            {

            private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

                 /* The date/time conversion code is going to be moved outside the asynctask later,
                 * so for convenience we're breaking it out into its own method now.
                 */
                 private String getReadableDateString(long time){
                    // Because the API returns a unix timestamp (measured in seconds),
                    // it must be converted to milliseconds in order to be converted to valid date.
                    Date date = new Date(time * 1000);
                    SimpleDateFormat format = new SimpleDateFormat("E, MMM d");
                    return format.format(date).toString();
                }

                /**
                * Prepare the weather high/lows for presentation.
                */
                private String formatHighLows(double high, double low) {
                    // For presentation, assume the user doesn't care about tenths of a degree.
                    long roundedHigh = Math.round(high);
                    long roundedLow = Math.round(low);

                    String highLowStr = roundedHigh + "/" + roundedLow;
                    return highLowStr;
                }

           /**
 +         * Take the String representing the complete forecast in JSON Format and
 +         * pull out the data we need to construct the Strings needed for the wireframes.
 +         *
 +         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
 +         * into an Object hierarchy for us.
 +         */
                 private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                         throws JSONException

                {

                     // These are the names of the JSON objects that need to be extracted.
                     final String OWM_LIST = "list";
                     final String OWM_WEATHER = "weather";
                     final String OWM_TEMPERATURE = "temp";
                     final String OWM_MAX = "max";
                     final String OWM_MIN = "min";
                     final String OWM_DATETIME = "dt";
                     final String OWM_DESCRIPTION = "main";

                    JSONObject forecastJson = new JSONObject(forecastJsonStr);
                    JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

                     String[] resultStrs = new String[numDays];
                     for(int i = 0; i < weatherArray.length(); i++) {
                         // For now, using the format "Day, description, hi/low"
                         String day;
                         String description;
                         String highAndLow;

                         // Get the JSON object representing the day
                         JSONObject dayForecast = weatherArray.getJSONObject(i);

                         // The date/time is returned as a long.  We need to convert that
                         // into something human-readable, since most people won't read "1400356800" as
                         // "this saturday".
                         long dateTime = dayForecast.getLong(OWM_DATETIME);
                         day = getReadableDateString(dateTime);

                         // description is in a child array called "weather", which is 1 element long.
                         JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                         description = weatherObject.getString(OWM_DESCRIPTION);

                         // Temperatures are in a child object called "temp".  Try not to name variables
                         // "temp" when working with temperature.  It confuses everybody.
                         JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                         double high = temperatureObject.getDouble(OWM_MAX);
                         double low = temperatureObject.getDouble(OWM_MIN);

                         highAndLow = formatHighLows(high, low);
                         resultStrs[i] = day + " - " + description + " - " + highAndLow;
                     }

                     for (String s : resultStrs) {
                         Log.v(LOG_TAG, "Forecast entry: " + s);
                     }
                     return resultStrs;

                 }
                @Override
                protected String[] doInBackground(String... params) {

                    // Without a zip code, we have nothing to look up.  Verify against length of 0
                    if (params.length == 0) {
                        return null;
                    }

                    // declared variable outside of the try / catch in order to be closed to the final block.
                    HttpURLConnection urlConnection = null;
                    BufferedReader reader = null;

                    //String contains the JSON response.
                    String forecastJsonStr = null;

                    String format = "json";
                    String units = "metric";
                    int numDays = 7;

                    try
                    {


                        //construct the URL for the query to openweathermap.org.  Followed by opening the connection
                        // possible parameters are available at OWM's forecast API page, http://openweathermap.org/API#forecast

                        final String FORECAST_BASE_URL =
                                "http://api.openweathermap.org/data/2.5/forecast/daily?";
                        final String QUERY_PARAM = "q";
                        final String FORMAT_PARAM = "mode";
                        final String UNITS_PARAM = "units";
                        final String DAYS_PARAM = "cnt";

                        Uri builtURI = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                .appendQueryParameter(QUERY_PARAM, params[0])
                                .appendQueryParameter(FORMAT_PARAM, format)
                                .appendQueryParameter(UNITS_PARAM, units)
                                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                                .build();

                        URL url = new URL(builtURI.toString());

                        Log.v(LOG_TAG, "Built URI " + builtURI.toString());

                        // Create the request to OpenWeatherMap and then open connection.
                        urlConnection = (HttpURLConnection) url.openConnection();
                        urlConnection.setRequestMethod("GET");
                        urlConnection.connect();

                        // Read the input stream into a String
                        InputStream inputStream = urlConnection.getInputStream();
                        StringBuffer buffer = new StringBuffer();
                        if (inputStream == null) {
                            // no stream, nothing to do.
                            return null;
                        }
                        reader = new BufferedReader(new InputStreamReader(inputStream));

                        String line;
                        while ((line = reader.readLine()) != null) {
                            // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                            // But it does make debugging a *lot* easier if you print out the completed
                            // buffer for debugging.
                            buffer.append(line + "\n");
                        }
                        if (buffer.length() == 0) {
                            // Stream was empty.  No point in parsing.

                            return null;
                        }
                        forecastJsonStr = buffer.toString();

                        //Log the string
                        Log.v(LOG_TAG, "Forecast String: " + forecastJsonStr);
                    } catch (IOException e) {
                        Log.e("Error", "Error ", e);
                        // If the code didn't successfully get the weather data, there's no point in attemping
                        // to parse it.
                        return null;
                    } finally{
                        if (urlConnection != null) {
                            urlConnection.disconnect();
                        }
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (final IOException e) {
                                Log.e(LOG_TAG, "Error closing stream", e);
                            }
                        }
                    }

                    try {
                        return getWeatherDataFromJson(forecastJsonStr, numDays);
                    } catch (JSONException e) {
                        Log.e(LOG_TAG, e.getMessage(), e);
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void onPostExecute(String[] result) {
                    if (result != null) {
                        mForecastAdapter.clear();
                        for (String dayForecastStr : result) {
                            mForecastAdapter.add(dayForecastStr);
                        }
                    }
                }
            }
}