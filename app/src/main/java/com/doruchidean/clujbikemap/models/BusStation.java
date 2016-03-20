package com.doruchidean.clujbikemap.models;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Doru on 11/03/16.
 *
 */
public class BusStation implements Comparable<BusStation>{

    private LatLng latLngLocation;
    private String name, type;
    private List<String> linii;
    private int id;

    public BusStation(
            int id, String name, double latitude, double longitude, String type, List<String> linii){

        this.latLngLocation = new LatLng(latitude, longitude);
        this.name = name;
        this.type = type;
        this.linii = linii;
        this.id = id;
    }

    public LatLng getLatLngLocation() {
        return latLngLocation;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public List<String> getLinii() {
        return linii;
    }

    public int getId() {
        return id;
    }

    @Override
    public int compareTo(BusStation another) {
        return this.name.compareToIgnoreCase(another.name);
    }
}
