package umaya.edu.checador.models;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by OSORIO on 17/10/2016.
 */
public class RequestSingleton {
    private static RequestSingleton instance;
    private RequestQueue requestQueue;
    private static Context context;

    /**
     * inicializa la instancia
     * @param context
     */
    private RequestSingleton(Context context){
        this.context = context;
        requestQueue = getRequestQueue();
    }

    /**
     * retorna la instancia de la clase
     * @param context
     * @return
     */
    public static synchronized RequestSingleton getInstance(Context context){
        if (instance == null){
            instance = new RequestSingleton(context);
        }
        return instance;
    }

    /**
     * Obtiene la instancia de Volley
     * @return
     */
    public RequestQueue getRequestQueue(){
        if (requestQueue == null){
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
}
