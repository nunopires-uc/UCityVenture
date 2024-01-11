package com.example.ucityventure;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.provider.Settings;

public class CommonClass {


    //Classe de funções estáticas que são utilizadas ao longo do projecto

    //Função que avalia se existe conexão à internet
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network nw = connectivityManager.getActiveNetwork();
            if (nw == null) return false;
            NetworkCapabilities actNw = connectivityManager.getNetworkCapabilities(nw);
            return actNw != null && (actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                    actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH));
        } else {
            //Verifica se os tipos de conexão não são nulos, existindo conexão à internet
            if (connectivityManager != null) {
                Network[] networks = connectivityManager.getAllNetworks();
                for (Network network : networks) {
                    NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(network);
                    if (networkCapabilities != null &&
                            (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                                    networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH))) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    //Função para verificar se existe GPS disponível
    public static boolean isGPSAvailable(Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            return true;
        } else {
            return false;
        }
    }
}
