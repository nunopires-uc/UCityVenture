package com.example.ucityventure;

import static com.example.ucityventure.CommonClass.isGPSAvailable;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationRequest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Looper;
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

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
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

    //Componentes referentes à interface de utilizador
    EditText timeInput, locationInput, licenseInput, capacityInput, infoInput;
    Button createRideButton;
    Spinner originInput, destinationInput;

    //declaração de um sharedviewmodel, que manipula CompoundLocation e GeoPoints
    private SharedViewModel model;

    //Variaveis que serão usadas no calendário
    private int year, month, day, hour, minute;

    //id do utilizador logado
    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

    //armazena a primeira localização do utilizador, e de seguida a que escolheu no mapa
    GeoPoint currentLocalGeoPoint;

    //Declaração da variável para manipular a firestore
    FirebaseFirestore db;

    //Variável de localização atual
    Location currentLocation;

    private FusedLocationProviderClient fusedLocationClient;


    //Variáveis para guardar latitude e longitude
    Double Latitude, Longitude;

    public CreateRideFragment() {
        // Required empty public constructor
    }


    //Função para resolver a última localização do utilizador
    private Location getLastKnownLocation() {
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return null;
            }
            Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    //Encontrar a localização do utilizador
    public void find_Location(Context con) {
        locationManager = (LocationManager) con.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0, new LocationListener() {
                public void onLocationChanged(Location location) {
                }

                public void onProviderDisabled(String provider) {
                }

                public void onProviderEnabled(String provider) {
                }

                public void onStatusChanged(String provider, int status, Bundle extras) {
                }
            });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                currentLocation.setLatitude(location.getLatitude());
                currentLocation.setLongitude(location.getLongitude());
            }
        }
    }


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
        //Reter a localização do utilizador
        locationManager = (LocationManager) getActivity().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        currentLocation = getLastKnownLocation();
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        db = FirebaseFirestore.getInstance();

        //Encontrar a localização, do utilizador, são feitos vários pedidos de localização para não falhar quando se mostra a verdadeira localização do utilizador.
        find_Location(getContext());

        //Verificar se foram dadaas permissões para aceder à localização
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

        //Verificar se o gps está disponivel
        if(isGPSAvailable(getContext())){
            //Este passo server para tentar resolver mais uma vez a localização do utilizador, e colocá-la num geopoint que vai ser visualizado no mapa
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
            }else {
                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Ocorreu um erro", Toast.LENGTH_LONG);
                toast.show();
                return;
            }

            if (currentLocation != null) {
                //Se a localização atual não for nula então colocar no geopoint a latitude e longitude do utilizador.
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
                });
            }
        }


        // instanciar o viewmodel
        model = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // ficar à escuta se foi colocada alguma CompoundLocation no viewmodel (isto acontece dentro do MapsFragment)
        model.getSelectedCompoundLocation().observe(getViewLifecycleOwner(), cl -> {
            //Mudar a cor do ícone da localização
            locationInput.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_baseline_gps_fixed_24_blue, 0, 0, 0);
            //Colocar no editText a morada da localização escolhida
            locationInput.setText(cl.getLocationAddress().getAddressLine(0).toString());
            Latitude = cl.getLatitude();
            Longitude = cl.getLongitude();
        });



        //spinner com localidades de origem e destino
        String[] localidades = {"Origem", "Águeda", "Aguiar da Beira", "Alandroal", "Albergaria-a-Velha", "Albufeira", "Alcácer do Sal", "Alcanena", "Alcobaça", "Alcochete", "Alcoutim", "Alenquer", "Alfândega da Fé", "Alijó", "Aljezur", "Aljustrel", "Almada", "Almeida", "Almeirim", "Almodôvar", "Alpiarça", "Alter do Chão", "Alvaiázere", "Alvito", "Amadora", "Amarante", "Amares", "Anadia", "Angra do Heroísmo", "Ansião", "Arcos de Valdevez", "Arganil", "Armamar", "Arouca", "Arraiolos", "Arronches", "Arruda dos Vinhos", "Aveiro", "Avis", "Azambuja", "Baião", "Barcelos", "Barrancos", "Barreiro", "Batalha", "Beja", "Belmonte", "Benavente", "Bombarral", "Borba", "Boticas", "Braga", "Bragança", "Cabeceiras de Basto", "Cadaval", "Caldas da Rainha", "Calheta (Madeira)", "Calheta (São Jorge)", "Caminha", "Campo Maior", "Cantanhede", "Carrazeda de Ansiães", "Carregal do Sal", "Cartaxo", "Cascais", "Castanheira de Pêra", "Castelo Branco", "Castelo de Paiva", "Castelo de Vide", "Castro Daire", "Castro Marim", "Castro Verde", "Celorico da Beira", "Celorico de Basto", "Chamusca", "Chaves", "Cinfães", "Coimbra", "Condeixa-a-Nova", "Constância", "Coruche", "Corvo", "Covilhã", "Crato", "Cuba", "Câmara de Lobos", "Elvas", "Entroncamento", "Espinho", "Esposende", "Estarreja", "Estremoz", "Évora", "Fafe", "Faro", "Felgueiras", "Ferreira do Alentejo", "Ferreira do Zêzere", "Figueira da Foz", "Figueira de Castelo Rodrigo", "Figueiró dos Vinhos", "Fornos de Algodres", "Freixo de Espada à Cinta", "Fronteira", "Funchal", "Fundão", "Gavião", "Golegã", "Gondomar", "Gouveia", "Grândola", "Guarda", "Guimarães", "Góis", "Horta", "Idanha-a-Nova", "Ílhavo", "Lagoa (Algarve)", "Lagoa (São Miguel)", "Lagos", "Lajes das Flores", "Lajes do Pico", "Lamego", "Leiria", "Lisboa", "Loulé", "Loures", "Lourinhã", "Lousã", "Lousada", "Mação", "Macedo de Cavaleiros", "Machico", "Madalena", "Mafra", "Maia", "Mangualde", "Manteigas", "Marco de Canaveses", "Marinha Grande", "Marvão", "Matosinhos", "Mealhada", "Meda", "Melgaço", "Mesão Frio", "Mira", "Miranda do Corvo", "Miranda do Douro", "Mirandela", "Mogadouro", "Moimenta da Beira", "Moita", "Monção", "Monchique", "Mondim de Basto", "Monforte", "Montalegre", "Montemor-o-Novo", "Montemor-o-Velho", "Montijo", "Mora", "Mortágua", "Moura", "Mourão", "Murça", "Murtosa", "Mértola", "Nazaré", "Nelas", "Nisa", "Nordeste", "Óbidos", "Odemira", "Odivelas", "Oeiras", "Oleiros", "Olhão", "Oliveira de Azeméis", "Oliveira de Frades", "Oliveira do Bairro", "Oliveira do Hospital", "Ourique", "Ourém", "Ovar", "Paços de Ferreira", "Palmela", "Pampilhosa da Serra", "Paredes", "Paredes de Coura", "Pedrógão Grande", "Penacova", "Penafiel", "Penalva do Castelo", "Penamacor", "Penedono", "Penela", "Peniche", "Peso da Régua", "Pinhel", "Pombal", "Ponta Delgada", "Ponta do Sol", "Ponte da Barca", "Ponte de Lima", "Ponte de Sor", "Portalegre", "Portel", "Portimão", "Porto", "Porto Moniz", "Porto Santo", "Porto de Mós", "Povoação", "Praia da Vitória", "Proença-a-Nova", "Póvoa de Lanhoso", "Póvoa de Varzim", "Redondo", "Reguengos de Monsaraz", "Resende", "Ribeira Brava", "Ribeira Grande", "Ribeira de Pena", "Rio Maior", "Sabrosa", "Sabugal", "Salvaterra de Magos", "Santa Comba Dão", "Santa Cruz", "Santa Cruz da Graciosa", "Santa Cruz das Flores", "Santa Maria da Feira", "Santa Marta de Penaguião", "Santana", "Santarém", "Santiago do Cacém", "Santo Tirso", "São Brás de Alportel", "São João da Madeira", "São João da Pesqueira", "São Pedro do Sul", "São Roque do Pico", "São Vicente", "Sardoal", "Sátão", "Seia", "Seixal", "Sernancelhe", "Serpa", "Sertã", "Sesimbra", "Setúbal", "Sever do Vouga", "Silves", "Sines", "Sintra", "Sobral de Monte Agraço", "Soure", "Sousel", "Tábua", "Tabuaço", "Tarouca", "Tavira", "Terras de Bouro", "Tomar", "Tondela", "Torre de Moncorvo", "Torres Novas", "Torres Vedras", "Trancoso", "Trofa", "Vagos", "Vale de Cambra", "Valença", "Valongo", "Valpaços", "Velas", "Vendas Novas", "Viana do Alentejo", "Viana do Castelo", "Vidigueira", "Vieira do Minho", "Vila Flor", "Vila Franca de Xira", "Vila Franca do Campo", "Vila Nova da Barquinha", "Vila Nova de Cerveira", "Vila Nova de Famalicão", "Vila Nova de Foz Côa", "Vila Nova de Gaia", "Vila Nova de Paiva", "Vila Nova de Poiares", "Vila Pouca de Aguiar", "Vila Real", "Vila Real de Santo António", "Vila Velha de Ródão", "Vila Verde", "Vila Viçosa", "Vila de Rei", "Vila do Bispo", "Vila do Conde", "Vila do Porto", "Vimioso", "Vinhais", "Viseu", "Vizela", "Vouzela"};
        String[] localidades2 = {"Destino", "Águeda", "Aguiar da Beira", "Alandroal", "Albergaria-a-Velha", "Albufeira", "Alcácer do Sal", "Alcanena", "Alcobaça", "Alcochete", "Alcoutim", "Alenquer", "Alfândega da Fé", "Alijó", "Aljezur", "Aljustrel", "Almada", "Almeida", "Almeirim", "Almodôvar", "Alpiarça", "Alter do Chão", "Alvaiázere", "Alvito", "Amadora", "Amarante", "Amares", "Anadia", "Angra do Heroísmo", "Ansião", "Arcos de Valdevez", "Arganil", "Armamar", "Arouca", "Arraiolos", "Arronches", "Arruda dos Vinhos", "Aveiro", "Avis", "Azambuja", "Baião", "Barcelos", "Barrancos", "Barreiro", "Batalha", "Beja", "Belmonte", "Benavente", "Bombarral", "Borba", "Boticas", "Braga", "Bragança", "Cabeceiras de Basto", "Cadaval", "Caldas da Rainha", "Calheta (Madeira)", "Calheta (São Jorge)", "Caminha", "Campo Maior", "Cantanhede", "Carrazeda de Ansiães", "Carregal do Sal", "Cartaxo", "Cascais", "Castanheira de Pêra", "Castelo Branco", "Castelo de Paiva", "Castelo de Vide", "Castro Daire", "Castro Marim", "Castro Verde", "Celorico da Beira", "Celorico de Basto", "Chamusca", "Chaves", "Cinfães", "Coimbra", "Condeixa-a-Nova", "Constância", "Coruche", "Corvo", "Covilhã", "Crato", "Cuba", "Câmara de Lobos", "Elvas", "Entroncamento", "Espinho", "Esposende", "Estarreja", "Estremoz", "Évora", "Fafe", "Faro", "Felgueiras", "Ferreira do Alentejo", "Ferreira do Zêzere", "Figueira da Foz", "Figueira de Castelo Rodrigo", "Figueiró dos Vinhos", "Fornos de Algodres", "Freixo de Espada à Cinta", "Fronteira", "Funchal", "Fundão", "Gavião", "Golegã", "Gondomar", "Gouveia", "Grândola", "Guarda", "Guimarães", "Góis", "Horta", "Idanha-a-Nova", "Ílhavo", "Lagoa (Algarve)", "Lagoa (São Miguel)", "Lagos", "Lajes das Flores", "Lajes do Pico", "Lamego", "Leiria", "Lisboa", "Loulé", "Loures", "Lourinhã", "Lousã", "Lousada", "Mação", "Macedo de Cavaleiros", "Machico", "Madalena", "Mafra", "Maia", "Mangualde", "Manteigas", "Marco de Canaveses", "Marinha Grande", "Marvão", "Matosinhos", "Mealhada", "Meda", "Melgaço", "Mesão Frio", "Mira", "Miranda do Corvo", "Miranda do Douro", "Mirandela", "Mogadouro", "Moimenta da Beira", "Moita", "Monção", "Monchique", "Mondim de Basto", "Monforte", "Montalegre", "Montemor-o-Novo", "Montemor-o-Velho", "Montijo", "Mora", "Mortágua", "Moura", "Mourão", "Murça", "Murtosa", "Mértola", "Nazaré", "Nelas", "Nisa", "Nordeste", "Óbidos", "Odemira", "Odivelas", "Oeiras", "Oleiros", "Olhão", "Oliveira de Azeméis", "Oliveira de Frades", "Oliveira do Bairro", "Oliveira do Hospital", "Ourique", "Ourém", "Ovar", "Paços de Ferreira", "Palmela", "Pampilhosa da Serra", "Paredes", "Paredes de Coura", "Pedrógão Grande", "Penacova", "Penafiel", "Penalva do Castelo", "Penamacor", "Penedono", "Penela", "Peniche", "Peso da Régua", "Pinhel", "Pombal", "Ponta Delgada", "Ponta do Sol", "Ponte da Barca", "Ponte de Lima", "Ponte de Sor", "Portalegre", "Portel", "Portimão", "Porto", "Porto Moniz", "Porto Santo", "Porto de Mós", "Povoação", "Praia da Vitória", "Proença-a-Nova", "Póvoa de Lanhoso", "Póvoa de Varzim", "Redondo", "Reguengos de Monsaraz", "Resende", "Ribeira Brava", "Ribeira Grande", "Ribeira de Pena", "Rio Maior", "Sabrosa", "Sabugal", "Salvaterra de Magos", "Santa Comba Dão", "Santa Cruz", "Santa Cruz da Graciosa", "Santa Cruz das Flores", "Santa Maria da Feira", "Santa Marta de Penaguião", "Santana", "Santarém", "Santiago do Cacém", "Santo Tirso", "São Brás de Alportel", "São João da Madeira", "São João da Pesqueira", "São Pedro do Sul", "São Roque do Pico", "São Vicente", "Sardoal", "Sátão", "Seia", "Seixal", "Sernancelhe", "Serpa", "Sertã", "Sesimbra", "Setúbal", "Sever do Vouga", "Silves", "Sines", "Sintra", "Sobral de Monte Agraço", "Soure", "Sousel", "Tábua", "Tabuaço", "Tarouca", "Tavira", "Terras de Bouro", "Tomar", "Tondela", "Torre de Moncorvo", "Torres Novas", "Torres Vedras", "Trancoso", "Trofa", "Vagos", "Vale de Cambra", "Valença", "Valongo", "Valpaços", "Velas", "Vendas Novas", "Viana do Alentejo", "Viana do Castelo", "Vidigueira", "Vieira do Minho", "Vila Flor", "Vila Franca de Xira", "Vila Franca do Campo", "Vila Nova da Barquinha", "Vila Nova de Cerveira", "Vila Nova de Famalicão", "Vila Nova de Foz Côa", "Vila Nova de Gaia", "Vila Nova de Paiva", "Vila Nova de Poiares", "Vila Pouca de Aguiar", "Vila Real", "Vila Real de Santo António", "Vila Velha de Ródão", "Vila Verde", "Vila Viçosa", "Vila de Rei", "Vila do Bispo", "Vila do Conde", "Vila do Porto", "Vimioso", "Vinhais", "Viseu", "Vizela", "Vouzela"};

        Spinner originInput = view.findViewById(R.id.originInput);
        Spinner destinationInput = view.findViewById(R.id.destinationInput);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, localidades);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(), R.layout.spinner_item, localidades2);
        originInput.setAdapter(adapter);
        destinationInput.setAdapter(adapter2);

        //Definição dos dados do calendário
        final Calendar c = Calendar.getInstance();
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);
        hour = c.get(Calendar.HOUR_OF_DAY);
        minute = c.get(Calendar.MINUTE);
        LocalDateTime rideDate;

        //handler matriculas para apenas permitir o formato português
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

        //Click listener do calendário para escolher a data da boleia
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

        //Mudar para o fragmento MapsFragment
        locationInput.setOnClickListener(v -> {
            if (currentLocalGeoPoint != null) {
                Bundle bundle = new Bundle();
                //"Enviar" o geopoint da localização do utilizador para o MapsFragment
                bundle.putParcelable("currentLocalGeoPoint", currentLocalGeoPoint);
                MapsFragment mapsFragment = new MapsFragment();
                mapsFragment.setArguments(bundle);
                ((MainActivity)getActivity()).MudarFragmentoPOP(mapsFragment);
            } else {
                //Caso o geopoint seja nulo, tentar encontrar outra vez a localização do utilizador e mudar para o MapsFragment
                Log.d("MyLocal$", "Cannot navigate to MapsFragment because current local geo point is null");
                find_Location(getContext());
                currentLocalGeoPoint = new GeoPoint(currentLocation.getLatitude(), currentLocation.getLongitude());
                if(currentLocation != null){
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("currentLocalGeoPoint", currentLocalGeoPoint);
                    MapsFragment mapsFragment = new MapsFragment();
                    mapsFragment.setArguments(bundle);
                    ((MainActivity)getActivity()).MudarFragmentoPOP(mapsFragment);
                }
            }
        });

        //Criar a boleia
        createRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String origem = originInput.getSelectedItem().toString();
                String destino = destinationInput.getSelectedItem().toString();
                String matricula = licenseInput.getText().toString();
                String info = infoInput.getText().toString();


                //Verificar se os dados de input estão preparados para ser armazenados
                if(!origem.equals("") && !origem.equals("Origem") && !origem.equals(destino)){
                    if(!destino.equals("") && !destino.equals("Destino") && !destino.equals(origem)){
                        if(!matricula.equals("") && matricula.length() == 8){
                            if(!timeInput.getText().toString().equals("")){
                                DateTimeFormatter formatter = null;
                                LocalDateTime horaSaida = null;
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    formatter = DateTimeFormatter.ofPattern("d/M/yyyy H:m");
                                    DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                                    horaSaida = LocalDateTime.parse(timeInput.getText().toString(), formatter);
                                }

                                //Verificar se uma localização foi guardada
                                if(!locationInput.getText().toString().equals("")){
                                    //Verificar se foi introduzido o número de lugares vagos no carro
                                    if(!capacityInput.getText().toString().equals("")){
                                        int lugares = Integer.parseInt(capacityInput.getText().toString());
                                        //Criar a nova boleia
                                        Ride newRide = new Ride();
                                        newRide.setOrigin(origem);
                                        newRide.setDestination(destino);
                                        newRide.setInfo(info);
                                        newRide.setTime(String.valueOf(horaSaida));
                                        newRide.setProvider(uuid);
                                        newRide.setRideCapacity(lugares);
                                        newRide.setRidePassangers(new ArrayList<>());
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            newRide.setTime(horaSaida.getDayOfMonth() + "/" + horaSaida.getMonth().getValue() + "/" + horaSaida.getYear() + " " + horaSaida.getHour() + ":" + horaSaida.getMinute() + ":00");
                                        }
                                        newRide.setLicense(matricula);
                                        newRide.setOriginLat(Latitude);
                                        newRide.setOriginLon(Longitude);
                                        newRide.setState("ongoing");

                                        //gera id aleatorio
                                        String randomId = db.collection("rides").document().getId();
                                        newRide.setId(randomId);

                                        //armazena a boleia na coleção rides do firestore
                                        db.collection("rides")
                                                .document(randomId)
                                                .set(newRide)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //Mensagem de sucesso de criação de boleia
                                                        Log.d("CreateRideFragment", "Boleia criada com sucesso " + randomId);
                                                        Snackbar.make(view, "Boleia criada com sucesso", Snackbar.LENGTH_LONG).show();

                                                        DocumentReference docRef = db.collection("transactions").document("num_rides");
                                                        // Aumenta atomicamente o campo 'num' do documento em 1.
                                                        docRef.update("num", FieldValue.increment(1));
                                                        // Sair do fragmento atual
                                                        ((MainActivity)getActivity()).PopCurrentFragment();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w("CreateRideFragment", "Erro ao criar boleia", e);
                                                        Snackbar.make(view, "Erro ao criar boleia", Snackbar.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Capacidade incorreta", Toast.LENGTH_LONG);
                                        toast.show();
                                        System.out.println("Capacidade incorreta");
                                    }
                                } else {
                                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Localização incorreta", Toast.LENGTH_LONG);
                                    toast.show();
                                    System.out.println("Localização incorreta");
                                }
                            } else {
                                Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Data incorreta", Toast.LENGTH_LONG);
                                toast.show();
                                System.out.println("Data incorreta");
                            }
                        } else {
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Matrícula incorreta!", Toast.LENGTH_LONG);
                            toast.show();
                            System.out.println("Matrícula incorreta!");
                        }
                    } else {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Destino incorreto!", Toast.LENGTH_LONG);
                        toast.show();
                        System.out.println("Destino incorreto!");
                    }
                } else {
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Origem incorreta!", Toast.LENGTH_LONG);
                    toast.show();
                    System.out.println("Origem incorreta!");
                }
            }
        });
    }
}