package com.example.ucityventure;

import static android.app.Activity.RESULT_OK;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.List;
import java.util.Random;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link QRFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class QRFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int PERMISSION_REQUEST_CAMERA = 1;

    FirebaseFirestore db;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ImageView imageViewStatus;

    String uuid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

    TextView CorrectHitchhikeText;

    ProgressBar progressBar;

    private MutableLiveData<String> pinLiveData = new MutableLiveData<>();

    String PIN;

    Button createRideButton;

    public QRFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment QRFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static QRFragment newInstance(String param1, String param2) {
        QRFragment fragment = new QRFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private final ActivityResultLauncher<Intent> scanQrResultLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    resultData -> {
                        if (resultData.getResultCode() == RESULT_OK) {
                            ScanIntentResult result = ScanIntentResult.parseActivityResult(resultData.getResultCode(), resultData.getData());

                            if (result.getContents() == null) {
                                Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_LONG).show();
                            } else {
                                // analisar conteudo capturado pela camera
                                String qrContents = result.getContents();

                                //Criar um pin aleatorio
                                Random random = new Random();
                                PIN = String.format("%04d", random.nextInt(10000));

                                //PIN userID readID status
                                //String newQueueElement = PIN + "+" + "hb546ehn7e5j85e78jjn4" + "+" + qrContents + "+" + "0";

                                //Scan confirmation element
                                ScanConfirmation scelement = new ScanConfirmation();
                                scelement.setPIN(PIN);
                                scelement.setUserID(uuid);
                                scelement.setProviderID(qrContents);
                                scelement.setStatus("0");

                                //Colocar na coleção a transação
                                db.collection("myqrconfirmations")
                                        .document(scelement.getPIN()) // Use the random ID as the document ID
                                        .set(scelement)       // Set the data for the document
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                progressBar.setVisibility(View.VISIBLE);
                                                pinLiveData.setValue(PIN);
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(getActivity().getCurrentFocus(), "Erro ao dar ler QR", Snackbar.LENGTH_LONG).show();
                                            }
                                        });
                                Toast.makeText(getActivity(), "Scanned: " + qrContents, Toast.LENGTH_LONG).show();
                            }
                        }
                    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            }
        }
    }

    private void startQRScanner() {
        //Aspeto visual do qrscanner, e opções
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setOrientationLocked(true);
        scanOptions.setDesiredBarcodeFormats(String.valueOf(BarcodeFormat.QR_CODE));
        scanOptions.setPrompt("");
        //iniciar o Qr scanner
        scanQrResultLauncher.launch(new ScanContract().createIntent(getContext(), scanOptions));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_q_r, container, false);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        return view;
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        ImageView imageCode = view.findViewById(R.id.imageCode);
        createRideButton = view.findViewById(R.id.createRideButton);
        imageViewStatus = view.findViewById(R.id.confirmationStatus);
        CorrectHitchhikeText = view.findViewById(R.id.CorrectHitchhikeText);
        progressBar = view.findViewById(R.id.progressBar);

        MultiFormatWriter mWriter = new MultiFormatWriter();
        try {
            //Criar um qrcode com base no id do utilizador logado
            BitMatrix mMatrix = mWriter.encode(uuid, BarcodeFormat.QR_CODE, 400,400);
            BarcodeEncoder mEncoder = new BarcodeEncoder();
            //Criar um mapa de bits do código
            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);
            //Definir o código QR gerado para a imageView
            imageCode.setImageBitmap(mBitmap);
            // Para ocultar o teclado
            /*InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(etText.getApplicationWindowToken(), 0);*/
        } catch (WriterException e) {
            e.printStackTrace();
        }

        //Iniciar o leitor
        createRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQRScanner();
            }
        });

        //Observador da transação gerada
        pinLiveData.observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String pin) {
                //fica à escuta com o pin da transação
                DocumentReference docRef = db.collection("myqrconfirmations").document(pin);
                docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot snapshot,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("QRFragment", "Listen failed.", e);
                            return;
                        }

                        if (snapshot != null && snapshot.exists()) {
                            Log.d("MyPIN", pin.toString());
                            Log.d("QRFragment", "Current data: " + snapshot.getData());

                            //retira o estado da transação, que foi manipulado pelo servidor
                            String status = snapshot.get("status").toString();

                            progressBar.setVisibility(View.VISIBLE);

                            //verificar o estado da transação, 0 o servidor não fez nenhuma modificação
                            if(!status.equals("0")){
                                progressBar.setVisibility(View.GONE);
                                //Se o estado for -1 então a boleia nao pertence à pessoa
                                if(status.equals("-1")){
                                    CorrectHitchhikeText.setText("A boleia está incorreta!");
                                    imageViewStatus.setImageResource(R.drawable.baseline_error_24);
                                    CorrectHitchhikeText.setVisibility(View.VISIBLE);
                                    imageViewStatus.setVisibility(View.VISIBLE);
                                }else{
                                    //Caso a boleia esteja correta apresenta imagem de sucesso
                                    CorrectHitchhikeText.setVisibility(View.VISIBLE);
                                    imageViewStatus.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            Log.d("QRFragment", "Current data: null");
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Redefinir a orientação
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}