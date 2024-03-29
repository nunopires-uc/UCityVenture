package com.example.ucityventure;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RecoverPasswordFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecoverPasswordFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View v;

    Button recoverButton;

    EditText emailInput;

    public RecoverPasswordFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment RecoverPasswordFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RecoverPasswordFragment newInstance(String param1, String param2) {
        RecoverPasswordFragment fragment = new RecoverPasswordFragment();
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
        v = inflater.inflate(R.layout.recover_password_fragment, container, false);

        recoverButton = v.findViewById(R.id.recoverButton);
        emailInput = v.findViewById(R.id.emailInput);

        //Botao de recuperação de password
        recoverButton.setOnClickListener(v -> {
            if(!emailInput.getText().toString().equals("")){
                FirebaseAuth auth = FirebaseAuth.getInstance();
                //Recebe a string obtida pela EditText
                String emailAddress = emailInput.getText().toString();

                //Envia um email de recuperação para o utilizador
                auth.sendPasswordResetEmail(emailAddress)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Snackbar.make(v, "E-mail enviado! Siga as instruções para recuperar a sua palavra-passe!", Snackbar.LENGTH_LONG).show();
                                } else {
                                    //Se o email não existir ou for inválido apresenta uma mensagem de erro
                                    Snackbar.make(v, "E-mail inválido ou incorreto!", Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });

                ((MainActivity)getActivity()).PopCurrentFragment();
            }
        });



        return v;
    }
}