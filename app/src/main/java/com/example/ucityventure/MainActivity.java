package com.example.ucityventure;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    //caçador de ciganos
    //Olá
    //mequie

    String Root_Frag = "root_fagment";

    public void CarregarFragmento(Fragment fragment_name) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.add(R.id.FL, fragment_name);

        fm.popBackStack(Root_Frag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ft.addToBackStack(Root_Frag);
        ft.commit();
    }

    public void MudarFragmento(Fragment fragment_name){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.FL, fragment_name);

        fm.popBackStack(Root_Frag, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        ft.addToBackStack(Root_Frag);

        ft.commit();
        invalidateOptionsMenu();
    }

    public void PopCurrentFragment() {
        FragmentManager fm = getSupportFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
            fm.popBackStack();
            invalidateOptionsMenu();
        }
    }


    //Para ser possível voltar atrás
    public void MudarFragmentoPOP(Fragment fragment_name){
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        ft.replace(R.id.FL, fragment_name);
        ft.addToBackStack(null); // add this line

        ft.commit();
        invalidateOptionsMenu();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*
        Contas default:
        joserojas@gmail.com
        joserojas

         */

        CarregarFragmento(new LoginFragment());
    }
}