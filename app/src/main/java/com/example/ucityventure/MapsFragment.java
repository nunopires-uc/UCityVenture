package com.example.ucityventure;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.util.CollectionUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

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

    TextView subTitle;
    Button setPosButton;

    MapView map = null;

    Address currentpoint;

    IMapController mapController;

    GeoPoint currentLocalGeoPoint, selectedGeoPoint;

    private SharedViewModel model;

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 123;

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
            currentLocalGeoPoint = getArguments().getParcelable("currentLocalGeoPoint");
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_maps, container, false);
        return view;
    }

    private Marker marker; // Keep a reference to the marker

    private void addMarkerAtLocation(GeoPoint p) {
        if (marker != null) {
            // Remove the existing marker
            map.getOverlays().remove(marker);
        }

        // Create a marker at the specified location
        marker = new Marker(map);
        marker.setPosition(p);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        map.invalidate();

        currentpoint = getAddressFromLocation(p);
        Log.d("End", currentpoint.getAddressLine(0));

        subTitle.setText(currentpoint.getAddressLine(0).toString());
        selectedGeoPoint = new GeoPoint(currentpoint.getLatitude(), currentpoint.getLongitude());
        // Use a geocoding service to get the street name
        // Note: This is a placeholder, replace with your chosen geocoding service
        // String streetName = getStreetName(p);

        Log.i("MapsFragment", "Selected location: " + ", " + p.getLatitude() + ", " + p.getLongitude());

    }

    public Address getAddressFromLocation(GeoPoint p) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(p.getLatitude(), p.getLongitude(), 1);
            Address add = addresses.get(0);
            return add;
        } catch (IOException e) {
            return null;
        }
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        subTitle = view.findViewById(R.id.subTitle);
        setPosButton = view.findViewById(R.id.setPosButton);

        // Load/initialize the osmdroid configuration
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        // Initialize the map view
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(9.5);



        /*LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        Log.d("Sl", location.toString());

        if (location != null) {
            Log.d("Sl", location.toString());
        } else {
            Log.d("Sl", "Location is null");
        }*/

        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);
        addMarkerAtLocation(startPoint);


        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                // Handle single tap if needed
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                // Handle long press (add a marker at the long-pressed location)
                addMarkerAtLocation(p);
                return true;
            }
        });

        // Add MapEventsOverlay to the map
        map.getOverlays().add(0, mapEventsOverlay);


        setPosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("emoji", selectedGeoPoint.toString());
                //model.selectGeoPoint(selectedGeoPoint);
                // Update other data in the model
                // Navigate back to CreateRideFragment
            }
        });

    }



    @Override
    public void onResume() {
        super.onResume();
        if (map != null) {
            map.onResume();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (map != null) {
            map.onPause();
        }
    }
}