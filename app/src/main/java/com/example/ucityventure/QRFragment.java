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
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

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
                                String qrContents = result.getContents();
                                // Handle the scanned QR code here

                                CollectionReference transactionsCollection = db.collection("transactions");
                                DocumentReference documentRef = transactionsCollection.document("MLRkVMuqJElVYxbD9jxP");
                                Random random = new Random();
                                PIN = String.format("%04d", random.nextInt(10000));

                                //PIN userID readID status
                                String newQueueElement = PIN + "+" + "hb546ehn7e5j85e78jjn4" + "+" + qrContents + "+" + "0";
                                documentRef.update("queue", FieldValue.arrayUnion(newQueueElement))
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                // Handle success
                                                Log.d("Firestore", "Element added to the queue successfully");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Handle failure
                                                Log.e("Firestore", "Error adding element to the queue", e);
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
        ScanOptions scanOptions = new ScanOptions();
        scanOptions.setOrientationLocked(true);
        scanOptions.setDesiredBarcodeFormats(String.valueOf(BarcodeFormat.QR_CODE));
        scanOptions.setPrompt("");
        scanQrResultLauncher.launch(new ScanContract().createIntent(getContext(), scanOptions));
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    startQRScanner();
                } else {
                    Toast.makeText(getActivity(), "Camera permission is required", Toast.LENGTH_LONG).show();
                }
            });

    private final ActivityResultLauncher<Intent> activityResultLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    Log.d("Dados", data.toString());
                }
            });

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


        MultiFormatWriter mWriter = new MultiFormatWriter();
        try {
            //BitMatrix class to encode entered text and set Width & Height
            BitMatrix mMatrix = mWriter.encode("456g74h56h7we4h76weh764we", BarcodeFormat.QR_CODE, 400,400);
            BarcodeEncoder mEncoder = new BarcodeEncoder();
            Bitmap mBitmap = mEncoder.createBitmap(mMatrix);//creating bitmap of code
            imageCode.setImageBitmap(mBitmap);//Setting generated QR code to imageView
            // to hide the keyboard
            /*InputMethodManager manager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            manager.hideSoftInputFromWindow(etText.getApplicationWindowToken(), 0);*/
        } catch (WriterException e) {
            e.printStackTrace();
        }

        createRideButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startQRScanner();
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Reset the orientation to allow system handling
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}