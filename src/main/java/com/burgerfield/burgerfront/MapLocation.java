package com.burgerfield.burgerfront;


/**
 * Simple data object representing a marker on a map.
 */
public class MapLocation {

    private double latitude;
    private double longitude;
    private String name;
    boolean isBurgerSpot;

    public MapLocation(double latitude, double longitude, String name, boolean isBurgerSpot) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.isBurgerSpot = isBurgerSpot;
    }

    public boolean isBurgerSpot() {
        return isBurgerSpot;
    }

    public void setBurgerSpot(boolean burgerSpot) {
        isBurgerSpot = burgerSpot;
    }

    public MapLocation() {

    }

    public MapLocation(double latitude, double longitude, String name) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}