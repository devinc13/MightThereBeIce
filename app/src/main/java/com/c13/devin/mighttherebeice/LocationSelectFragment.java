package com.c13.devin.mighttherebeice;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import LocationDataObjects.LocationApiData;
import LocationDataObjects.Result;
import retrofit.RestAdapter;
import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class LocationSelectFragment extends Fragment {

    public static final String LOCATION = "location";
    private ArrayList<String> locations;

    private OnLocationSelectedListener onLocationSelectedListener;
    private Subscription subscription;

    public LocationSelectFragment() {
    }

    public interface OnLocationSelectedListener {
        public void onLocationSelected(String location);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            onLocationSelectedListener = (OnLocationSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLocationSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_location_select, container, false);

        final EditText editText = (EditText) view.findViewById(R.id.location_edit_text);
        final ListView listView = (ListView) view.findViewById(R.id.location_list);

        locations = new ArrayList<String>();

        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.list_view_text_view, locations);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                String location = (String) listView.getItemAtPosition(position);
                SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(LOCATION, location);
                editor.apply();

                onLocationSelectedListener.onLocationSelected(location);
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

        final RequestManager service = restAdapter.create(RequestManager.class);

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getText().length() >= 3) {
                    String location = editText.getText().toString();

                    if (subscription == null || subscription.isUnsubscribed()) {
                        subscription = service.getLocation(location, Keys.getWeatherApiKey())
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<LocationApiData>() {
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
                                    public void onNext(LocationApiData locationApiData) {
                                        adapter.clear();
                                        List<Result> results = locationApiData.getSearchApi().getResult();
                                        for (Result result : results) {
                                            adapter.add(result.getAreaName().get(0).getValue());
                                        }

                                        adapter.notifyDataSetChanged();
                                    }
                                });
                    }
                } else {
                    adapter.clear();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.might_there_be_ice);
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
