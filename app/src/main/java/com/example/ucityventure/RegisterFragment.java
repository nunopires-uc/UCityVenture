package com.example.ucityventure;

import static android.content.ContentValues.TAG;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;



import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RegisterFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RegisterFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    EditText nameInput, passInput, passConfirmInput, emailInput;
    Button registerButton;

    ProgressBar progressBar;

    FirebaseAuth mAuth;

    FirebaseFirestore db;

    public RegisterFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RegisterFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RegisterFragment newInstance(String param1, String param2) {
        RegisterFragment fragment = new RegisterFragment();
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
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        nameInput = view.findViewById(R.id.nameInput);
        passInput = view.findViewById(R.id.passInput);
        passConfirmInput = view.findViewById(R.id.passConfirmInput);
        emailInput = view.findViewById(R.id.emailInput);
        registerButton = view.findViewById(R.id.registerButton);
        progressBar = view.findViewById(R.id.progressBar);


        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //Verifica se existe conexão à internet, caso não exista, redireciona o utilizador para ligar o wifi
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

        //Componentes
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Recebe os valores das EditTexts
                String name = nameInput.getText().toString();
                String password = passInput.getText().toString();
                String passwordConfirm = passConfirmInput.getText().toString();
                String email = emailInput.getText().toString();

                progressBar.setVisibility(View.VISIBLE);

                //Tamanho minimo de password
                int password_minsize = 6;
                //Verificação dos valores das EditTexts
                if(!name.equals("") && !password.equals("") && !passwordConfirm.equals("")) {
                    if(password.equals(passwordConfirm)){
                        if(password.length() >= password_minsize) {
                            //Criar novo utilizador na base de dados
                            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(requireActivity(), new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {

                                    if (task.isSuccessful()) {
                                        //Definir os vários parâmetros do utilizador
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        User userClass = new User();
                                        userClass.setEmail(email);
                                        userClass.setId(user.getUid());
                                        userClass.setNumRatings(0);
                                        userClass.setSomaRatings(0.0f);
                                        userClass.setNome(name);

                                        // Add a new document with a generated ID
                                        db.collection("users").document(user.getUid())
                                                .set(userClass)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        progressBar.setVisibility(View.GONE);
                                                        Log.d(TAG, "DocumentSnapshot added with ID: " + user.getUid());
                                                        Snackbar.make(view, "User created successfully", Snackbar.LENGTH_LONG).show();

                                                        //Assim que a conta é criada o utilizador é redirecionado para o menu principal da aplicação
                                                        ((MainActivity)getActivity()).MudarFragmento(new MainFragment());
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.w(TAG, "Error adding document", e);
                                                        Snackbar.make(view, "Error creating user", Snackbar.LENGTH_LONG).show();
                                                    }
                                                });
                                    } else {
                                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Snackbar.make(view, "Error creating user", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                            });

                        }else{
                            System.out.println("Tamanho incorreto de password!");
                            Toast toast = Toast.makeText(getActivity().getApplicationContext(), "A password deve conter pelo menos " + password_minsize + " caracteres!", Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }else{
                        System.out.println("Password não correspondem!");
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Passwords não correspondem!", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }else{
                    System.out.println("Campos em branco");
                    Toast toast = Toast.makeText(getActivity().getApplicationContext(), "Campos vazios!", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        });

    }




}