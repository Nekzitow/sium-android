package umaya.edu.checador.models;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by OSORIO on 22/10/2016.
 */

public class Configurations {
    public static final String loginUrl = "/docentes/login";

    public static  String CHECK_URL = "http://192.168.1.4/docentes/check";

    public static final String KEY_USER = "user";
    public static final String KEY_PASSWORD = "password";

    public static final String LOGIN_SUCCESS = "Success";


    public static final String SHARED_PREF_NAME = "loginapp";

    public static final String SHARED_NAME = "nombre";
    public static final String SHARED_ID = "id";
    public static final String SHARED_URL = "id";

    public static final String LOGGEDIN_SHARED_PREF = "loggedin";


}
