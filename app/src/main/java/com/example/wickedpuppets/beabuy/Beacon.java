package com.example.wickedpuppets.beabuy;

/**
 * Created by michalosak on 22/04/2017.
 */

public class Beacon {

    String beaconId;
    String user;

    public Beacon(){

    }

    public Beacon(String beaconId, String user) {
        this.beaconId = beaconId;
        this.user = user;
    }

    public String getBeaconId() {
        return beaconId;
    }

    public void setBeaconId(String beaconId) {
        this.beaconId = beaconId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
