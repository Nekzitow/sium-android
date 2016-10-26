package umaya.edu.checador;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import umaya.edu.checador.models.Configurations;
import umaya.edu.checador.models.DBHelper;
import umaya.edu.checador.models.RequestSingleton;

public class Login extends AppCompatActivity {
    //creamos todas las variables
    private TextView txt;
    private EditText usuario;
    private EditText password;
    private Button login;

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

        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.POST, Configurations.loginUrl, js, new Response.Listener<JSONArray>() {

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
                        Toast.makeText(getApplicationContext(), "Error en la conexión" + error.getMessage(), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getApplicationContext(), "Bienvenido de nuevo: " + personal.getString("nombre"), Toast.LENGTH_SHORT).show();
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
        SharedPreferences preferences = Login.this.getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Configurations.LOGGEDIN_SHARED_PREF, true);
        editor.putString(Configurations.SHARED_NAME, nombre);
        editor.putInt(Configurations.SHARED_ID, id);
        editor.commit();
    }

}
