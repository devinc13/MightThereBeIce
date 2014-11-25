package com.c13.devin.mighttherebeice;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LocationSelectFragment extends Fragment {

    public static final String LOCATION = "location";

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

        Button button = (Button) view.findViewById(R.id.submit);
        final EditText editText = (EditText) view.findViewById(R.id.location);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String location = editText.getText().toString();
                if (location.isEmpty()) {
                    Toast.makeText(getActivity(), R.string.error_empty_location, Toast.LENGTH_SHORT).show();
                } else {
                    InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                    SharedPreferences.Editor editor = getActivity().getPreferences(Context.MODE_PRIVATE).edit();
                    editor.putString(LOCATION, location);
                    editor.apply();

                    onLocationSelectedListener.onLocationSelected(location);
                }
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
