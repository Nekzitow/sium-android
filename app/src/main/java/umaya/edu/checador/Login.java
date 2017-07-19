package umaya.edu.checador;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

import umaya.edu.checador.Services.Utilidades;
import umaya.edu.checador.models.Configurations;
import umaya.edu.checador.models.DBHelper;
import umaya.edu.checador.models.Notificaciones;
import umaya.edu.checador.models.RequestSingleton;

public class Login extends AppCompatActivity {
    //creamos todas las variables
    private TextView txt;
    private EditText usuario;
    private EditText password;
    private Button login;
    private RadioGroup mRadioGroup;
    //tipo login = 2 por default es docente 1 e admon
    private int tipoLogin = 2;//docente

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private SharedPreferences preferences;

    public static final String TAG = "Login";
    private boolean loggedIn = false;
    //Variables de volley
    private RequestQueue queue;

    private ProgressDialog progressBar;

    private int tipo;

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
        tipo = getIntent().getIntExtra("tipo",0);
        progressBar = new ProgressDialog(this);
        SharedPreferences preferences = getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        loggedIn = preferences.getBoolean(Configurations.LOGGEDIN_SHARED_PREF, false);
        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup1);
        getExtras();
        if (loggedIn) {
            launchActivity(Principal.class);
        }else{
            mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                    switch (i){
                        case R.id.radioDocente:
                            tipoLogin = 2;
                            break;
                        case R.id.radioAdmon:
                            tipoLogin = 1;
                            break;
                    }
                }
            });
            firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
            defaultConfig();
        }
    }

    public void validateUser() {
        //Obtenemos el usuario y contraseña de login
        final String userName = usuario.getText().toString().trim();
        final String passwordField = password.getText().toString().trim();
        if (userName.equals("") || passwordField.equals("")){
            if (userName.equals(""))
                usuario.setError("Usuario Requerido");
            if (passwordField.equals(""))
                password.setError("Ingrese su contraseña");
        }else {
            queue = RequestSingleton.getInstance(this.getApplicationContext()).getRequestQueue();
            //hacemos el login
            progressBar.setMessage("Iniciado sesión");
            progressBar.show();
            JSONObject s = new JSONObject();
            JSONArray js = new JSONArray();
            try {
                switch (tipoLogin){
                    case 1:
                        s.put(Configurations.KEY_USER, userName+"@universidadmaya.edu.mx");
                        break;
                    case 2:
                        s.put(Configurations.KEY_USER, userName);
                        break;
                }
                s.put(Configurations.KEY_PASSWORD, passwordField);
                js.put(0, s);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.w(TAG,js.toString());
            SharedPreferences preferences = getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
            String url2 = preferences.getString(Configurations.SHARED_URL, "http://192.168.1.4");
            Log.d(TAG,url2);
            //comparamos el tipo de usuario para preparar la url a enviar
            switch (tipoLogin){
                case 1:
                    url2+=Configurations.LOGIN_URL_ADMON;
                    break;
                case 2:
                    url2+=Configurations.loginUrl;
                    break;
            }

            Log.i(TAG,url2);
            final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                    (Request.Method.POST, url2, js, new Response.Listener<JSONArray>() {

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
                            Toast.makeText(getApplicationContext(), "No se establecio conexión con el servidor"+error.getMessage(), Toast.LENGTH_SHORT).show();
                            //Log.e(TAG, error.getMessage());
                            defaultConfig();
                        }
                    });
            //agregamos la peticion
            queue.add(jsonObjectRequest);
        }


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
            String user = "";
            if (tipoLogin == 1)
                user = usuarios.getString("email");
            else
                user = usuarios.getString("usuario");
            int id = dbHelper.showMatchUser(sqLiteDatabaseRead, Integer.parseInt(personal.getString("id")));
            switch (id) {
                case 0:
                    setPreferences(personal.getString("nombre"), personal.getInt("id"));
                    dbHelper.insertLogin(sqLiteDatabase, personal, user);
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
        editor.putInt(Configurations.LOGIN_TIPE_USER,tipoLogin);
        editor.commit();
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
            preferences = Login.this.getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void getExtras() {
        Notificaciones notificaciones = new Notificaciones();
        if (getIntent().getExtras() != null) {
            for (String key : getIntent().getExtras().keySet()) {
                Object value = getIntent().getExtras().get(key);
                if (key.equalsIgnoreCase("titulo")) {
                    notificaciones.setTitulo(value.toString());
                    Log.i(TAG,value.toString());
                    getIntent().removeExtra(key);
                }

                if (key.equalsIgnoreCase("cuerpo") ) {
                    notificaciones.setContenido(value.toString());
                    Log.i(TAG,value.toString());
                    getIntent().removeExtra(key);
                }
                if (key.equalsIgnoreCase("date") ) {
                    notificaciones.setExtra(value.toString());
                    Log.i(TAG,value.toString());
                    getIntent().removeExtra(key);
                }
            }
            notificaciones.setFecha(Utilidades.obtenerFecha());
            if (notificaciones.getTitulo() == null) {
            }else{
                Log.i(TAG,"ENTRO");
                DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
                SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
                dbHelper.insertNotifications(sqLiteDatabase, notificaciones);
            }

        }
    }

}
