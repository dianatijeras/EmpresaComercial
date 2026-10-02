package co.edu.uniquindio.empresacomercial.empresacomercial.model;

import java.util.ArrayList;
import java.util.List;

public class Ciudad {

    private int id;
    private String nombre;
    private List<Proveedor> proveedores = new ArrayList<>();

    public Ciudad() {
    }

    public Ciudad(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<Proveedor> getProveedores() {
        return proveedores;
    }

    public void setProveedores(List<Proveedor> proveedores) {
        this.proveedores = proveedores;
    }

    @Override
    public  String toString(){
        return nombre;
    }
}
