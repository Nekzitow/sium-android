package umaya.edu.checador.models;

import android.util.Log;

import java.security.Timestamp;

import math.geom2d.polygon.Polygon2D;
import math.geom2d.polygon.SimplePolygon2D;

/**
 * Created by OSORIO on 26/04/2017.
 */

public class Coordenadas {

    private double latitud;
    private double longitud;
    private int status;
    private Timestamp fecha;

    public Coordenadas() {
        this.latitud = 0;
        this.longitud = 0;
        this.fecha = null;
        this.status = 0;
    }

    public Coordenadas(double latitud, double longitud, int status, Timestamp fecha) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.status = status;
        this.fecha = fecha;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Timestamp getFecha() {
        return fecha;
    }

    public void setFecha(Timestamp fecha) {
        this.fecha = fecha;
    }

    public int getLatitudTrasladada(Coordenadas traslado, int zoom) {
        int lat_int = (int) (latitud * 10000 * zoom);
        int lat_tras = (int) traslado.getLatitud();
        return lat_int - lat_tras;
    }

    public int getLongitudTrasladada(Coordenadas traslado, int zoom) {
        int long_int = (int) (longitud * 10000 * zoom);
        int long_tras = (int) traslado.getLongitud();
        return long_int - long_tras;
    }

    /**
     * Verifica si esta coordenada está fuera del rango dado por el ancho y alto.
     * @param ancho
     * @param alto
     * @param traslado
     * @param zoom
     * @return
     */
    public boolean estaFuera(int ancho, int alto, Coordenadas traslado, int zoom) {
        if (getLatitudTrasladada(traslado, zoom) < 0
                || getLatitudTrasladada(traslado, zoom) > ancho
                || getLongitudTrasladada(traslado, zoom) < 0
                || getLongitudTrasladada(traslado, zoom) > alto)
            return true;
        return false;
    }

    /**
     * Verifica si la coordenada está fuera del terreno.
     * @param terreno Terreno dentro del cual debe estar el ganado.
     * @param traslado Coordenada para calcular el traslado.
     * @param zoom Nivel de acercamiento.
     * @return True si está fuera del área, de lo contrario false.
     */
    public boolean estaFuera( Plantel terreno, Coordenadas traslado, int zoom ) {
        double[] x = new double[terreno.getCoordenadas().size()];
        double[] y = new double[terreno.getCoordenadas().size()];
        for( int i=0; i<terreno.getCoordenadas().size(); i++ ) {
            x[i] = terreno.getCoordenadas().get(i).getLatitudTrasladada(traslado, zoom);
            y[i] = terreno.getCoordenadas().get(i).getLongitudTrasladada(traslado, zoom);
        }
        Log.d("aaaaaaaaaaa", "esta pasando aqui");
        Polygon2D p = new SimplePolygon2D(x,y);
        boolean contenido = p.contains(getLatitudTrasladada(traslado, zoom), getLongitudTrasladada(traslado, zoom));
        //Polygon p = new Polygon(x, y, terreno.getCoordenadas().size());

        // =true;
        //p.contains(getLatitudTrasladada(traslado, zoom), getLongitudTrasladada(traslado, zoom));
        return !contenido;
    }
}
