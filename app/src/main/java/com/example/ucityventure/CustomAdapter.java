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

public class CustomAdapter extends ArrayAdapter<Ride> {

    //Esta classe é responsável pelo comportamento dos vários cards dentro de listviews
    //A maior parte das funcionalidades desta classe estão relacionadas com o botão de entrar/sair de uma boleia

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();
    Executor executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    private List<Ride> defaultList;


    public CustomAdapter(Context context, List<Ride> rides) {
        super(context, 0, rides);
        this.defaultList = new ArrayList<>(rides);


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Obter a informação da boleia especificada
        Ride ride = getItem(position);


        if (convertView == null) {
            //Utiliza o card "card_ride"
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_ride, parent, false);
        }


        TextView rideName = convertView.findViewById(R.id.rideName);
        TextView rideDesc = convertView.findViewById(R.id.rideDesc);
        TextView rideCount = convertView.findViewById(R.id.rideCount);
        Button rideButton = convertView.findViewById(R.id.rideButton);

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


        //Parte visual
        if(ride.getRidePassangers().size() >= ride.getRideCapacity()){
            //Boleia cheia
            rideButton.setEnabled(false);
            rideButton.setText("Cheio");

        } else {
            //Com vagas
            rideButton.setEnabled(true);
            rideButton.setText("Entrar");
        }

        if(ride.getRidePassangers().contains(uuid)){
            //Utilizador atual já está inscrito nesta boleia
            rideButton.setEnabled(true);
            rideButton.setText("Inscrito");
        }



        View finalConvertView = convertView;
        rideButton.setOnClickListener(v -> {
            if(ride.getRidePassangers().contains(uuid)){
                //Está inscrito - Sair
                unsubRide(uuid, ride);
                rideButton.setText("Entrar");

            } else {
                //Não está inscrito
                if(rideButton.getText().toString().equals("Entrar")){
                    //Entrar
                    if(ride.getRidePassangers().size() < ride.getRideCapacity()){
                        //Tem vagas
                        subRide(uuid, ride);
                        rideButton.setText("Sair");
                    } else {
                        //Não tem vagas
                        Snackbar.make(finalConvertView, "Não existem vagas", Snackbar.LENGTH_SHORT).show();
                    }
                } else {
                    //Sair
                    unsubRide(uuid, ride);
                    rideButton.setText("Entrar");
                }
            }
        });



        // Return the completed view to render on screen
        return convertView;
    }


    //Função que permite a inscrição de um utilizador numa determinada boleia
    public void subRide(String userID, Ride ride){

        ArrayList<String> novaLista = ride.getRidePassangers();
        novaLista.add(userID);


        executor.execute(() -> {
            //Referência ao objeto da boleia especificada
            DocumentReference docRef = db.collection("rides").document(ride.getId());

            //Substitui o objeto da boleia por outro com a lista de passageiros atualizada
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

    //Função que permite a desinscrição de um utilizador numa determinada boleia
    public void unsubRide(String userID, Ride ride){
        ArrayList<String> novaLista = ride.getRidePassangers();
        novaLista.remove(userID);


        executor.execute(() -> {
            //Referência ao objeto da boleia especificada
            DocumentReference docRef = db.collection("rides").document(ride.getId());

            //Substitui o objeto da boleia por outro com a lista de passageiros atualizada
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

    //Função que permite filtrar as boleias por origem/destino
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
