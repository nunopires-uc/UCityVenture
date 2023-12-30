package com.example.ucityventure;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //isto é o id do user que ta logado
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
            selectedRide = getArguments().getParcelable("selectedRide");
            Log.i("Kazzio", selectedRide.toString());
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





        if(selectedRide != null){
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



            //Ir buscar o objeto do provider
            DocumentReference docRef = db.collection("users").document(selectedRide.getProvider()); // Replace with your collection name and document ID
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                            provider = document.toObject(User.class);

                            driverName.setText(provider.getNome());
                            driverNickname.setText(provider.getEmail());

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




            //Botao de entrar
            if(selectedRide.getRidePassangers().size() >= selectedRide.getRideCapacity()){
                //cheia
                joinButton.setEnabled(false);
                joinButton.setText("Cheio");

            } else {
                //com vagas
                joinButton.setEnabled(true);
                joinButton.setText("Entrar");
            }

            if(selectedRide.getRidePassangers().contains(uuid)){
                //o user ta inscrito nesta ride
                joinButton.setEnabled(true);
                joinButton.setText("Inscrito");
            } else {
                //nao esta inscrito
            }

            joinButton.setOnClickListener(v -> {
                if(selectedRide.getRidePassangers().contains(uuid)){
                    //ta inscrito - Sair
                    unsubRide(uuid, selectedRide);
                    joinButton.setText("Entrar");

                } else {
                    //nao ta
                    if(joinButton.getText().toString().equals("Entrar")){
                        //quer entrar
                        if(selectedRide.getRidePassangers().size() < selectedRide.getRideCapacity()){
                            //ha vagas
                            subRide(uuid, selectedRide);
                            joinButton.setText("Sair");
                        } else {
                            //nao ha vagas
                            Snackbar.make(view, "Não existem vagas", Snackbar.LENGTH_SHORT).show();
                        }
                    } else {
                        //quer sair
                        unsubRide(uuid, selectedRide);
                        joinButton.setText("Entrar");
                    }
                }
            });




        }
    }

    public void subRide(String userID, Ride ride){

        ArrayList<String> novaLista = ride.getRidePassangers();
        novaLista.add(userID);


        executor.execute(() -> {
            DocumentReference docRef = db.collection("rides").document(ride.getId());

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

    public void unsubRide(String userID, Ride ride){
        ArrayList<String> novaLista = ride.getRidePassangers();
        novaLista.remove(userID);


        executor.execute(() -> {
            DocumentReference docRef = db.collection("rides").document(ride.getId());

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


}