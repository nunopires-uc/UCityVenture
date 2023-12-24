package com.example.ucityventure;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.osmdroid.util.GeoPoint;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<GeoPoint> selectedGeoPoint = new MutableLiveData<>();
    // Add other MutableLiveData for other data

    public void selectGeoPoint(GeoPoint geoPoint) {
        selectedGeoPoint.setValue(geoPoint);
    }

    public LiveData<GeoPoint> getSelectedGeoPoint() {
        return selectedGeoPoint;
    }
    // Add other getters and setters for other data
}
