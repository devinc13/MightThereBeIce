package com.c13.devin.mighttherebeice;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import java.util.ArrayList;

public class LocationSelectFragment extends Fragment {

    public static final String LOCATION = "location";
    private ArrayList<String> locations;

    private OnLocationSelectedListener onLocationSelectedListener;

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

                String location = (String) listView.getItemAtPosition(position);
                SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                editor.putString(LOCATION, location);
                editor.apply();

                onLocationSelectedListener.onLocationSelected(location);
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (editText.getText().length() >= 3) {
                    String location = editText.getText().toString();

                    adapter.clear();
                    adapter.add(location);
                    adapter.notifyDataSetChanged();
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
}
