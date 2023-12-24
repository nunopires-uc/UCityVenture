package com.example.ucityventure;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.MetadataChanges;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    Executor executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    FirebaseFirestore db = FirebaseFirestore.getInstance();

    //isto é o id do user que ta logado
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
    View v;



    CustomAdapter adapter;
    ArrayList<Ride> ridesList, defaultList;



    EditText originInput, destinationInput;



    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {





        v = inflater.inflate(R.layout.main_fragment, container, false);

        originInput = v.findViewById(R.id.originInput);
        destinationInput = v.findViewById(R.id.destinationInput);

        ridesList = new ArrayList<>();

        adapter = new CustomAdapter(requireActivity(), ridesList);

        if(getActivity() != null){

            defaultList = new ArrayList<>(ridesList);

            ListView listView = v.findViewById(R.id.ListViewRides);
            listView.setAdapter(adapter);

            listenForChanges(v);
        }

        originInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterListByText(originInput.getText().toString(), destinationInput.getText().toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        destinationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                filterListByText(originInput.getText().toString(), destinationInput.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        return v;
    }

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
                                        /*
                                        check whether the Fragment is currently added to its Activity before attempting to use requireActivity().
                                        You can do this by calling the isAdded() method, which returns true if the Fragment is currently added to
                                        the Activity
                                         */
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

                                            ridesList.add(ride);


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
                            populateListFromDatabase(v);
                        }
                    });
        });
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    public void filterListByText(String originQuery, String destinationQuery){
        if(adapter != null){

            adapter.filter(originQuery, destinationQuery);
        }
    }
}