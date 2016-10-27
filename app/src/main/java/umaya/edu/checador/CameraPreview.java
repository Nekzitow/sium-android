package umaya.edu.checador;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import umaya.edu.checador.models.Configurations;
import umaya.edu.checador.models.CustomDialog;
import umaya.edu.checador.models.RequestSingleton;

public class CameraPreview extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView zXingScannerView;
    private RequestQueue queue;
    private ProgressDialog progressBar;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private SharedPreferences preferences;

    public static final String TAG = "LectorQR";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_preview);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.queue = RequestSingleton.getInstance(getApplicationContext()).getRequestQueue();
        progressBar = new ProgressDialog(this);
        readQR();
    }

    private void readQR(){
        zXingScannerView = new ZXingScannerView(this);

        setContentView(zXingScannerView);
        zXingScannerView.setResultHandler(this);
        zXingScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (zXingScannerView != null){
            zXingScannerView.stopCamera();
        }
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.e("handler", rawResult.getText()); // Prints scan results
        Log.e("handler", rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode)
        // show the scanner result into dialog box.
        /*android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Resultados");
        builder.setMessage(rawResult.getText());
        android.app.AlertDialog alert1 = builder.create();
        alert1.show();*/
        validateUser(rawResult.getText());
        // If you would like to resume scanning, call this method below:
        //zXingScannerView.resumeCameraPreview(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home){
            NavUtils.navigateUpFromSameTask(this);
        }
        return true;
    }

    public void validateUser(String idUser){
        //Obtenemos el usuario y contraseña de login
        //hacemos el login
        progressBar.setMessage("Verificando Asistencia");
        progressBar.show();
        JSONObject s = new JSONObject();
        JSONArray js = new JSONArray();
        try {
            s.put(Configurations.SHARED_ID,idUser);
            js.put(0,s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences preferences = getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String url = preferences.getString(Configurations.SHARED_URL, "http://192.168.1.4");
        Log.d(TAG,url);
        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.POST,url+Configurations.CHECK_URL,js, new Response.Listener<JSONArray>(){

                    @Override
                    public void onResponse(JSONArray response) {
                        progressBar.dismiss();
                        Log.i(TAG,response.toString());
                        try {
                            progressBar.dismiss();
                            JSONObject objeto = response.getJSONObject(0);
                            int value = objeto.getInt("respuesta");
                            CustomDialog customDialog = new CustomDialog();
                            customDialog.showDialogo(CameraPreview.this, value);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        progressBar.dismiss();
                        Toast.makeText(getApplicationContext(), "No se establecio conexion con el servidor: Intente de nuevo", Toast.LENGTH_LONG).show();
                        defaultConfig();

                    }
                });
        //agregamos la peticion
        queue.add(jsonObjectRequest);

    }

    /**
     * Obtener la configuracion para determinar la longitud alojada para los mensaje
     */
    public void fetchConfig() {
        long cacheExpiration = 3600; // 1 hora en segundos
        // If developer mode is enabled reduce cacheExpiration to 0 so that
        // each fetch goes to the server. This should not be used in release
        // builds.
        // en el caso que este activado el modo desarrolador la expiracion del cache se reduce a 0
        if (firebaseRemoteConfig.getInfo().getConfigSettings()
                .isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        firebaseRemoteConfig.activateFetched();
                        Log.d(TAG,"SEE");
                        applyUrlConfig();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG,"Error al obtener la configuración: " +
                                e.getMessage());
                        applyUrlConfig();
                    }
                });
    }

    /**
     * Aplicamos la configuracion obtenida
     */
    private void applyUrlConfig(){
        String urlConnect = firebaseRemoteConfig.getString("URL_CONNECT");
        if (preferences == null){
            preferences = CameraPreview.this.getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            Log.d(TAG,"ENTRO");
        }
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Configurations.SHARED_URL,urlConnect);
        editor.commit();
        Log.d(TAG,"URL obtenida: " + urlConnect);
    }

    private void defaultConfig(){
        // Definir las configuracion remotas
        FirebaseRemoteConfigSettings firebaseRemoteConfigSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setDeveloperModeEnabled(true)
                        .build();
        // Definimos los valores de configuracion por defecto. Los valores predeterminados
        // se utilizan cuando los valores de configuracion no estan disponibles.
        // Por ejemplo si se ha producido un error al obtener los valores del servidor
        Map<String, Object> defaultConfigMap = new HashMap<>();
        //valor predeterminado en el caso que no se obtenga los valores de configuracion
        defaultConfigMap.put("URL_CONNECT", "http://192.168.1.4");

        //aplicamos las configuraciones y los valores prederteminado
        firebaseRemoteConfig.setConfigSettings(firebaseRemoteConfigSettings);
        firebaseRemoteConfig.setDefaults(defaultConfigMap);

        //obtenemos configuracion remota
        fetchConfig();
    }
}
