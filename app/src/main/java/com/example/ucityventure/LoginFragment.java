package com.example.ucityventure;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LoginFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LoginFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    //Variáveis de elementos de UI

    EditText emailInput, passwordInput;
    Button loginButton;

    FirebaseAuth mAuth;

    TextView registerText, forgotPasswordText;

    ProgressBar progressBar;

    public LoginFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment LoginFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static LoginFragment newInstance(String param1, String param2) {
        LoginFragment fragment = new LoginFragment();
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
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emailInput = view.findViewById(R.id.emailInput);
        passwordInput = view.findViewById(R.id.passwordInput);
        loginButton = view.findViewById(R.id.loginButton);
        registerText = view.findViewById(R.id.registerText);
        progressBar = view.findViewById(R.id.progressBar);
        forgotPasswordText = view.findViewById(R.id.forgotPasswordText);

        mAuth = FirebaseAuth.getInstance();

        //Utilizador default para não estar sempre a inserir email e password (para testes)
        emailInput.setText("dj8@uc.pt");
        passwordInput.setText("123456");

        //Verificar se existe internet disponivel
        if(!CommonClass.isNetworkAvailable(getContext())){
            new AlertDialog.Builder(getActivity())
                    .setTitle("Não existe conexão à internet, deseja ligar o Wifi?")
                    .setPositiveButton("Ligar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            Intent turnWifiOn = new Intent(Settings.ACTION_WIFI_SETTINGS);
                            startActivity(turnWifiOn);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            getActivity().finishAndRemoveTask();
                        }
                    })
                    .show();
        }

        //Verificar se o gps está ligado
        if(!CommonClass.isGPSAvailable(getContext())){
            new AlertDialog.Builder(getActivity())
                    .setTitle("O GPS não está ativo, deseja ligar o GPS?")
                    .setPositiveButton("Ligar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // GPS is not enabled, redirect user to settings
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            getActivity().finishAndRemoveTask();
                        }
                    })
                    .show();
        }

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                //efetuar login
                mAuth.signInWithEmailAndPassword(emailInput.getText().toString(), passwordInput.getText().toString())
                        .addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                progressBar.setVisibility(View.GONE);
                                if (task.isSuccessful()) {
                                    if(getActivity() instanceof MainActivity){
                                        ((MainActivity)getActivity()).MudarFragmento(new MainFragment());
                                    }
                                } else {
                                    Snackbar.make(view, "Invalid email or password", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        });

        //Mudar para o fragmento de registo
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).MudarFragmentoPOP(new RegisterFragment());
            }
        });

        forgotPasswordText.setOnClickListener(v -> {
            ((MainActivity)getActivity()).MudarFragmentoPOP(new RecoverPasswordFragment());
        });
    }
}