package umaya.edu.checador;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import umaya.edu.checador.Notifications.PushNotificationsContract;
import umaya.edu.checador.Notifications.PushNotificationsPresenter;
import umaya.edu.checador.models.Configurations;
import umaya.edu.checador.Notifications.PrincipalAdapter;
import umaya.edu.checador.models.Notificaciones;

public class Principal extends AppCompatActivity implements PushNotificationsContract.View{
    private ZXingScannerView zXingScannerView;
    private static final int ZXING_CAMERA_PERMISSION = 1;
    public static final String ACTION_NOTIFY_NEW = "NOTIFY_NEW";
    private Class<?> mClss;

    private PrincipalAdapter principalAdapter;
    private BroadcastReceiver broadcastReceiver;
    private RecyclerView mRecyclerView;
    private LinearLayout mNoMessagesView;
    private PushNotificationsPresenter mPresenter;
    private PushNotificationsPresenter mNotificationsPresenter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchActivity(CameraPreview.class);
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
        principalAdapter = new PrincipalAdapter();
        mRecyclerView = (RecyclerView) findViewById(R.id.notifications);
        mNoMessagesView = (LinearLayout) findViewById(R.id.noMessages);
        mRecyclerView.setAdapter(principalAdapter);
        mNotificationsPresenter = new PushNotificationsPresenter(
                this, FirebaseMessaging.getInstance());
    }


    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
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
    }

    private void logout(){
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
                        editor.putString(Configurations.SHARED_NAME,"");
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
        mRecyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        mNoMessagesView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    @Override
    public void popPushNotification(Notificaciones pushMessage) {
        principalAdapter.addItem(pushMessage);
    }

    @Override
    public void setPresenter(PushNotificationsContract.Presenter presenter) {
        if (presenter != null){
            mPresenter = (PushNotificationsPresenter) presenter;
        }else{
            throw new RuntimeException("El presenter de notificaciones no puede ser null");
        }
    }
}
