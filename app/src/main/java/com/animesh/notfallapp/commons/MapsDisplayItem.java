package com.animesh.notfallapp.commons;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.io.Serializable;


public class MapsDisplayItem implements ClusterItem {

    final String title;
    final LatLng latLng;
    final String address;
    final String phoneNumber;

    public MapsDisplayItem(String status, LatLng latLng, String address, String phoneNumber) {
        this.title = status;
        this.latLng = latLng;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public LatLng getPosition() {
        return latLng;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getAddress() {
        return address;
    }

}
