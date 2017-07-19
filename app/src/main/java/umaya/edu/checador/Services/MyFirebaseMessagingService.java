package umaya.edu.checador.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import umaya.edu.checador.Principal;
import umaya.edu.checador.R;
import umaya.edu.checador.models.DBHelper;
import umaya.edu.checador.models.Notificaciones;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "Messaging Service";

    public void onMessageReceived(RemoteMessage remoteMessage) {
        Log.d(TAG, "¡Mensaje recibido!");
        displayNotification(remoteMessage.getNotification(), remoteMessage.getData());
        Notificaciones notificaciones = new Notificaciones();
        notificaciones.setTitulo(remoteMessage.getNotification().getTitle());
        notificaciones.setContenido(remoteMessage.getNotification().getBody());
        notificaciones.setExtra(remoteMessage.getData().get("date"));
        notificaciones.setFecha(Utilidades.obtenerFecha());
        insertNotificacion(notificaciones);
        sendNewPromoBroadcast(remoteMessage,Utilidades.obtenerFecha());
    }

    private void sendNewPromoBroadcast(RemoteMessage remoteMessage,String fecha) {
        Intent intent = new Intent(Principal.ACTION_NOTIFY_NEW);
        intent.putExtra("titulo", remoteMessage.getNotification().getTitle());
        intent.putExtra("subtitulo", remoteMessage.getNotification().getBody());
        intent.putExtra("date", remoteMessage.getData().get("date"));
        intent.putExtra("fecha", fecha);
        LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(intent);
    }

    private void insertNotificacion(Notificaciones notificaciones){
        DBHelper dbHelper = DBHelper.getInstance(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = dbHelper.getWritableDatabase();
        dbHelper.insertNotifications(sqLiteDatabase,notificaciones);
    }

    private void displayNotification(RemoteMessage.Notification notification, Map<String, String> data) {
        Intent intent = new Intent(this, Principal.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.iconcheck)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setPriority(2)
                .setSound(defaultSoundUri)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, notificationBuilder.build());
    }
}
