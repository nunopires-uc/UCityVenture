package com.example.ucityventure;

import static android.content.ContentValues.TAG;

import android.os.Bundle;

import androidx.annotation.NonNull;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
    ArrayList<Ride> ridesList;



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
        // Inflate the layout for this fragment



        v = inflater.inflate(R.layout.main_fragment, container, false);

        ListView listView = v.findViewById(R.id.ListViewNotes);


        return v;
    }

    public void populateListFromDatabase(View v){
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
                                            float originLat = Float.parseFloat(document.getData().get("originLat").toString());
                                            float originLon = Float.parseFloat(document.getData().get("originLon").toString());
                                            String provider = document.getData().get("provider").toString();
                                            int rideCapacity = Integer.parseInt(document.getData().get("rideCapacity").toString());

                                            String ridePassangers = document.getData().get("ridePassangers").toString();

                                            String state = document.getData().get("state").toString();
                                            String time = document.getData().get("time").toString();

                                            Ride ride = new Ride();
                                            ride.setDestination(destination);
                                            ride.setInfo(info);
                                            ride.setLicense(license);
                                            ride.setOrigin(origin);
                                            ride.setOriginLat(originLat);
                                            ride.setOriginLon(originLon);
                                            ride.setProvider(provider);
                                            ride.setRideCapacity(rideCapacity);
                                            //ride.setRideCapacity(ridePassangers);
                                            ride.setState(state);
                                            ride.setTime(time);

                                            System.out.println(ride.toString());
                                            ridesList.add(ride);


                                            adapter = new CustomAdapter(requireActivity(), ridesList);
                                            //defaultList = new ArrayList<>(cardItems);
                                            // Find the ListView and set the adapter
                                            ListView listView = v.findViewById(R.id.ListViewNotes);
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
}