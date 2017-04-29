package umaya.edu.checador.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.ColorMatrix;
import android.util.Log;

import com.google.android.gms.actions.NoteIntents;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by OSORIO on 25/10/2016.
 */

public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    //instancia DB para el patron singleton
    private static DBHelper instance;
    private static final int DB_VERSION = 1;
    //nombre de la base de datos
    private static final String DB_NAME = "sium";
    //nombre de la tabla
    private static final String TABLE_NAME = "login";
    private static final String TABLE_NAME_NOTIFICATION = "notificaciones";
    //Columnas
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_USER = "usuario";
    private static final String COLUMN_NOMBRE = "nombre";
    private static final String COLUMN_IDREMOTE = "id_user";

    //columnas de notificaciones
    private static final String COLUMN_FECHA = "fecha";
    private static final String COLUMN_TITULO = "titulo";
    private static final String COLUMN_CONTENIDO = "contenido";
    private static final String COLUMN_REMITENTE = "remitente";

    public static synchronized DBHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DBHelper(context);
        }
        return instance;
    }

    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(createTableLogin());
        sqLiteDatabase.execSQL(createTableNotifications());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(dropTableLogin());
        onCreate(sqLiteDatabase);
    }

    private String createTableLogin() {
        StringBuffer CREATE_LOGIN = new StringBuffer("CREATE TABLE ");
        CREATE_LOGIN.append(TABLE_NAME);
        CREATE_LOGIN.append("(");

        CREATE_LOGIN.append(COLUMN_ID);
        CREATE_LOGIN.append(" INTEGER PRIMARY KEY,");

        CREATE_LOGIN.append(COLUMN_USER);
        CREATE_LOGIN.append(" TEXT,");

        CREATE_LOGIN.append(COLUMN_NOMBRE);
        CREATE_LOGIN.append(" TEXT,");


        CREATE_LOGIN.append(COLUMN_IDREMOTE);
        CREATE_LOGIN.append(" INTEGER UNIQUE)");

        return CREATE_LOGIN.toString();
    }

    private String createTableNotifications(){
        StringBuffer CREATE_NOTIFICATIONS = new StringBuffer("CREATE TABLE ");
        CREATE_NOTIFICATIONS.append(TABLE_NAME_NOTIFICATION);
        CREATE_NOTIFICATIONS.append("(");

        CREATE_NOTIFICATIONS.append(COLUMN_ID);
        CREATE_NOTIFICATIONS.append(" INTEGER PRIMARY KEY,");

        CREATE_NOTIFICATIONS.append(COLUMN_FECHA);
        CREATE_NOTIFICATIONS.append(" TEXT,");

        CREATE_NOTIFICATIONS.append(COLUMN_TITULO);
        CREATE_NOTIFICATIONS.append(" TEXT,");

        CREATE_NOTIFICATIONS.append(COLUMN_CONTENIDO);
        CREATE_NOTIFICATIONS.append(" TEXT,");


        CREATE_NOTIFICATIONS.append(COLUMN_REMITENTE);
        CREATE_NOTIFICATIONS.append(" TEXT)");

        return CREATE_NOTIFICATIONS.toString();
    }

    private String dropTableLogin() {
        StringBuffer DROP_LOGIN = new StringBuffer("DROP TABLE IF EXISTS ");
        DROP_LOGIN.append(TABLE_NAME);
        return DROP_LOGIN.toString();
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public void insertLogin(SQLiteDatabase sqLiteDatabase, JSONObject empleados, String user) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_NOMBRE, empleados.getString("nombre"));
            contentValues.put(COLUMN_USER, user);
            contentValues.put(COLUMN_IDREMOTE, empleados.getString("id"));
            long id = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void insertNotifications(SQLiteDatabase sqLiteDatabase, Notificaciones notificaciones){
        try{
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_FECHA,notificaciones.getFecha());
            contentValues.put(COLUMN_TITULO,notificaciones.getTitulo());
            contentValues.put(COLUMN_CONTENIDO,notificaciones.getContenido());
            contentValues.put(COLUMN_REMITENTE,notificaciones.getExtra());
            long id = sqLiteDatabase.insert(TABLE_NAME_NOTIFICATION,null,contentValues);
            Log.i(TAG,"se guardo con id: "+id);
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
    }

    public int showMatchUser(SQLiteDatabase sqLiteDatabase, int idUser) {
        int respuesta = 0;
        Cursor c,c1,c2;
        c = sqLiteDatabase.rawQuery("SELECT count(*) FROM login", null);
        c.moveToFirst();
        if (c.getInt(0) > 0){
            //significa que si hay resultados
            c1 = sqLiteDatabase.rawQuery("SELECT * FROM login WHERE id_user=?", new String[]{Integer.toString(idUser)});

            if (c1.moveToFirst()) {
                respuesta = 2;
            } else {
                respuesta = 1;
            }
        }else{
            //usuario nuevo
            respuesta = 0;
        }
        return respuesta;
    }

    public Empleado getEmpleado(SQLiteDatabase sqLiteDatabase){
        Empleado mEmpleado = new Empleado();
        Cursor c;
        c = sqLiteDatabase.rawQuery("SELECT * FROM login",null);
        while (c.moveToNext()){
            mEmpleado.setId(c.getInt(c.getColumnIndexOrThrow(COLUMN_ID)));
            mEmpleado.setIdenti(c.getInt(c.getColumnIndexOrThrow(COLUMN_IDREMOTE)));
            mEmpleado.setNombre(c.getString(c.getColumnIndexOrThrow(COLUMN_NOMBRE)));
        }
        c.close();
        sqLiteDatabase.close();
        return mEmpleado;
    }

    public ArrayList<Notificaciones> getAllNotifications(){
        SQLiteDatabase db = getReadableDatabase();
        ArrayList<Notificaciones> notificaciones = new ArrayList<Notificaciones>();
        try{
            String[] columnas = {COLUMN_ID, COLUMN_FECHA, COLUMN_TITULO, COLUMN_CONTENIDO, COLUMN_REMITENTE};
            String orderBy = COLUMN_ID+" ASC";
            Cursor c = db.query(TABLE_NAME_NOTIFICATION,columnas,null,null,null,null,orderBy);
            while (c.moveToNext()){
                Notificaciones not = new Notificaciones();
                not.setId(c.getInt(c.getColumnIndex(COLUMN_ID))+"");
                not.setFecha(c.getString(c.getColumnIndex(COLUMN_FECHA)));
                not.setTitulo(c.getString(c.getColumnIndex(COLUMN_TITULO)));
                not.setContenido(c.getString(c.getColumnIndex(COLUMN_CONTENIDO)));
                not.setExtra(c.getString(c.getColumnIndex(COLUMN_REMITENTE)));
                notificaciones.add(not);
            }
            db.close();
            c.close();
        }catch (Exception e){
            Log.e(TAG,e.getMessage());
        }
        return notificaciones;
    }
}
