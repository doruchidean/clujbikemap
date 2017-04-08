package com.doruchidean.clujbikemap.models;

/**
 * Created by Doru on 27/03/2017.
 *
 */

public class BusSchedule {

    private String
            name,
            nameCapat1,
            nameCapat2,
            apiEndpoint,
            plecariCapat1LV,
            plecariCapat1S,
            plecariCapat1D,
            plecariCapat2LV,
            plecariCapat2S,
            plecariCapat2D;

    public String getPlecariCapat2D() {
        return plecariCapat2D;
    }

    public void setPlecariCapat2D(String plecariCapat2D) {
        this.plecariCapat2D = plecariCapat2D;
    }

    public String getPlecariCapat2S() {
        return plecariCapat2S;
    }

    public void setPlecariCapat2S(String plecariCapat2S) {
        this.plecariCapat2S = plecariCapat2S;
    }

    public String getPlecariCapat2LV() {
        return plecariCapat2LV;
    }

    public void setPlecariCapat2LV(String plecariCapat2LV) {
        this.plecariCapat2LV = plecariCapat2LV;
    }

    public String getPlecariCapat1D() {
        return plecariCapat1D;
    }

    public void setPlecariCapat1D(String plecariCapat1D) {
        this.plecariCapat1D = plecariCapat1D;
    }

    public String getPlecariCapat1S() {
        return plecariCapat1S;
    }

    public void setPlecariCapat1S(String plecariCapat1S) {
        this.plecariCapat1S = plecariCapat1S;
    }

    public String getPlecariCapat1LV() {
        return plecariCapat1LV;
    }

    public void setPlecariCapat1LV(String plecariCapat1LV) {
        this.plecariCapat1LV = plecariCapat1LV;
    }

    public String getBusNumber() {
        return apiEndpoint;
    }

    public void setBusNumber(String apiEndpoint) {
        this.apiEndpoint = apiEndpoint;
    }

    public String getNameCapat2() {
        return nameCapat2;
    }

    public void setNameCapat2(String nameCapat2) {
        this.nameCapat2 = nameCapat2;
    }

    public String getNameCapat1() {
        return nameCapat1;
    }

    public void setNameCapat1(String nameCapat1) {
        this.nameCapat1 = nameCapat1;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return apiEndpoint + " " + name;
    }
}
