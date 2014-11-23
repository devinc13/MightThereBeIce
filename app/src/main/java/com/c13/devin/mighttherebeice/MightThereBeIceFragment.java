package com.c13.devin.mighttherebeice;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import DataObjects.ApiData;
import DataObjects.Hourly;
import retrofit.RestAdapter;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MightThereBeIceFragment extends Fragment {

    private TextView resultsTextView;
    private TextView detailsTextView;
    private Button detailsButton;
    private Button hideDetailsButton;
    private CardView detailsCardView;
    private Subscription subscription;
    private OnChangeLocationSelectedListener onChangeLocationSelectedListener;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_might_there_be_ice, container, false);
        resultsTextView = (TextView) view.findViewById(R.id.text_view_results);
        detailsTextView = (TextView) view.findViewById(R.id.text_view_details);
        detailsButton = (Button) view.findViewById(R.id.button_details);
        hideDetailsButton = (Button) view.findViewById(R.id.button_hide_details);
        detailsCardView = (CardView) view.findViewById(R.id.card_view_details);
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
            setupContent();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupContent();
    }

    private void setupContent() {
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsCardView.setVisibility(View.VISIBLE);
            }
        });

        hideDetailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailsCardView.setVisibility(View.GONE);
            }
        });

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

        subscription = service.getData(location, Keys.getWeatherApiKey(), startDate, endDate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<ApiData>() {
                    @Override
                    public void onCompleted() {
                        if (subscription != null && !subscription.isUnsubscribed()) {
                            subscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                        if (subscription != null && !subscription.isUnsubscribed()) {
                            subscription.unsubscribe();
                        }
                    }

                    @Override
                    public void onNext(ApiData apiData) {
                        Boolean below0 = false;
                        Boolean precipitation = false;
                        String weather = "Location = " + apiData.getData().getRequest().get(0).getQuery() + "\n";
                        Calendar c = Calendar.getInstance();
                        int hour = c.get(Calendar.HOUR_OF_DAY) * 100;
                        int count = 0;

                        for (int i = 1; i >= 0; i--) {
                            List<Hourly> hourly = apiData.getData().getWeather().get(i).getHourly();
                            for (int j = hourly.size() - 1; j >= 0; j--) {
                                // Check if the time is before the current time, or if we have passed into the previous day
                                if (Integer.valueOf(hourly.get(j).getTime()) < hour || i == 0) {
                                    if (count > 8) {
                                        break;
                                    }

                                    count++;

                                    if (Integer.valueOf(hourly.get(j).getTempC()) < 0.0) {
                                        below0 = true;
                                    }

                                    if (Float.valueOf(hourly.get(j).getPrecipMM()) > 1.0) {
                                        precipitation = true;
                                    }
                                    weather = weather + "Date = " + apiData.getData().getWeather().get(i).getDate() + " Time = " + hourly.get(j).getTime() + " Temp = " + hourly.get(j).getTempC() + "°C Precipitation = " + hourly.get(j).getPrecipMM() + " mm\n";
                                }
                            }
                        }

                        detailsTextView.setText(weather);

                        String severity;
                        String reason;
                        if (below0 && precipitation) {
                            severity = "High";
                            reason = "There was precipitation and the temperature was below 0 degrees Celsius in the last 24 hours.";
                        } else if (below0) {
                            severity = "Medium";
                            reason = "The temperature was below 0 degrees Celsius in the last 24 hours.";
                        } else {
                            severity = "Low";
                            reason = "The temperature hasn't been below 0 degrees Celsius in the last 24 hours.";
                        }

                        String result = "There is a " + severity + " chance of ice.\n" + reason;

                        resultsTextView.setText(result);
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
