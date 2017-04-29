package umaya.edu.checador.Services;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by OSORIO on 16/01/2017.
 */

public class Utilidades {
    static public boolean isNetworkAviable(Context context){
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null
                && activeNetwork.isConnectedOrConnecting();
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
}
