package umaya.edu.checador.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * Created by OSORIO on 16/01/2017.
 */

public class Utilidades {
    public static String LOG_TAG = "Utils";
    static public boolean isNetworkAviable(Context context){
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
    }

    static public boolean isActiveInternetConnection(Context context,URL url){
        if (isNetworkAviable(context)) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (url.openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error checking internet connection", e);
            }
        } else {
            Log.d(LOG_TAG, "No network available!");
        }
        return false;
    }

    public static String getMonth(int mes){
        String mesActual = "";
        switch (mes){
            case 0:
                mesActual = "ENE";
                break;
            case 1:
                mesActual = "FEB";
                break;
            case 2:
                mesActual = "MAR";
                break;
            case 3:
                mesActual = "ABR";
                break;
            case 4:
                mesActual = "MAY";
                break;
            case 5:
                mesActual = "JUN";
                break;
            case 6:
                mesActual = "JUL";
                break;
            case 7:
                mesActual = "AGO";
                break;
            case 8:
                mesActual = "SEP";
                break;
            case 9:
                mesActual = "OCT";
                break;
            case 10:
                mesActual = "NOV";
                break;
            case 11:
                mesActual = "DIC";
                break;
        }
        return mesActual;
    }


    public static void sendToastMessageLong(Context context, String Message){
        Toast.makeText(context, Message, Toast.LENGTH_LONG).show();
    }

    public static String obtenerFecha(){
        Calendar fechaActual = Calendar.getInstance();
        String mes = Utilidades.getMonth(fechaActual.get(Calendar.MONTH));
        String dia = fechaActual.get(Calendar.DATE) < 10 ? "0"+fechaActual.get(Calendar.DATE) : fechaActual.get(Calendar.DATE)+"";
        String fecha = dia+" "+mes;
        return fecha;
    }
}
