package com.animesh.notfallapp.commons;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;


public class MapsDisplayItems implements ClusterItem {

    final String title;
    final LatLng latLng;


    public MapsDisplayItems(String title, LatLng latLng) {
        this.title = title;
        this.latLng = latLng;
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

}
