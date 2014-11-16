package com.c13.devin.mighttherebeice;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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
                            .build();

                    RequestManager service = restAdapter.create(RequestManager.class);

                    subscription = service.getData(location, Keys.getWeatherApiKey())
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
                                    String weather = "Location = " + apiData.getData().getRequest().get(0).getQuery() + "\n";
                                    weather = weather + "Date = " + apiData.getData().getWeather().get(0).getDate() + "\n";
                                    List<Hourly> hourly = apiData.getData().getWeather().get(0).getHourly();
                                    for (int i = 0; i < hourly.size(); i++) {
                                        weather = weather + hourly.get(i).getTime() + " : " + hourly.get(i).getTempC() + " °C\n";
                                    }

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
