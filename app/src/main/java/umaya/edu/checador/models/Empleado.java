package umaya.edu.checador.models;

/**
 * Created by OSORIO on 29/04/2017.
 */

public class Empleado {

    private int id;
    private int identi;
    private String nombre;

    public Empleado(){
        this.id = 0;
        this.identi = 0;
        this.nombre = "";
    }

    public Empleado(int id, int identi, String nombre) {
        this.id = id;
        this.identi = identi;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getIdenti() {
        return identi;
    }

    public void setIdenti(int identi) {
        this.identi = identi;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
