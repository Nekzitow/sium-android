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
    //url de login provisional debido al tipo de conexion
    public static final String loginUrl = "/checador/login.php";

    public static final String LOGIN_URL_ADMON = "/checador/logina.php";

    public static  String CHECK_URL = "/checador/check.php";

    public static  String CHECK_URL_ADMON = "/checador/checka.php";

    public static final String KEY_USER = "user";
    public static final String KEY_PASSWORD = "password";

    public static final String LOGIN_SUCCESS = "Success";


    public static final String SHARED_PREF_NAME = "loginapp";

    public static final String SHARED_NAME = "nombre";
    public static final String SHARED_ID = "id";
    public static final String SHARED_URL = "url";
    public static final String SHARED_CHECK = "hora";

    public static final String LOGGEDIN_SHARED_PREF = "loggedin";

    public static final String LOGIN_TIPE_USER= "tipo";


}
