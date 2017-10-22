package com.example.wickedpuppets.beabuy.interfaces;

import com.example.wickedpuppets.beabuy.Beacon;
import com.example.wickedpuppets.beabuy.User;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by pgurdek on 22.10.17.
 */

public interface RetroFitInterface {

    @POST("/users/beacons/")
    Call<JSONObject> postBeaconInfo(@Body Beacon beacon);

    @POST("/users/add/")
    Call<JSONObject> postUser(@Body User user);
}
