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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class LocationSelectFragment extends Fragment {

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

                    RequestQueue queue = Volley.newRequestQueue(getActivity());
                    String url = "http://api.worldweatheronline.com/free/v2/past-weather.ashx?q="
                            + location
                            + "&format=json&extra=localObsTime%2CisDayTime&date=today&key=7ce966b5e2e278e92e32afaa8a1c2";

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                            new Response.Listener() {
                                @Override
                                public void onResponse(Object response) {
                                    textView.setText(response.toString());
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            textView.setText("That didn't work!");
                        }
                    });

                    queue.add(stringRequest);
                }
            }
        });

        return view;
    }
}
