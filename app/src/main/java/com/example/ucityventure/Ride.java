package com.example.ucityventure;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class Ride implements Parcelable{

    //Classe referente às boleias
    //Todos os parâmetros são auto-explicativos
    private String destination;
    private String info;
    private String license;
    private String origin;
    private Double originLat;
    private Double originLon;
    private String provider;
    private int rideCapacity;
    private ArrayList<String> ridePassangers;
    private String state;
    private String time, id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Ride() {
    }

    public Ride(String destination, String info, String license, String origin, Double originLat, Double originLon, String provider, int rideCapacity, ArrayList<String> ridePassangers, String state, String time) {
        this.destination = destination;
        this.info = info;
        this.license = license;
        this.origin = origin;
        this.originLat = originLat;
        this.originLon = originLon;
        this.provider = provider;
        this.rideCapacity = rideCapacity;
        this.ridePassangers = ridePassangers;
        this.state = state;
        this.time = time;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public Double getOriginLat() {
        return originLat;
    }

    public void setOriginLat(Double originLat) {
        this.originLat = originLat;
    }

    public Double getOriginLon() {
        return originLon;
    }

    public void setOriginLon(Double originLon) {
        this.originLon = originLon;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public int getRideCapacity() {
        return rideCapacity;
    }

    public void setRideCapacity(int rideCapacity) {
        this.rideCapacity = rideCapacity;
    }

    public ArrayList<String> getRidePassangers() {
        return ridePassangers;
    }

    public void setRidePassangers(ArrayList<String> ridePassangers) {
        this.ridePassangers = ridePassangers;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Ride{");
        sb.append("destination='").append(destination).append('\'');
        sb.append(", info='").append(info).append('\'');
        sb.append(", license='").append(license).append('\'');
        sb.append(", origin='").append(origin).append('\'');
        sb.append(", originLat=").append(originLat);
        sb.append(", originLon=").append(originLon);
        sb.append(", provider='").append(provider).append('\'');
        sb.append(", rideCapacity=").append(rideCapacity);
        sb.append(", ridePassangers=").append(ridePassangers);
        sb.append(", state='").append(state).append('\'');
        sb.append(", time='").append(time).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {

    }
}
