package umaya.edu.checador;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.zxing.Result;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import umaya.edu.checador.models.Configurations;
import umaya.edu.checador.models.CustomDialog;
import umaya.edu.checador.models.RequestSingleton;

public class CameraPreview extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private ZXingScannerView zXingScannerView;
    private RequestQueue queue;
    private ProgressDialog progressBar;

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

        final JsonArrayRequest jsonObjectRequest = new JsonArrayRequest
                (Request.Method.POST,Configurations.CHECK_URL,js, new Response.Listener<JSONArray>(){

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
                        Toast.makeText(getApplicationContext(), "Error en la conexión"+error.toString(),Toast.LENGTH_SHORT).show();
                    }
                });
        //agregamos la peticion
        queue.add(jsonObjectRequest);

    }
}
