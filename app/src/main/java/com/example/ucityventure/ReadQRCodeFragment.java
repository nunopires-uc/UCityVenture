package com.example.ucityventure;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.Manifest;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ReadQRCodeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ReadQRCodeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final int PERMISSION_REQUEST_CAMERA = 1;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ReadQRCodeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ReadQRCodeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ReadQRCodeFragment newInstance(String param1, String param2) {
        ReadQRCodeFragment fragment = new ReadQRCodeFragment();
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

        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                startQRScanner();
            }
        } else {
            startQRScanner();
        }
    }

    private void startQRScanner() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActivityResultLauncher<Intent> scanQrResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                resultData -> {
                    if (resultData.getResultCode() == RESULT_OK) {
                        ScanIntentResult result = ScanIntentResult.parseActivityResult(resultData.getResultCode(), resultData.getData());

                        if (result.getContents() == null) {
                            Toast.makeText(getContext(), "Scan cancelled", Toast.LENGTH_LONG).show();
                        } else {
                            String qrContents = result.getContents();
                            // Handle the scanned QR code here.
                            Log.d("XYYZE", qrContents);
                            Toast.makeText(getActivity(), "Scanned: " + qrContents, Toast.LENGTH_LONG).show();
                        }
                    }
                });

        // Customize the prompt message by setting a custom text
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

        View view = inflater.inflate(R.layout.fragment_read_q_r_code, container, false);

        // Set the layout orientation to portrait mode
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Reset the orientation to allow system handling
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }

}