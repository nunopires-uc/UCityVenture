package com.example.ucityventure;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RideProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RideProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Executor executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    Ride selectedRide;
    User provider;

    TextView driverName, driverNickname, driverLicense, driverRating, mainTitle, subTitle, rideInfo, rideDestination, rideOrigin;
    Button joinButton;

    MapView map = null;

    Address currentpoint;

    IMapController mapController;

    GeoPoint currentLocalGeoPoint, selectedGeoPoint;
    private Marker marker;

    private SharedViewModel model;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //ID do utilizadores autenticado atualmente
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

    public RideProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RideProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RideProfileFragment newInstance(String param1, String param2) {
        RideProfileFragment fragment = new RideProfileFragment();
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
        return inflater.inflate(R.layout.ride_profile_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        driverName = view.findViewById(R.id.driverName);
        driverNickname = view.findViewById(R.id.driverNickname);
        driverLicense = view.findViewById(R.id.driverLicense);
        driverRating = view.findViewById(R.id.driverRating);
        mainTitle = view.findViewById(R.id.mainTitle);
        subTitle = view.findViewById(R.id.subTitle);
        rideInfo = view.findViewById(R.id.rideInfo);

        rideOrigin = view.findViewById(R.id.rideOrigin);
        rideDestination = view.findViewById(R.id.rideDestination);

        joinButton = view.findViewById(R.id.joinButton);


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



        //Se os dados tiverem sido corretamente recebidos, devem ser apresentados usando a função "setRideValues"
        //o "listenForChanges" escuta alterações à viagem em questão e atualiza os dados casa se verifique uma alteração
        if(selectedRide != null){
            setRideValues(selectedRide);
            listenForChanges(selectedRide);

        }
    }

    //Função que permite adicionar a inscrição de um determinado User a uma determinada boleia
    public void subRide(String userID, Ride ride){

        ArrayList<String> novaLista = ride.getRidePassangers();
        novaLista.add(userID);


        executor.execute(() -> {
            //Através da criação de uma referência da coleção
            DocumentReference docRef = db.collection("rides").document(ride.getId());

            //Substituir o objeto de viagem por um novo com a lista de passageiros atualizada
            docRef.update("ridePassangers", novaLista)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mainHandler.post(() -> {
                                Log.d(TAG, "Note updated successfully!");
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mainHandler.post(() -> {
                                Log.w(TAG, "Error updating note", e);
                            });
                        }
                    });
        });
    }
    //Função que permite remover a inscrição de um determinado User a uma determinada boleia
    public void unsubRide(String userID, Ride ride){
        ArrayList<String> novaLista = ride.getRidePassangers();
        novaLista.remove(userID);


        executor.execute(() -> {
            //Através da criação de uma referência da coleção
            DocumentReference docRef = db.collection("rides").document(ride.getId());


            //Substituir o objeto de viagem por um novo com a lista de passageiros atualizada
            docRef.update("ridePassangers", novaLista)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mainHandler.post(() -> {
                                Log.d(TAG, "Note updated successfully!");
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            mainHandler.post(() -> {
                                Log.w(TAG, "Error updating note", e);
                            });
                        }
                    });
        });
    }

    //Escuta alterações numa determinada boleia e atualiza os dados apresentados
    void listenForChanges(Ride selectedRide) {
        executor.execute(() -> {
            db.collection("rides").document(selectedRide.getId())
                    .addSnapshotListener(MetadataChanges.EXCLUDE, new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            if (snapshot != null && snapshot.exists()) {
                                Log.d(TAG, "Document data: " + snapshot.getData());
                                //assim que exista alguma alteração na tabela "rides/[id da boleia]", os valores sao repostos pelos mais atualizados
                                setRideValues(snapshot.toObject(Ride.class));
                            } else {
                                Log.d(TAG, "Current data: null");
                            }
                        }
                    });
        });
    }

    //Define os dados a serem apresentados no ecrã
    void setRideValues(Ride selectedRide){
        mainTitle.setText("Boleia para " + selectedRide.getDestination());
        subTitle.setText("Desde " + selectedRide.getOrigin());


        driverLicense.setText(selectedRide.getLicense());
        rideOrigin.setText("Origem: " + selectedRide.getOrigin());
        rideDestination.setText("Destino: " + selectedRide.getDestination());

        if(!selectedRide.getInfo().equals("")){
            rideInfo.setText("Informações: " + selectedRide.getInfo());
        } else {
            rideInfo.setText("Sem informações");
        }


        //Adiciona um marcador nas coordenadas do local de origem da boleia
        GeoPoint startPoint = new GeoPoint(selectedRide.getOriginLat(), selectedRide.getOriginLon());
        mapController.setCenter(startPoint);
        addMarkerAtLocation(startPoint);



        //Ir buscar o objeto do provider da boleia
        DocumentReference docRef = db.collection("users").document(selectedRide.getProvider()); // Replace with your collection name and document ID
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        //Receber o objeto do utilizador
                        provider = document.toObject(User.class);

                        driverName.setText(provider.getNome());
                        driverNickname.setText(provider.getEmail());


                        //Calcula o rating do utilizador
                        if(provider.getSomaRatings() != 0 && provider.getNumRatings() != 0){
                            float rating = provider.getSomaRatings() / provider.getNumRatings();
                            String fRating = String.format("%.02f", rating);
                            driverRating.setText(fRating + " pontos");


                        } else {
                            driverRating.setText("Sem rating");
                        }

                    } else {


                    }
                } else {

                }
            }
        });


        //Se quem está a aceder ao perfil de boleia for o próprio utilizador que a criou, o botão não é apresentado
        if(selectedRide.getProvider().equals(uuid)){
            joinButton.setVisibility(View.INVISIBLE);
        } else {
            joinButton.setVisibility(View.VISIBLE);
        }


        //Lógica de apresentação do botão de entrar/sair de boleia
        if(selectedRide.getRidePassangers().size() >= selectedRide.getRideCapacity()){
            //Está cheia
            joinButton.setEnabled(false);
            joinButton.setText("Cheio");

        } else {
            //Tem vagas
            joinButton.setEnabled(true);
            joinButton.setText("Entrar");
        }

        if(selectedRide.getRidePassangers().contains(uuid)){
            //Já está inscrito
            joinButton.setEnabled(true);
            joinButton.setText("Inscrito");
        }

        //Lógica de inscrição/desinscrição na boleia
        joinButton.setOnClickListener(v -> {
            if(selectedRide.getRidePassangers().contains(uuid)){
                //Está inscrito - Sair
                unsubRide(uuid, selectedRide);
                joinButton.setText("Entrar");

            } else {
                //Não está inscrito
                if(joinButton.getText().toString().equals("Entrar")){
                    //Entrar
                    if(selectedRide.getRidePassangers().size() < selectedRide.getRideCapacity()){
                        //Tem vagas
                        subRide(uuid, selectedRide);
                        joinButton.setText("Sair");
                    } else {
                        //Não tem vagas

                    }
                } else {
                    //Sair
                    unsubRide(uuid, selectedRide);
                    joinButton.setText("Entrar");
                }
            }
        });




    }
    //Adicionar marcador num ponto especifico
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

    //Devolve uma morada de um determinado ponto geográfico
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
                // handle case where no addresses were found
            }
        } catch (IOException e) {
            // handle exception
        }
        return null;
    }

}