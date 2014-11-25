package com.c13.devin.mighttherebeice;

import DataObjects.ApiData;
import LocationDataObjects.LocationApiData;
import retrofit.http.GET;
import retrofit.http.Query;
import rx.Observable;

public interface RequestManager {
    @GET("/free/v2/past-weather.ashx?format=json&extra=localObsTime")
    Observable<ApiData> getData(@Query("q") String location, @Query("key") String key, @Query("date") String date, @Query("enddate") String enddate);

    @GET("/free/v2/search.ashx?format=json")
    Observable<LocationApiData> getLocation(@Query("q") String location, @Query("key") String key);
}

