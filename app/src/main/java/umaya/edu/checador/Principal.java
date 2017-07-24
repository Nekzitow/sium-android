package umaya.edu.checador;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.maps.android.PolyUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import umaya.edu.checador.Notifications.PushNotificationsContract;
import umaya.edu.checador.Notifications.PushNotificationsPresenter;
import umaya.edu.checador.Services.GetCurrentLocation;
import umaya.edu.checador.Services.Utilidades;
import umaya.edu.checador.models.Configurations;
import umaya.edu.checador.Notifications.PrincipalAdapter;
import umaya.edu.checador.models.Coordenadas;
import umaya.edu.checador.models.DBHelper;
import umaya.edu.checador.models.Empleado;
import umaya.edu.checador.models.Notificaciones;
import umaya.edu.checador.models.Plantel;

public class Principal extends AppCompatActivity implements PushNotificationsContract.View,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private ZXingScannerView zXingScannerView;
    private static final String TAG = "principal";
    private static final int ZXING_CAMERA_PERMISSION = 1;
    public static final String ACTION_NOTIFY_NEW = "NOTIFY_NEW";
    private static final int zoom = 100;
    private ArrayList<Plantel> mPlantelArrayList;
    private Class<?> mClss;

    private PrincipalAdapter principalAdapter;
    private BroadcastReceiver broadcastReceiver;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoMessagesView;
    private LinearLayout mNoNetworkConnected;
    private PushNotificationsPresenter mPresenter;
    private PushNotificationsPresenter mNotificationsPresenter;
    private ArrayList<Notificaciones> notificaciones = new ArrayList<Notificaciones>();
    private ArrayList<LatLng> arrayPoints = new ArrayList<LatLng>();
    private ArrayList<LatLng> arrayPointsEstacionamiento = new ArrayList<LatLng>();
    protected GoogleApiClient mGoogleApiClient;
    protected Location mLastLocation;
    private TextView nombreDocente;
    private TextView identificador;
    private TextView mLastcheck;
    GetCurrentLocation currentLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        initCollapsingToolbar();
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if(comparaRango()){
                launchActivity(CameraPreview.class);
                //}
            }
        });
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String title = intent.getStringExtra("titulo");
                String description = intent.getStringExtra("subtitulo");
                String expiryDate = intent.getStringExtra("date");
                String discount = intent.getStringExtra("fecha");
                mPresenter.savePushMessage(title, description, expiryDate, discount);
            }
        };
        nombreDocente = (TextView) findViewById(R.id.nombreDocente);
        identificador = (TextView) findViewById(R.id.identificador);
        mLastcheck = (TextView) findViewById(R.id.ultimoCheck);
        principalAdapter = new PrincipalAdapter(notificaciones);
        mRecyclerView = (RecyclerView) findViewById(R.id.notifications);
        mNoMessagesView = (LinearLayout) findViewById(R.id.noMessages);
        mNoNetworkConnected = (LinearLayout) findViewById(R.id.noNetwork);
        mRecyclerView.setAdapter(principalAdapter);
        mNotificationsPresenter = new PushNotificationsPresenter(
                this, FirebaseMessaging.getInstance());
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        llenarRecycler();
        llenarPlanteles();
        buildGoogleApiClient();
        currentLoc = new GetCurrentLocation(this);
        setUserData();
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuLogout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
        LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(broadcastReceiver, new IntentFilter(ACTION_NOTIFY_NEW));
        getLastCheck();
        try {
            new TaskConnection().execute(new URL("http://www.universidadmaya.edu.mx"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        //mPresenter.start();
        LocalBroadcastManager.getInstance(getApplication())
                .registerReceiver(broadcastReceiver, new IntentFilter(ACTION_NOTIFY_NEW));
        notificaciones.clear();
        principalAdapter.notifyDataSetChanged();
        principalAdapter.clear();
    }

    private void logout() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage("¿Seguro que quieres cerrar sesión?");
        alertDialogBuilder.setPositiveButton("Sí",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        //Getting out sharedpreferences
                        SharedPreferences preferences = getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                        //Getting editor
                        SharedPreferences.Editor editor = preferences.edit();

                        //Puting the value false for loggedin
                        editor.putBoolean(Configurations.LOGGEDIN_SHARED_PREF, false);
                        editor.putString(Configurations.SHARED_NAME, "");
                        editor.putInt(Configurations.SHARED_ID, 0);
                        //Saving the sharedpreferences
                        editor.commit();

                        //Starting login activity
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        finish();
                    }
                });

        alertDialogBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                    }
                });

        //Showing the alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplication())
                .unregisterReceiver(broadcastReceiver);
        notificaciones.clear();
        principalAdapter.notifyDataSetChanged();
        principalAdapter.clear();
    }

    public void launchActivity(Class<?> clss) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            mClss = clss;
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, ZXING_CAMERA_PERMISSION);
        } else {
            Intent intent = new Intent(this, clss);
            startActivity(intent);
        }
    }


    @Override
    public void showNotifications(ArrayList<Notificaciones> notifications) {
        principalAdapter.replaceData(notifications);
    }

    @Override
    public void showEmptyState(boolean empty) {
        mNoNetworkConnected.setVisibility(View.GONE);
        mRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        mNoMessagesView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void popPushNotification(Notificaciones pushMessage) {
        principalAdapter.addItem(pushMessage);
    }

    @Override
    public void setPresenter(PushNotificationsContract.Presenter presenter) {
        if (presenter != null) {
            mPresenter = (PushNotificationsPresenter) presenter;
        } else {
            throw new RuntimeException("El presenter de notificaciones no puede ser null");
        }
    }

    public void llenarRecycler() {
        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
        notificaciones = dbHelper.getAllNotifications();
        mPresenter.clearNotifications();
        for (Notificaciones noti : notificaciones) {
            Log.i(TAG, "tamaño de lista: " + noti.getContenido());
            mPresenter.savePushMessage(noti.getTitulo(), noti.getContenido(), noti.getExtra(), noti.getFecha());
        }
    }

    public void llenarPlanteles() {
        //COORDENADAS PARA PLANTEL TUXTLA CENTRO
        arrayPoints.add(new LatLng(16.749883, -93.136911));
        arrayPoints.add(new LatLng(16.749938, -93.137286));
        arrayPoints.add(new LatLng(16.750039, -93.137441));
        arrayPoints.add(new LatLng(16.750335, -93.137358));
        arrayPoints.add(new LatLng(16.750281, -93.137070));
        arrayPoints.add(new LatLng(16.750582, -93.136885));
        arrayPoints.add(new LatLng(16.750969, -93.136705));
        arrayPoints.add(new LatLng(16.751145, -93.136621));
        arrayPoints.add(new LatLng(16.751040, -93.136137));
        arrayPoints.add(new LatLng(16.750741, -93.136222));
        arrayPoints.add(new LatLng(16.750793, -93.136510));
        arrayPoints.add(new LatLng(16.750320, -93.136751));

        //ESTACIONAMIENTO
        arrayPointsEstacionamiento.add(new LatLng(16.749687, -93.138211));
        arrayPointsEstacionamiento.add(new LatLng(16.749470, -93.138269));
        arrayPointsEstacionamiento.add(new LatLng(16.749484, -93.138811));
        arrayPointsEstacionamiento.add(new LatLng(16.749750, -93.138717));
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {


    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
        currentLoc.connectGoogleApi();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
        currentLoc.disConnectGoogleApi();
    }

    public void obtenerPosicion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mLastLocation.setLatitude(Double.parseDouble(currentLoc.latitude));
            mLastLocation.setLongitude(Double.parseDouble(currentLoc.longitude));
            Toast.makeText(getApplicationContext(),
                    "Latitud: " + mLastLocation.getLatitude() + ". Longitud: "
                            + mLastLocation.getLongitude(), Toast.LENGTH_LONG)
                    .show();
            Log.i(TAG, "Latitud: " + mLastLocation.getLatitude() + ". Longitud: " + mLastLocation.getLongitude());
        }


    }

    public boolean comparaRango() {
        boolean respuesta = false;
        obtenerPosicion();
        if (mLastLocation != null) {
            LatLng miUbicacion = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            if (PolyUtil.containsLocation(miUbicacion, arrayPoints, true)) {
                respuesta = true;
            } else {
                if (PolyUtil.containsLocation(miUbicacion, arrayPointsEstacionamiento, true)) {
                    respuesta = true;
                } else {
                    respuesta = false;
                }
            }

        }

        return respuesta;
    }

    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(" ");
        AppBarLayout appBarLayout = (AppBarLayout) findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        // hiding & showing the title when toolbar expanded & collapsed
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbar.setTitle(getString(R.string.app_name));
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbar.setTitle(" ");
                    isShow = false;
                }
            }
        });
    }

    private void setUserData() {
        DBHelper mDbHelper = DBHelper.getInstance(getApplicationContext());
        SQLiteDatabase mSqLiteDatabase = mDbHelper.getReadableDatabase();
        Empleado empleados = mDbHelper.getEmpleado(mSqLiteDatabase);
        nombreDocente.setText(empleados.getNombre().trim());
        identificador.setText("Número de código: " + empleados.getIdenti() + "");
        getLastCheck();
    }

    private void getLastCheck() {
        SharedPreferences preferences = getSharedPreferences(Configurations.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        String url = preferences.getString(Configurations.SHARED_CHECK, "");
        mLastcheck.setText("Último registro: " + url + "");
    }

    public class TaskConnection extends AsyncTask<URL, Void, Boolean> {

        @Override
        protected Boolean doInBackground(URL... urls) {
            URL mUrl = urls[0];
            boolean verdadero = false;
            if (!Utilidades.isActiveInternetConnection(getApplicationContext(), mUrl)) {
                //no hay internet
                verdadero = true;
            }
            return verdadero;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (aBoolean) {
                mNoNetworkConnected.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
                mNoMessagesView.setVisibility(View.GONE);
            }
        }
    }
}
