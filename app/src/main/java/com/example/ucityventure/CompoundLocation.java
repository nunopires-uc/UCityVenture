package com.example.ucityventure;

import android.location.Address;

public class CompoundLocation {

    //Classe que guarda o endereço de um local, a sua latitude, e longitude

    Address locationAddress;
    Double Latitude, Longitude;

    public CompoundLocation(Address locationAddress, Double latitude, Double longitude) {
        this.locationAddress = locationAddress;
        Latitude = latitude;
        Longitude = longitude;
    }

    public Address getLocationAddress() {
        return locationAddress;
    }

    public void setLocationAddress(Address locationAddress) {
        this.locationAddress = locationAddress;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }
}
