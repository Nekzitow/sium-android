package umaya.edu.checador.models;

import java.util.ArrayList;

/**
 * Created by OSORIO on 26/04/2017.
 */

public class Plantel {

    private int idPlantel;
    private String nombre;
    private ArrayList<Coordenadas> coordenadas;

    public Plantel() {
        this.idPlantel = 0;
        this.nombre = "";
        this.coordenadas = new ArrayList<Coordenadas>();
    }

    public Plantel(int idPlantel, String nombre, ArrayList<Coordenadas> coordenadas) {
        this.idPlantel = idPlantel;
        this.nombre = nombre;
        this.coordenadas = coordenadas;
    }

    public int getIdPlantel() {
        return idPlantel;
    }

    public void setIdPlantel(int idPlantel) {
        this.idPlantel = idPlantel;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public ArrayList<Coordenadas> getCoordenadas() {
        return coordenadas;
    }

    public void setCoordenadas(ArrayList<Coordenadas> coordenadas) {
        this.coordenadas = coordenadas;
    }

    public int calculaZoomOptimo( int ancho, int alto ) {
        //Buscamos el zoom óptimo desde 100 hasta 1
        int zoom = 1000;
        for( zoom=1000; zoom>=1; zoom-- ) {
            Coordenadas traslado = calculaTraslado(ancho, alto, zoom);
            //Verificamos que ningún punto esté fuera del área de pintado
            if( !estaFuera(ancho, alto, traslado, zoom) )
                break;
        }
        return zoom;
    }

    /**
     * Función que calcula el traslado total que debe hacerse para dibujar el terreno en un área de pintado comenzando en coordenadas 0,0.
     * @param ancho Ancho del área de pintado.
     * @param alto Alto del área de pintado.
     * @return Coordenada que indica el monto a trasladar en x, y.
     */
    public Coordenadas calculaTraslado(int ancho, int alto, int zoom) {
        Coordenadas traslado = new Coordenadas();
        if( coordenadas.size()>0 ) {
            //Inicializamos los mínimos y máximos con los valores de la primer coordenada
            double xmin = coordenadas.get(0).getLatitud();
            double ymin = coordenadas.get(0).getLongitud();
            double xmax = coordenadas.get(0).getLatitud();
            double ymax = coordenadas.get(0).getLongitud();

            //Buscamos dentro de todas las coordenadas los mínimos y máximos
            for( Coordenadas c : coordenadas ) {
                if( c.getLatitud()<xmin )
                    xmin = c.getLatitud();
                if( c.getLatitud()>xmax )
                    xmax = c.getLatitud();
                if( c.getLongitud()<ymin )
                    ymin = c.getLongitud();
                if( c.getLongitud()>ymax )
                    ymax = c.getLongitud();
            }

            //Calculamos el centro del rectángulo formado por xmin,ymin hasta xmax,ymax
            double centrox = (xmin + xmax)/2;
            double centroy = (ymin + ymax)/2;

            //Convertimos las coordenadas del centro a entero, multiplicando por 1000000
            //para recorrer el punto decimal 6 posiciones
            int centrox_int = (int)(centrox*10000*zoom);
            int centroy_int = (int)(centroy*10000*zoom);

            //El traslado será la diferencia para llegar al centrol área de pintado
            int centro_ancho = ancho/2;
            int centro_alto = alto/2;

            traslado.setLatitud(centrox_int-centro_ancho);
            traslado.setLongitud(centroy_int-centro_alto);
        }
        return traslado;
    }

    /**
     * Verifica si alguna coordenada del terreno está fuera del área dada.
     * @param ancho Ancho del área.
     * @param alto Alto del área.
     * @param traslado Coordenada para calcular el traslado necesario.
     * @param zoom Nivel de acercamiento.
     * @return True si alguna coordenada está fuera del área, de lo contrario false.
     */
    private boolean estaFuera( int ancho, int alto, Coordenadas traslado, int zoom ) {
        for( Coordenadas c : coordenadas ) {
            if( c.estaFuera(ancho, alto, traslado, zoom) )
                return true;
        }
        return false;
    }

}
