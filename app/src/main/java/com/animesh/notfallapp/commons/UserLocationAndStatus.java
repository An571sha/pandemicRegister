package com.animesh.notfallapp.commons;

public class UserLocationAndStatus {

    private String userId;
    private Double latitude;
    private Double longitude;
    private String address;
    private String status;
    private String phoneNumber;
    private int distanceToUserLocation;

    public UserLocationAndStatus(String userId, Double latitude, Double longitude, String address, String status, String phoneNumber) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.status = status;
        this.phoneNumber = phoneNumber;
    }

    public UserLocationAndStatus(String userId, Double latitude, Double longitude, String address, String status, String phoneNumber, int distanceToUserLocation) {
        this.userId = userId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.status = status;
        this.phoneNumber = phoneNumber;
        this.distanceToUserLocation = distanceToUserLocation;
    }

    public UserLocationAndStatus() {

    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getDistanceToUserLocation() {
        return distanceToUserLocation;
    }

    public void setDistanceToUserLocation(int distanceToUserLocation) {
        this.distanceToUserLocation = distanceToUserLocation;
    }




}
