package com.example.ucityventure;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.type.LatLng;

import org.osmdroid.util.GeoPoint;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CreateRideFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CreateRideFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static final int LOCATION_REQUEST_CODE = 1;


    EditText timeInput, locationInput, licenseInput, capacityInput, infoInput;
    Button createRideButton;
    Spinner originInput, destinationInput;

    private SharedViewModel model;

    //Variaveis
    private int year, month, day, hour, minute;

    GeoPoint currentLocalGeoPoint;

    Location currentLocation;

    LatLng originPt;

    public CreateRideFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CreateRideFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CreateRideFragment newInstance(String param1, String param2) {
        CreateRideFragment fragment = new CreateRideFragment();
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
        return inflater.inflate(R.layout.fragment_create_ride, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timeInput = view.findViewById(R.id.timeInput);
        locationInput = view.findViewById(R.id.locationInput);
        createRideButton = view.findViewById(R.id.createRideButton);
        originInput = view.findViewById(R.id.originInput);
        destinationInput = view.findViewById(R.id.destinationInput);
        licenseInput = view.findViewById(R.id.licenseInput);
        capacityInput = view.findViewById(R.id.capacityInput);
        infoInput = view.findViewById(R.id.infoInput);

        //perms
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("Permissões em falta!")
                    .setMessage("Pretende permitir à aplicação o uso dos serviços de localização?")
                    .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //Prompt the user once explanation has been shown
                            ActivityCompat.requestPermissions(getActivity(),
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                        }
                    })
                    .setNegativeButton("Não", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Necessita de permitir o acesso aos serviços de localização para criar uma boleia!", Toast.LENGTH_LONG);
                            toast.show();
                            getActivity().finish();
                            return;
                        }
                    })
                    .setCancelable(false)
                    .create()
                    .show();
        }

        LocationManager locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;

        if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {

            criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setAltitudeRequired(true);
            criteria.setBearingRequired(true);
            criteria.setSpeedRequired(true);

        }

        String provider = locationManager.getBestProvider(criteria, true);
        Location currentLocation = null;

        if (provider != null) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            } else {
                currentLocation = locationManager.getLastKnownLocation(provider);
            }
        }

        if (currentLocation != null) {
            System.out.println(currentLocation.getLatitude() + " /// " + currentLocation.getLongitude());
            currentLocalGeoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
        } else {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (location != null) {
                        currentLocalGeoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
                        System.out.println(location.getLatitude() + " /// " + location.getLongitude());
                    }
                }
                // Implement other methods as necessary
            });
        }

        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
        /*model.getSelectedGeoPoint().observe(getViewLifecycleOwner(), geoPoint -> {
            Log.d("CHEGOU!!", geoPoint.toString());
            locationInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_gps_fixed_24_blue, 0, 0, 0);
            locationInput.setText("Localização - Guardada");
        });*/

        model.getSelectedCompoundLocation().observe(getViewLifecycleOwner(), cl -> {
            locationInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_gps_fixed_24_blue, 0, 0, 0);
            locationInput.setText(cl.getLocationAddress().getAddressLine(0).toString());
        });



        Log.d("localxyz", currentLocation.toString());


        //spinner
        String[] localidades = {"Origem", "Águeda", "Aguiar da Beira", "Alandroal", "Albergaria-a-Velha", "Albufeira", "Alcácer do Sal", "Alcanena", "Alcobaça", "Alcochete", "Alcoutim", "Alenquer", "Alfândega da Fé", "Alijó", "Aljezur", "Aljustrel", "Almada", "Almeida", "Almeirim", "Almodôvar", "Alpiarça", "Alter do Chão", "Alvaiázere", "Alvito", "Amadora", "Amarante", "Amares", "Anadia", "Angra do Heroísmo", "Ansião", "Arcos de Valdevez", "Arganil", "Armamar", "Arouca", "Arraiolos", "Arronches", "Arruda dos Vinhos", "Aveiro", "Avis", "Azambuja", "Baião", "Barcelos", "Barrancos", "Barreiro", "Batalha", "Beja", "Belmonte", "Benavente", "Bombarral", "Borba", "Boticas", "Braga", "Bragança", "Cabeceiras de Basto", "Cadaval", "Caldas da Rainha", "Calheta (Madeira)", "Calheta (São Jorge)", "Caminha", "Campo Maior", "Cantanhede", "Carrazeda de Ansiães", "Carregal do Sal", "Cartaxo", "Cascais", "Castanheira de Pêra", "Castelo Branco", "Castelo de Paiva", "Castelo de Vide", "Castro Daire", "Castro Marim", "Castro Verde", "Celorico da Beira", "Celorico de Basto", "Chamusca", "Chaves", "Cinfães", "Coimbra", "Condeixa-a-Nova", "Constância", "Coruche", "Corvo", "Covilhã", "Crato", "Cuba", "Câmara de Lobos", "Elvas", "Entroncamento", "Espinho", "Esposende", "Estarreja", "Estremoz", "Évora", "Fafe", "Faro", "Felgueiras", "Ferreira do Alentejo", "Ferreira do Zêzere", "Figueira da Foz", "Figueira de Castelo Rodrigo", "Figueiró dos Vinhos", "Fornos de Algodres", "Freixo de Espada à Cinta", "Fronteira", "Funchal", "Fundão", "Gavião", "Golegã", "Gondomar", "Gouveia", "Grândola", "Guarda", "Guimarães", "Góis", "Horta", "Idanha-a-Nova", "Ílhavo", "Lagoa (Algarve)", "Lagoa (São Miguel)", "Lagos", "Lajes das Flores", "Lajes do Pico", "Lamego", "Leiria", "Lisboa", "Loulé", "Loures", "Lourinhã", "Lousã", "Lousada", "Mação", "Macedo de Cavaleiros", "Machico", "Madalena", "Mafra", "Maia", "Mangualde", "Manteigas", "Marco de Canaveses", "Marinha Grande", "Marvão", "Matosinhos", "Mealhada", "Meda", "Melgaço", "Mesão Frio", "Mira", "Miranda do Corvo", "Miranda do Douro", "Mirandela", "Mogadouro", "Moimenta da Beira", "Moita", "Monção", "Monchique", "Mondim de Basto", "Monforte", "Montalegre", "Montemor-o-Novo", "Montemor-o-Velho", "Montijo", "Mora", "Mortágua", "Moura", "Mourão", "Murça", "Murtosa", "Mértola", "Nazaré", "Nelas", "Nisa", "Nordeste", "Óbidos", "Odemira", "Odivelas", "Oeiras", "Oleiros", "Olhão", "Oliveira de Azeméis", "Oliveira de Frades", "Oliveira do Bairro", "Oliveira do Hospital", "Ourique", "Ourém", "Ovar", "Paços de Ferreira", "Palmela", "Pampilhosa da Serra", "Paredes", "Paredes de Coura", "Pedrógão Grande", "Penacova", "Penafiel", "Penalva do Castelo", "Penamacor", "Penedono", "Penela", "Peniche", "Peso da Régua", "Pinhel", "Pombal", "Ponta Delgada", "Ponta do Sol", "Ponte da Barca", "Ponte de Lima", "Ponte de Sor", "Portalegre", "Portel", "Portimão", "Porto", "Porto Moniz", "Porto Santo", "Porto de Mós", "Povoação", "Praia da Vitória", "Proença-a-Nova", "Póvoa de Lanhoso", "Póvoa de Varzim", "Redondo", "Reguengos de Monsaraz", "Resende", "Ribeira Brava", "Ribeira Grande", "Ribeira de Pena", "Rio Maior", "Sabrosa", "Sabugal", "Salvaterra de Magos", "Santa Comba Dão", "Santa Cruz", "Santa Cruz da Graciosa", "Santa Cruz das Flores", "Santa Maria da Feira", "Santa Marta de Penaguião", "Santana", "Santarém", "Santiago do Cacém", "Santo Tirso", "São Brás de Alportel", "São João da Madeira", "São João da Pesqueira", "São Pedro do Sul", "São Roque do Pico", "São Vicente", "Sardoal", "Sátão", "Seia", "Seixal", "Sernancelhe", "Serpa", "Sertã", "Sesimbra", "Setúbal", "Sever do Vouga", "Silves", "Sines", "Sintra", "Sobral de Monte Agraço", "Soure", "Sousel", "Tábua", "Tabuaço", "Tarouca", "Tavira", "Terras de Bouro", "Tomar", "Tondela", "Torre de Moncorvo", "Torres Novas", "Torres Vedras", "Trancoso", "Trofa", "Vagos", "Vale de Cambra", "Valença", "Valongo", "Valpaços", "Velas", "Vendas Novas", "Viana do Alentejo", "Viana do Castelo", "Vidigueira", "Vieira do Minho", "Vila Flor", "Vila Franca de Xira", "Vila Franca do Campo", "Vila Nova da Barquinha", "Vila Nova de Cerveira", "Vila Nova de Famalicão", "Vila Nova de Foz Côa", "Vila Nova de Gaia", "Vila Nova de Paiva", "Vila Nova de Poiares", "Vila Pouca de Aguiar", "Vila Real", "Vila Real de Santo António", "Vila Velha de Ródão", "Vila Verde", "Vila Viçosa", "Vila de Rei", "Vila do Bispo", "Vila do Conde", "Vila do Porto", "Vimioso", "Vinhais", "Viseu", "Vizela", "Vouzela"};
        String[] localidades2 = {"Destino", "Águeda", "Aguiar da Beira", "Alandroal", "Albergaria-a-Velha", "Albufeira", "Alcácer do Sal", "Alcanena", "Alcobaça", "Alcochete", "Alcoutim", "Alenquer", "Alfândega da Fé", "Alijó", "Aljezur", "Aljustrel", "Almada", "Almeida", "Almeirim", "Almodôvar", "Alpiarça", "Alter do Chão", "Alvaiázere", "Alvito", "Amadora", "Amarante", "Amares", "Anadia", "Angra do Heroísmo", "Ansião", "Arcos de Valdevez", "Arganil", "Armamar", "Arouca", "Arraiolos", "Arronches", "Arruda dos Vinhos", "Aveiro", "Avis", "Azambuja", "Baião", "Barcelos", "Barrancos", "Barreiro", "Batalha", "Beja", "Belmonte", "Benavente", "Bombarral", "Borba", "Boticas", "Braga", "Bragança", "Cabeceiras de Basto", "Cadaval", "Caldas da Rainha", "Calheta (Madeira)", "Calheta (São Jorge)", "Caminha", "Campo Maior", "Cantanhede", "Carrazeda de Ansiães", "Carregal do Sal", "Cartaxo", "Cascais", "Castanheira de Pêra", "Castelo Branco", "Castelo de Paiva", "Castelo de Vide", "Castro Daire", "Castro Marim", "Castro Verde", "Celorico da Beira", "Celorico de Basto", "Chamusca", "Chaves", "Cinfães", "Coimbra", "Condeixa-a-Nova", "Constância", "Coruche", "Corvo", "Covilhã", "Crato", "Cuba", "Câmara de Lobos", "Elvas", "Entroncamento", "Espinho", "Esposende", "Estarreja", "Estremoz", "Évora", "Fafe", "Faro", "Felgueiras", "Ferreira do Alentejo", "Ferreira do Zêzere", "Figueira da Foz", "Figueira de Castelo Rodrigo", "Figueiró dos Vinhos", "Fornos de Algodres", "Freixo de Espada à Cinta", "Fronteira", "Funchal", "Fundão", "Gavião", "Golegã", "Gondomar", "Gouveia", "Grândola", "Guarda", "Guimarães", "Góis", "Horta", "Idanha-a-Nova", "Ílhavo", "Lagoa (Algarve)", "Lagoa (São Miguel)", "Lagos", "Lajes das Flores", "Lajes do Pico", "Lamego", "Leiria", "Lisboa", "Loulé", "Loures", "Lourinhã", "Lousã", "Lousada", "Mação", "Macedo de Cavaleiros", "Machico", "Madalena", "Mafra", "Maia", "Mangualde", "Manteigas", "Marco de Canaveses", "Marinha Grande", "Marvão", "Matosinhos", "Mealhada", "Meda", "Melgaço", "Mesão Frio", "Mira", "Miranda do Corvo", "Miranda do Douro", "Mirandela", "Mogadouro", "Moimenta da Beira", "Moita", "Monção", "Monchique", "Mondim de Basto", "Monforte", "Montalegre", "Montemor-o-Novo", "Montemor-o-Velho", "Montijo", "Mora", "Mortágua", "Moura", "Mourão", "Murça", "Murtosa", "Mértola", "Nazaré", "Nelas", "Nisa", "Nordeste", "Óbidos", "Odemira", "Odivelas", "Oeiras", "Oleiros", "Olhão", "Oliveira de Azeméis", "Oliveira de Frades", "Oliveira do Bairro", "Oliveira do Hospital", "Ourique", "Ourém", "Ovar", "Paços de Ferreira", "Palmela", "Pampilhosa da Serra", "Paredes", "Paredes de Coura", "Pedrógão Grande", "Penacova", "Penafiel", "Penalva do Castelo", "Penamacor", "Penedono", "Penela", "Peniche", "Peso da Régua", "Pinhel", "Pombal", "Ponta Delgada", "Ponta do Sol", "Ponte da Barca", "Ponte de Lima", "Ponte de Sor", "Portalegre", "Portel", "Portimão", "Porto", "Porto Moniz", "Porto Santo", "Porto de Mós", "Povoação", "Praia da Vitória", "Proença-a-Nova", "Póvoa de Lanhoso", "Póvoa de Varzim", "Redondo", "Reguengos de Monsaraz", "Resende", "Ribeira Brava", "Ribeira Grande", "Ribeira de Pena", "Rio Maior", "Sabrosa", "Sabugal", "Salvaterra de Magos", "Santa Comba Dão", "Santa Cruz", "Santa Cruz da Graciosa", "Santa Cruz das Flores", "Santa Maria da Feira", "Santa Marta de Penaguião", "Santana", "Santarém", "Santiago do Cacém", "Santo Tirso", "São Brás de Alportel", "São João da Madeira", "São João da Pesqueira", "São Pedro do Sul", "São Roque do Pico", "São Vicente", "Sardoal", "Sátão", "Seia", "Seixal", "Sernancelhe", "Serpa", "Sertã", "Sesimbra", "Setúbal", "Sever do Vouga", "Silves", "Sines", "Sintra", "Sobral de Monte Agraço", "Soure", "Sousel", "Tábua", "Tabuaço", "Tarouca", "Tavira", "Terras de Bouro", "Tomar", "Tondela", "Torre de Moncorvo", "Torres Novas", "Torres Vedras", "Trancoso", "Trofa", "Vagos", "Vale de Cambra", "Valença", "Valongo", "Valpaços", "Velas", "Vendas Novas", "Viana do Alentejo", "Viana do Castelo", "Vidigueira", "Vieira do Minho", "Vila Flor", "Vila Franca de Xira", "Vila Franca do Campo", "Vila Nova da Barquinha", "Vila Nova de Cerveira", "Vila Nova de Famalicão", "Vila Nova de Foz Côa", "Vila Nova de Gaia", "Vila Nova de Paiva", "Vila Nova de Poiares", "Vila Pouca de Aguiar", "Vila Real", "Vila Real de Santo António", "Vila Velha de Ródão", "Vila Verde", "Vila Viçosa", "Vila de Rei", "Vila do Bispo", "Vila do Conde", "Vila do Porto", "Vimioso", "Vinhais", "Viseu", "Vizela", "Vouzela"};

        Spinner originInput = view.findViewById(R.id.originInput);
        Spinner destinationInput = view.findViewById(R.id.destinationInput);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, localidades);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, localidades2);
        originInput.setAdapter(adapter);
        destinationInput.setAdapter(adapter2);

        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        LocalDateTime rideDate;

        //handler matriculas
        licenseInput.addTextChangedListener(new TextWatcher() {

            int maxLength = 8;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.length() == 2){
                    editable.append("-");
                }
                if(editable.length() == 5){
                    editable.append("-");
                }

                if(editable.toString().contains("---")){
                    editable.clear();
                }

                // Check if the current length exceeds the maximum length
                if (editable.length() > maxLength) {
                    // Trim the text to the maximum length
                    editable.delete(maxLength, editable.length());
                }
            }
        });

        timeInput.setOnClickListener(v -> {
            DatePickerDialog picker = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                    year = year;
                    month = month;
                    day = day;
                    int finalDay = day;
                    int finalMonth = month+1;
                    int finalYear = year;
                    TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener()
                    {
                        @Override
                        public void onTimeSet(TimePicker timePicker, int houra, int min) {
                            hour = houra;
                            minute = min;
                            timeInput.setText(finalDay + "/" + finalMonth + "/" + finalYear + " " + hour + ":" + minute);
                        }
                    },hour,minute,false);
                    timePickerDialog.show();
                }
            }, year, month,day);
            picker.show();
        });

        locationInput.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("currentLocalGeoPoint", currentLocalGeoPoint);
            MapsFragment mapsFragment = new MapsFragment();
            mapsFragment.setArguments(bundle);
            ((MainActivity)getActivity()).MudarFragmentoPOP(mapsFragment);

            Log.d("MyLocal$", currentLocalGeoPoint.toString());
        });

        createRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String origem = originInput.getSelectedItem().toString();
                String destino = destinationInput.getSelectedItem().toString();
                String matricula = licenseInput.getText().toString();
                String info = infoInput.getText().toString();

                /*
                if(!origem.equals("") && !origem.equals("Origem") && !origem.equals(destino)){
                    if(!destino.equals("") && !destino.equals("Destino") && !destino.equals(origem)){
                        if(!matricula.equals("") && matricula.length() == 8){
                            if(!timeInput.getText().toString().equals("")){
                                DateTimeFormatter formatter = null;
                                LocalDateTime horaSaida = null;
                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m");
                                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                                    horaSaida = LocalDateTime.parse(timeInput.getText().toString(), formatter);
                                }

                                if(!locationInput.getText().toString().equals("")){
                                    Double originLat = originPt.latitude;
                                    Double originLon = originPt.longitude;
                                    if(!capacityInput.getText().toString().equals("")){
                                        int lugares = Integer.parseInt(capacityInput.getText().toString());

                                        String p = "rides";
                                        DatabaseReference databaseReference = firebaseDatabase.getReference(p);
                                        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                List<Integer> ids = new ArrayList<>();
                                                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                                                    User user = childSnapshot.getValue(User.class);
                                                    System.out.println(user.getId());
                                                    ids.add(user.getId());
                                                }

                                                int lastID = Collections.max(ids);

                                                //nova ride
                                                Ride newRide = new Ride();
                                                newRide.setOrigin(origem);
                                                newRide.setDestination(destino);
                                                newRide.setId(lastID+1);
                                                newRide.setInfo(info);
                                                newRide.setProvider(finalCurrentUser.getId());
                                                newRide.setRiderCapacity(lugares);
                                                newRide.setRiderCount(0);
                                                newRide.setRiders("");
                                                newRide.setTime(horaSaida.getDayOfMonth() + "/" + horaSaida.getMonth().getValue() + "/" + horaSaida.getYear() + " " + horaSaida.getHour() + ":" + horaSaida.getMinute() + ":00");
                                                newRide.setLicense(matricula);
                                                newRide.setOriginLat(originLat);
                                                newRide.setOriginLon(originLon);
                                                newRide.setState("ongoing");



                                                String p = "rides/" + newRide.getId();
                                                DatabaseReference databaseReferenceAdd = firebaseDatabase.getReference(p);
                                                databaseReferenceAdd.addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        if(snapshot.exists()){
                                                            System.out.println("Já existe uma boleia com od id " + newRide.getId());

                                                        } else {
                                                            databaseReferenceAdd.setValue(newRide);
                                                            Toast toast = Toast.makeText(getApplicationContext(), "Nova boleia adicionada!", Toast.LENGTH_LONG);
                                                            toast.show();
                                                            System.out.println("Nova boleia adicionada!");
                                                            finish(); //Voltar para o menu principal

                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {

                                                    }
                                                });




                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });
                                    } else {
                                        Toast toast = Toast.makeText(getApplicationContext(), "Capacidade incorreta", Toast.LENGTH_LONG);
                                        toast.show();
                                        System.out.println("Capacidade incorreta");
                                    }
                                } else {
                                    Toast toast = Toast.makeText(getApplicationContext(), "Localização incorreta", Toast.LENGTH_LONG);
                                    toast.show();
                                    System.out.println("Localização incorreta");
                                }
                            } else {
                                Toast toast = Toast.makeText(getApplicationContext(), "Data incorreta", Toast.LENGTH_LONG);
                                toast.show();
                                System.out.println("Data incorreta");
                            }
                        } else {
                            Toast toast = Toast.makeText(getApplicationContext(), "Matrícula incorreta!", Toast.LENGTH_LONG);
                            toast.show();
                            System.out.println("Matrícula incorreta!");
                        }
                    } else {
                        Toast toast = Toast.makeText(getApplicationContext(), "Destino incorreto!", Toast.LENGTH_LONG);
                        toast.show();
                        System.out.println("Destino incorreto!");
                    }
                } else {
                    Toast toast = Toast.makeText(getApplicationContext(), "Origem incorreta!", Toast.LENGTH_LONG);
                    toast.show();
                    System.out.println("Origem incorreta!");
                }*/
            }
        });
    }
}