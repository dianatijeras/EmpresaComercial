package co.edu.uniquindio.empresacomercial.empresacomercial.model;

import java.util.ArrayList;
import java.util.List;

public class Proveedor {

    private String nit;
    private String nombre;
    private String telefono;
    private String direccion;
    private int idCiudad;
    private List<Producto> productos = new ArrayList<>();

    public Proveedor() {
    }

    public Proveedor(String nit, String nombre, String telefono, String direccion, int idCiudad) {
        this.nit = nit;
        this.nombre = nombre;
        this.telefono = telefono;
        this.direccion = direccion;
        this.idCiudad = idCiudad;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public int getIdCiudad() {
        return idCiudad;
    }

    public void setIdCiudad(int idCiudad) {
        this.idCiudad = idCiudad;
    }

    public List<Producto> getProductos() {
        return productos;
    }

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    /**
     * Dice si el proveedor ya tiene ese producto (se compara por id)
     * @param producto
     * @return
     */
    public boolean tieneProducto(Producto producto){
        for (Producto p : productos){
            if (p.getId() == producto.getId()){
                return true;
            }
        }
        return false;
    }

    /**
     * Agrega el producto si no estaba
     * Devuelve true si se agrego
     * @param producto
     * @return
     */
    public boolean agregarProducto(Producto producto){
        if (tieneProducto(producto)){
            return false;
        }
        productos.add(producto);
        return true;
    }

    /**
     * Quita el producto, se compara por id
     * Devuelve true si se quito
     * @param producto
     * @return
     */
    public boolean quitarProducto(Producto producto){
        return productos.removeIf(p -> p.getId() == producto.getId());
    }
}
