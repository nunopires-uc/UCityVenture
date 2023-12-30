package com.example.ucityventure;

import static android.app.PendingIntent.getActivity;
import static android.content.ContentValues.TAG;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomAdapter2 extends ArrayAdapter<Ride> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
    Executor executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    private List<Ride> defaultList;


    public CustomAdapter2(Context context, List<Ride> rides) {
        super(context, 0, rides);
        this.defaultList = new ArrayList<>(rides);


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Ride ride = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_own_ride, parent, false);
        }

        // Lookup view for data population
        TextView rideName = convertView.findViewById(R.id.rideName);
        TextView rideDesc = convertView.findViewById(R.id.rideDesc);
        TextView rideCount = convertView.findViewById(R.id.rideCount);


        // Populate the data into the template view using the data object
        rideName.setText(ride.getDestination());
        rideDesc.setText("Desde " + ride.getOrigin());
        rideCount.setText(ride.getRidePassangers().size() + "/" + ride.getRideCapacity());

        //Mostrar perfil da boleia


        convertView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("selectedRide", (Parcelable) ride);
            RideProfileFragment rideProfileFragment = new RideProfileFragment();
            rideProfileFragment.setArguments(bundle);
            ((MainActivity)getContext()).MudarFragmentoPOP(rideProfileFragment);
        });


        /*
        //Parte visual
        if(ride.getRidePassangers().size() >= ride.getRideCapacity()){
            //cheia
            rideButton.setEnabled(false);
            rideButton.setText("Cheio");

        } else {
            //com vagas
            rideButton.setEnabled(true);
            rideButton.setText("Entrar");
        }

        if(ride.getRidePassangers().contains(uuid)){
            //o user ta inscrito nesta ride
            rideButton.setEnabled(true);
            rideButton.setText("Inscrito");
        } else {
            //nao esta inscrito
        }

        //ordem:
        //ta inscrito?
        //

        View finalConvertView = convertView;
        rideButton.setOnClickListener(v -> {
            if(ride.getRidePassangers().contains(uuid)){
                //ta inscrito - Sair
                unsubRide(uuid, ride);
                rideButton.setText("Entrar");

            } else {
                //nao ta
                if(rideButton.getText().toString().equals("Entrar")){
                    //quer entrar
                    if(ride.getRidePassangers().size() < ride.getRideCapacity()){
                        //ha vagas
                        subRide(uuid, ride);
                        rideButton.setText("Sair");
                    } else {
                        //nao ha vagas
                        Snackbar.make(finalConvertView, "Não existem vagas", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    //quer sair
                    unsubRide(uuid, ride);
                    rideButton.setText("Entrar");
                }
            }
        });

         */

        convertView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Show a dialog when a card is long-pressed
                //showCardDetailsDialog(cardItem, position);
                return true; // Return true to consume the long-press event
            }
        });

        // Return the completed view to render on screen
        return convertView;
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


    public void filter(String originQuery, String destinationQuery){
        List<Ride> filteredItems = new ArrayList<>();
        for (Ride item : defaultList) {
            if (item.getOrigin().toLowerCase().contains(originQuery.toLowerCase()) && item.getDestination().toLowerCase().contains(destinationQuery.toLowerCase())) {

                filteredItems.add(item);
            }
        }
        clear();
        addAll(filteredItems);
        notifyDataSetChanged();
    }




}
