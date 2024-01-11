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

    //Esta classe é responsável pelo comportamento dos vários cards dentro de listviews
    //A maior parte das funcionalidades desta classe estão relacionadas com o botão de entrar/sair de uma boleia

    //A diferenca para o CustomAdapter está no tipo de card utilizado, enquanto o CustomAdapter usa o "card_ride", o CustomAdapter2 usa o "card_own_ride"

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
        //Obter a informação da boleia especificada
        Ride ride = getItem(position);


        if (convertView == null) {
            //Utiliza o card "card_own_ride"
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_own_ride, parent, false);
        }


        TextView rideName = convertView.findViewById(R.id.rideName);
        TextView rideDesc = convertView.findViewById(R.id.rideDesc);
        TextView rideCount = convertView.findViewById(R.id.rideCount);


        //Definir as informações do card
        rideName.setText(ride.getDestination());
        rideDesc.setText("Desde " + ride.getOrigin());
        rideCount.setText(ride.getRidePassangers().size() + "/" + ride.getRideCapacity());

        //Quando se clica no card - Mostrar perfil da boleia
        convertView.setOnClickListener(v -> {
            //Colocar a informação da boleia selecionada num bundle
            Bundle bundle = new Bundle();
            bundle.putParcelable("selectedRide", (Parcelable) ride);
            RideProfileFragment rideProfileFragment = new RideProfileFragment();
            rideProfileFragment.setArguments(bundle);
            //Redirecionar para outro fragment
            ((MainActivity)getContext()).MudarFragmentoPOP(rideProfileFragment);
        });





        // Return the completed view to render on screen
        return convertView;
    }








}
