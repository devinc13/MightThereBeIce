package com.c13.devin.mighttherebeice;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.List;

import DataObjects.ApiData;
import DataObjects.Hourly;
import retrofit.RestAdapter;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LocationSelectFragment extends Fragment {

    private Subscription subscription;

    public LocationSelectFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_select, container, false);

        Button button = (Button) view.findViewById(R.id.submit);
        final EditText editText = (EditText) view.findViewById(R.id.location);
        final TextView textView = (TextView) view.findViewById(R.id.response);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String location = editText.getText().toString();
                if (location.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.error_empty_location, Toast.LENGTH_SHORT).show();
                } else {
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
                                        //weather = weather + "Date = " + apiData.getData().getWeather().get(i).getDate() + "\n";
                                        List<Hourly> hourly = apiData.getData().getWeather().get(i).getHourly();
                                        for (int j = hourly.size() - 1; j >= 0; j--) {
                                            // Check if the time is before the current time, or if we have passed into the previous day
                                            if (Integer.valueOf(hourly.get(j).getTime())> hour || i == 0) {
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

                                    weather = weather + "\n \nThere is a " + severity + " chance of ice.\n" + reason;

                                    textView.setText(weather);
                                }
                            });
                }
            }
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
