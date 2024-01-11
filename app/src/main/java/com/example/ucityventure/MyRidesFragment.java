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
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyRidesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyRidesFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    Executor executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //ID do utilizador atualmente autenticado
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();


    CustomAdapter2 adapter;
    ArrayList<Ride> ridesList, defaultList;

    View v;

    public MyRidesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyRidesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyRidesFragment newInstance(String param1, String param2) {
        MyRidesFragment fragment = new MyRidesFragment();
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

        v = inflater.inflate(R.layout.my_rides_fragment, container, false);

        ridesList = new ArrayList<>();

        adapter = new CustomAdapter2(requireActivity(), ridesList);

        if(getActivity() != null){

            defaultList = new ArrayList<>(ridesList);

            ListView listView = v.findViewById(R.id.ListViewRides);
            listView.setAdapter(adapter);

            listenForChanges(v);
        }

        return v;
    }

    //Função que recolhe os dados da base de dados e popula a lista
    public void populateListFromDatabase(View v){
        ridesList.clear();
        executor.execute(() -> {
            db.collection("rides")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    mainHandler.post(() -> {
                                        //Recebe os dados da BD
                                        if(isAdded()){
                                            String destination = document.getData().get("destination").toString();
                                            String info = document.getData().get("info").toString();
                                            String license = document.getData().get("license").toString();
                                            String origin = document.getData().get("origin").toString();
                                            Double originLat = Double.parseDouble(document.getData().get("originLat").toString());
                                            Double originLon = Double.parseDouble(document.getData().get("originLon").toString());
                                            String provider = document.getData().get("provider").toString();
                                            int rideCapacity = Integer.parseInt(document.getData().get("rideCapacity").toString());

                                            ArrayList<String> ridePassangers = (ArrayList<String>) document.getData().get("ridePassangers");

                                            String state = document.getData().get("state").toString();
                                            String time = document.getData().get("time").toString();


                                            String id = document.getId();

                                            //Compila tudo num objeto de boleia
                                            Ride ride = new Ride();
                                            ride.setDestination(destination);
                                            ride.setInfo(info);
                                            ride.setLicense(license);
                                            ride.setOrigin(origin);
                                            ride.setOriginLat(originLat);
                                            ride.setOriginLon(originLon);
                                            ride.setProvider(provider);
                                            ride.setRideCapacity(rideCapacity);
                                            ride.setRidePassangers(ridePassangers);
                                            ride.setState(state);
                                            ride.setTime(time);
                                            ride.setId(id);

                                            //Adiciona apenas as boleias em que o "provider" tem o ID do utilizador atualmente autenticado
                                            if(ride.getProvider().equals(uuid)){
                                                ridesList.add(ride);
                                            }




                                            adapter = new CustomAdapter2(requireActivity(), ridesList);
                                            //defaultList = new ArrayList<>(cardItems);
                                            // Find the ListView and set the adapter
                                            ListView listView = v.findViewById(R.id.ListViewRides);
                                            listView.removeAllViewsInLayout();
                                            listView.setAdapter(adapter);
                                        }
                                    });
                                }
                            } else {
                                mainHandler.post(() -> {
                                    Log.w(TAG, "Error getting documents.", task.getException());
                                    System.out.println("Error getting documents.");
                                });
                            }
                        }
                    });
        });
    }

    //Escuta alterações
    void listenForChanges(View v){

        executor.execute(()-> {
            //este listener escuta alterações na tabela "rides"
            db.collection("rides")
                    .addSnapshotListener(MetadataChanges.EXCLUDE, new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                Log.w(TAG, "Listen failed.", e);
                                return;
                            }

                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                Log.d(TAG, "IT CHANGED");
                            }
                            //cardItems.clear();
                            //volta a popular a lista
                            populateListFromDatabase(v);
                        }
                    });
        });
    }
}