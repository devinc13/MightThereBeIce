package com.c13.devin.mighttherebeice;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import DataObjects.ApiData;
import DataObjects.Hourly;
import retrofit.RestAdapter;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MightThereBeIceFragment extends Fragment {

    private TextView resultsTextView;
    private TableLayout detailsTable;
    private Button detailsButton;
    private CardView resultsCardView;
    private CardView detailsCardView;
    private Subscription subscription;
    private ApiData apiData = null;
    private boolean detailsHidden = true;
    private ProgressDialog progressDialog;
    private OnChangeLocationSelectedListener onChangeLocationSelectedListener;

    public static final int Celsius = 0;
    public static final int Fahrenheit = 1;


    public interface OnChangeLocationSelectedListener {
        public void onChangeLocationSelected();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onChangeLocationSelectedListener = (OnChangeLocationSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnChangeLocationSelectedListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_might_there_be_ice, container, false);
        resultsTextView = (TextView) view.findViewById(R.id.text_view_results);
        detailsTable = (TableLayout) view.findViewById(R.id.details_table);
        detailsButton = (Button) view.findViewById(R.id.button_details);
        detailsCardView = (CardView) view.findViewById(R.id.card_view_details);
        resultsCardView = (CardView) view.findViewById(R.id.card_view_results);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.might_there_be_ice_menu, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_change_location) {
            onChangeLocationSelectedListener.onChangeLocationSelected();
            return true;
        } else if (id == R.id.action_refresh) {
            getContent();
            return true;
        } else if (id == R.id.action_settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
           return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (apiData == null) {
            getContent();
            detailsCardView.setVisibility(View.GONE);
        } else {
            resultsCardView.setVisibility(View.VISIBLE);
            setupContent();
            if (detailsHidden) {
                detailsCardView.setVisibility(View.GONE);
            } else {
                detailsButton.setText(R.string.hide_details);
            }
        }
    }

    private void getContent() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://api.worldweatheronline.com")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new RestAdapter.Log() {
                    @Override
                    public void log(String msg) {
                        Log.i("ApiRequest", msg);
                    }
                })
                .build();

        RequestManager service = restAdapter.create(RequestManager.class);

        // Start at yesterday and end at today
        Calendar c = Calendar.getInstance();
        // Month is 0 indexed
        String endDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);
        c.add(Calendar.DATE, -1);
        String startDate = c.get(Calendar.YEAR) + "-" + (c.get(Calendar.MONTH) + 1) + "-" + c.get(Calendar.DATE);

        Bundle bundle = getArguments();
        String location = bundle.getString(LocationSelectFragment.LOCATION);

        if (location.isEmpty()) {
            return;
        }

        progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.checking_weather_data));
        subscription = service.getData(location, Keys.getWeatherApiKey(), startDate, endDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiData>() {
                    @Override
                    public void onCompleted() {
                        if (subscription != null && !subscription.isUnsubscribed()) {
                            subscription.unsubscribe();
                        }

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (subscription != null && !subscription.isUnsubscribed()) {
                            subscription.unsubscribe();
                        }

                        if (progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }
                        resultsCardView.setVisibility(View.GONE);

                        Toast.makeText(getActivity(), R.string.error_checking_weather_data, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(ApiData response) {
                        apiData = response;
                        resultsCardView.setVisibility(View.VISIBLE);
                        setupContent();
                    }
                });
    }

    private void setupContent() {
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailsCardView.getVisibility() == View.GONE) {
                    detailsButton.setText(R.string.hide_details);
                    detailsCardView.setVisibility(View.VISIBLE);
                    detailsHidden = false;
                } else {
                    detailsButton.setText(R.string.details);
                    detailsCardView.setVisibility(View.GONE);
                    detailsHidden = true;
                }
            }
        });

        Boolean below0 = false;
        Boolean precipitation = false;
        String location = apiData.getData().getRequest().get(0).getQuery();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(location);
        }

        Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY) * 100;
        int count = 0;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        int TemperatureUnits = Integer.valueOf(prefs.getString("temperature_units", "-1"));

        // Make sure we aren't adding to a previously filled in table
        detailsTable.removeAllViews();

        for (int i = 1; i >= 0; i--) {
            List<Hourly> hourly = apiData.getData().getWeather().get(i).getHourly();
            for (int j = hourly.size() - 1; j >= 0; j--) {
                // Check if the time is before the current time, or if we have passed into the previous day
                if (Integer.valueOf(hourly.get(j).getTime()) <= hour || i == 0) {
                    if (count > 8) {
                        break;
                    }

                    count++;

                    if (Integer.valueOf(hourly.get(j).getTempC()) < 0.0) {
                        below0 = true;
                    }

                    // Make sure there is a reasonable amount of precipitation, not just one rain drop
                    if (Float.valueOf(hourly.get(j).getPrecipMM()) > 0.1) {
                        precipitation = true;
                    }

                    TableRow tableRow = new TableRow(getActivity());
                    tableRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                    TextView textView = new TextView(getActivity());
                    textView.setText(apiData.getData().getWeather().get(i).getDate());
                    tableRow.addView(textView);
                    TextView textView2 = new TextView(getActivity());
                    textView2.setText(getTimeString(hourly.get(j).getTime()));
                    tableRow.addView(textView2);
                    TextView textView3 = new TextView(getActivity());

                    if (TemperatureUnits == Fahrenheit) {
                        textView3.setText(hourly.get(j).getTempF() + "°F");
                    } else if (TemperatureUnits == Celsius) {
                        textView3.setText(hourly.get(j).getTempC() + "°C");
                    } else {
                        // Default to Celsius
                        textView3.setText(hourly.get(j).getTempC() + "°C");
                    }

                    tableRow.addView(textView3);
                    TextView textView4 = new TextView(getActivity());
                    textView4.setText(hourly.get(j).getPrecipMM() + " mm precipitation");
                    tableRow.addView(textView4);
                    detailsTable.addView(tableRow, 0, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                }
            }
        }

        String severity;
        String reason;
        if (below0 && precipitation) {
            severity = "High";
            reason = "There was precipitation and the temperature was below freezing in the last 24 hours.";
        } else if (below0) {
            severity = "Medium";
            reason = "The temperature was below freezing in the last 24 hours.";
        } else {
            severity = "Low";
            reason = "The temperature hasn't been below freezing in the last 24 hours.";
        }

        int minute = c.get(Calendar.MINUTE);
        String result = "There is a " + severity + " chance of ice.\n" + reason + "\nLast updated on " + c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.CANADA) + " " + c.get(Calendar.DAY_OF_MONTH) + " at " + c.get(Calendar.HOUR_OF_DAY) + ":" + (minute < 10 ? "0" + minute : minute);
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(result);
        StyleSpan styleSpan = new StyleSpan(android.graphics.Typeface.BOLD);
        spannableStringBuilder.setSpan(styleSpan, 11, 11 + severity.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        resultsTextView.setText(spannableStringBuilder);
    }

    private String getTimeString(String time) {
        if ("0".equals(time)) {
            return "00:00";
        } else if (time.length() == 3) {
            return "0" + time.substring(0, 1) + ":00";
        } else {
            return time.substring(0, 2) + ":00";
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
