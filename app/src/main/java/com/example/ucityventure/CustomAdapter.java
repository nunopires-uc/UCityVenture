package com.example.ucityventure;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CustomAdapter extends ArrayAdapter<Ride> {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    Executor executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());

    private List<Ride> defaultList;


    public CustomAdapter(Context context, List<Ride> rides) {
        super(context, 0, rides);
        this.defaultList = new ArrayList<>(rides);


    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Ride ride = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.card_ride, parent, false);
        }

        // Lookup view for data population
        TextView rideName = convertView.findViewById(R.id.rideName);
        TextView rideDesc = convertView.findViewById(R.id.rideDesc);
        TextView rideCount = convertView.findViewById(R.id.rideCount);
        Button rideButton = convertView.findViewById(R.id.rideButton);

        // Populate the data into the template view using the data object
        rideName.setText(ride.getOrigin() + "->" + ride.getDestination());
        rideDesc.setText("descricao e que");


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

}
