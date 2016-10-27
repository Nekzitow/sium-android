package umaya.edu.checador;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import umaya.edu.checador.models.Configurations;
import umaya.edu.checador.models.DBHelper;
import umaya.edu.checador.models.RequestSingleton;

public class Login extends AppCompatActivity {
    //creamos todas las variables
    private TextView txt;
    private EditText usuario;
    private EditText password;
    private Button login;

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private SharedPreferences preferences;

    public static final String TAG = "Login";
    private boolean loggedIn = false;
    //Variables de volley
    private RequestQueue queue;

    private ProgressDialog progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //inicializamos
        usuario = (EditText) findViewById(R.id.usuario);
        password = (EditText) findViewById(R.id.passwoord);
        login = (Button) findViewById(R.id.b_sign_in);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUser();
            }
        });
        progressBar = new ProgressDialog(this);
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        defaultConfig();
    }

    public void validateUser() {
        //Obtenemos el usuario y contraseña de login
        final String userName = usuario.getText().toString().trim();
        final String passwordField = password.getText().toString().trim();
        queue = RequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
        //hacemos el login
        progressBar.setMessage("Iniciado sesión");
        progressBar.show();
        JSONObject s = new JSONObject();
        JSONArray js = new JSONArray();
        try {
            s.put(Configurations.KEY_USER, userName);
            s.put(Configurations.KEY_PASSWORD, passwordField);
            js.put(0, s);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences preferences = getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String url2 = preferences.getString(Configurations.SHARED_URL, "http://192.168.1.4");
        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.POST, url2+Configurations.loginUrl, js, new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            progressBar.dismiss();
                            JSONObject objeto = response.getJSONObject(0);
                            if (objeto.getString("response").equalsIgnoreCase(Configurations.LOGIN_SUCCESS)) {
                                JSONObject Users = response.getJSONArray(1).getJSONObject(0);
                                JSONObject Empleado = response.getJSONArray(2).getJSONObject(0);
                                DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                                checkUser(Users, Empleado, dbHelper);
                            } else {
                                Toast.makeText(getApplicationContext(), objeto.getString("response") + " Intentelo Nuevamente", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        progressBar.dismiss();
                        Toast.makeText(getApplicationContext(), "No se establecio conexion con el servidor", Toast.LENGTH_SHORT).show();
                    }
                });
        //agregamos la peticion
        queue.add(jsonObjectRequest);

    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences preferences = getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        loggedIn = preferences.getBoolean(Configurations.LOGGEDIN_SHARED_PREF, false);
        if (loggedIn) {
            launchActivity(Principal.class);
        }
    }

    public void launchActivity(Class<?> clss) {
        Intent intent = new Intent(this, clss);
        startActivity(intent);
        finish();
    }

    public void checkUser(JSONObject usuarios, JSONObject personal, DBHelper dbHelper) {
        SQLiteDatabase sqLiteDatabaseRead = dbHelper.getReadableDatabase();
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        try {
            int id = dbHelper.showMatchUser(sqLiteDatabaseRead, Integer.parseInt(personal.getString("id")));
            switch (id) {
                case 0:
                    setPreferences(personal.getString("nombre"), personal.getInt("id"));
                    dbHelper.insertLogin(sqLiteDatabase, personal, usuarios.getString("usuario"));
                    Toast.makeText(getApplicationContext(), "Bienvenido: " + personal.getString("nombre"), Toast.LENGTH_SHORT).show();
                    launchActivity(Principal.class);
                    break;
                case 1:
                    Toast.makeText(getApplicationContext(), "Cuenta no asociada a este dispositivo", Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    setPreferences(personal.getString("nombre"), personal.getInt("id"));
                    Toast.makeText(getApplicationContext(), "Bienvenido de Nuevo: " + personal.getString("nombre"), Toast.LENGTH_SHORT).show();
                    launchActivity(Principal.class);
                    break;
                default:
                    break;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setPreferences(String nombre,int id){
        preferences = Login.this.getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Configurations.LOGGEDIN_SHARED_PREF, true);
        editor.putString(Configurations.SHARED_NAME, nombre);
        editor.putInt(Configurations.SHARED_ID, id);
        editor.commit();
        //preferencias de configuracion
        fetchConfig();
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
        if (preferences != null){
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(Configurations.SHARED_URL,urlConnect);
            editor.commit();
        }
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
