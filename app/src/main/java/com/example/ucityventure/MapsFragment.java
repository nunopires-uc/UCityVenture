package com.example.ucityventure;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapsFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private MyLocationNewOverlay myLocationNewOverlay;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    MapView map = null;

    public MapsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MapsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapsFragment newInstance(String param1, String param2) {
        MapsFragment fragment = new MapsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    private void addMarkerAtLocation(GeoPoint p) {
        // Create a marker at the specified location
        Marker marker = new Marker(map);
        marker.setPosition(p);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        map.invalidate();

        // Use a geocoding service to get the street name
        // Note: This is a placeholder, replace with your chosen geocoding service
        //String streetName = getStreetName(p);
        Log.i("MapsFragment", "Selected location: "  + ", " + p.getLatitude() + ", " + p.getLongitude());
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Load/initialize the osmdroid configuration
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        // Initialize the map view
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);

        // Initialize MyLocationNewOverlay
        myLocationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ctx), map);
        myLocationNewOverlay.enableMyLocation();
        map.getOverlays().add(myLocationNewOverlay);

        // Set up the mapEventsOverlay for long press
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                // Long press detected, add a marker and get the street name
                addMarkerAtLocation(p);
                return true;
            }
        });
        map.getOverlays().add(0, mapEventsOverlay);
        

        // Run onFirstFix to get the user's location
        myLocationNewOverlay.runOnFirstFix(new Runnable() {
            public void run() {
                GeoPoint myLocation = myLocationNewOverlay.getMyLocation();

                Log.d("Location", myLocation.toDoubleString());
                // Center the map on the user's location
                map.getController().setCenter(myLocation);
                // Set an appropriate zoom level (adjust the value as needed)
                map.getController().setZoom(15.0);
                // Animate to the user's location
                map.getController().animateTo(myLocation);
                // Add a marker at the user's location
                addMarkerAtLocation(myLocation);
            }
        });
    }



    @Override
    public void onResume() {
        super.onResume();
        myLocationNewOverlay.enableMyLocation();
        map.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        myLocationNewOverlay.disableMyLocation();
        map.onPause();
    }
}