package com.example.ucityventure;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
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

    //Colocar um pin no mapa
    private void addMarkerAtLocation(GeoPoint p) {
        if (marker != null) {
            // Remover o marcador existente
            map.getOverlays().remove(marker);
        }

        // Criar um marcador no local especificado
        marker = new Marker(map);
        marker.setPosition(p);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().add(marker);
        map.invalidate();

        currentpoint = getAddressFromLocation(p);

        //Colocar no canto superior a rua do marcador escolhido
        subTitle.setText(currentpoint.getAddressLine(0).toString());
        selectedGeoPoint = new GeoPoint(currentpoint.getLatitude(), currentpoint.getLongitude());

        Log.i("MapsFragment", "Selected location: " + ", " + p.getLatitude() + ", " + p.getLongitude());

    }

    // Retirar o endereço dado uma localização
    public Address getAddressFromLocation(GeoPoint p) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(p.getLatitude(), p.getLongitude(), 1);
            if (!addresses.isEmpty()) {
                Address add = addresses.get(0);
                return add;
            } else {
                // tratar os casos em que não foram encontrados endereços
            }
        } catch (IOException e) {
            // Não fazer nada
        }
        return null;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        subTitle = view.findViewById(R.id.subTitle);
        setPosButton = view.findViewById(R.id.setPosButton);
        GeoPoint startPoint;

        // Carregar/inicializar a configuração do osmdroid
        Context ctx = getActivity().getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        // Inicializar a vista do mapa
        map = (MapView) view.findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        // O método está deprecado, mas era como estava na documentação
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        // permite controlo no mapa
        mapController = map.getController();
        // colocar o zoom
        mapController.setZoom(9.5);

        //se a localização for nula, colocar o marcador numa localização default
        if(currentLocalGeoPoint != null){
            startPoint = new GeoPoint(currentLocalGeoPoint.getLatitude(), currentLocalGeoPoint.getLongitude());
        }else{
            startPoint = new GeoPoint(48.8583, 2.2944);
        }

        mapController.setCenter(startPoint);
        addMarkerAtLocation(startPoint);


        //Controlo para mudar o marcador quando se prime no mapa
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

        // Adicionar MapEventsOverlay ao mapa (funções de toque)
        map.getOverlays().add(0, mapEventsOverlay);


        //Colocar no sharedviewmodel o endereço, latitude, longitude do ponto escolhido
        setPosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(model != null){
                    CompoundLocation cl = new CompoundLocation(currentpoint, selectedGeoPoint.getLatitude(), selectedGeoPoint.getLongitude());
                    model.selectCompoundLocation(cl);
                    if(getParentFragmentManager() != null){
                        getParentFragmentManager().popBackStack();
                    }
                }
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