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
 * Use the {@link SubscribedRidesFragment
 * #newInstance} factory method to
 * create an instance of this fragment.
 */
public class SubscribedRidesFragment extends Fragment {

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

    //ID do utilizador que está autenticado
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();


    CustomAdapter adapter;
    ArrayList<Ride> ridesList, defaultList;

    View v;

    public SubscribedRidesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SubscribedRidesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SubscribedRidesFragment newInstance(String param1, String param2) {
        SubscribedRidesFragment fragment = new SubscribedRidesFragment();
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

        v = inflater.inflate(R.layout.subscribed_rides_fragment, container, false);

        ridesList = new ArrayList<>();

        adapter = new CustomAdapter(requireActivity(), ridesList);

        if(getActivity() != null){

            defaultList = new ArrayList<>(ridesList);

            ListView listView = v.findViewById(R.id.ListViewRides);
            //a variável adapter é referente à classe CustomAdaptar que é responsavel pelo comportamento dos cards nas listas
            listView.setAdapter(adapter);


            //Função que escuta as alterações na base de dados e atualiza a lista
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

                                        if(isAdded()){
                                            //Recolha dos dados
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

                                            //Se o utilizador atual estiver contido na lista de passageiros de uma boleia então a boleia é adicionada à lista de boleias a apresentar
                                            if(ridePassangers.contains(uuid)){
                                                ridesList.add(ride);
                                            }




                                            adapter = new CustomAdapter(requireActivity(), ridesList);
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
                            //assim que exista alguma alteração na tabela "rides", a lista é repopulada
                            populateListFromDatabase(v);
                        }
                    });
        });
    }
}